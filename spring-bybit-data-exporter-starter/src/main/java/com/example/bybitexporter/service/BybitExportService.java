package com.example.bybitexporter.service;

import com.example.bybitexporter.client.BybitCandleDto;
import com.example.bybitexporter.client.BybitClient;
import com.example.bybitexporter.config.BybitExporterProperties;
import com.example.bybitexporter.model.BybitKline;
import com.example.bybitexporter.model.ExportRunResult;
import com.example.bybitexporter.model.ExportStatus;
import com.example.bybitexporter.model.SymbolExportSummary;
import com.example.bybitexporter.repository.BybitKlineJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BybitExportService {
    private static final Logger logger = LoggerFactory.getLogger(BybitExportService.class);

    private final BybitExporterProperties properties;
    private final BybitClient bybitClient;
    private final BybitKlineJdbcRepository repository;
    private final TransactionTemplate transactionTemplate;

    public BybitExportService(BybitExporterProperties properties,
                              BybitClient bybitClient,
                              BybitKlineJdbcRepository repository,
                              PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.bybitClient = bybitClient;
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public ExportRunResult runExport() {
        Instant startedAt = Instant.now();
        try {
            validateProperties();
            ExecutorService executor = Executors.newFixedThreadPool(properties.getMaxParallelism());
            try {
                List<CompletableFuture<SymbolExportSummary>> futures = properties.getSymbols().stream()
                    .map(symbol -> CompletableFuture.supplyAsync(() -> exportSymbol(symbol), executor))
                    .toList();

                List<SymbolExportSummary> summaries = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

                long total = summaries.stream()
                    .mapToLong(SymbolExportSummary::getExportedCandles)
                    .sum();

                return new ExportRunResult(
                    ExportStatus.COMPLETED,
                    startedAt,
                    Instant.now(),
                    total,
                    summaries,
                    null
                );
            } finally {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            logger.error("Bybit export failed", ex);
            return new ExportRunResult(
                ExportStatus.FAILED,
                startedAt,
                Instant.now(),
                0,
                List.of(),
                ex.getMessage()
            );
        }
    }

    private SymbolExportSummary exportSymbol(String symbol) {
        Instant cursor = properties.getFrom();
        Instant end = properties.getTo();
        Duration intervalDuration = resolveIntervalDuration(properties.getInterval());
        long exported = 0;

        while (cursor.isBefore(end)) {
            Instant chunkEnd = end;
            List<BybitCandleDto> batch = fetchWithRetry(symbol, cursor, chunkEnd);
            if (batch.isEmpty()) {
                logger.info("No data returned for symbol {} at cursor {}", symbol, cursor);
                cursor = cursor.plus(intervalDuration);
                sleepDelay();
                continue;
            }

            List<BybitCandleDto> sorted = batch.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(BybitCandleDto::openTime))
                .toList();

            List<BybitKline> rows = sorted.stream()
                .map(dto -> toEntity(symbol, dto))
                .toList();

            int[] result = transactionTemplate.execute(status -> repository.upsertBatch(rows));
            exported += result == null ? 0 : result.length;

            Instant lastOpen = sorted.get(sorted.size() - 1).openTime();
            cursor = lastOpen.plus(intervalDuration);
            logger.info("Exported {} candles for {} up to {}", rows.size(), symbol, lastOpen);
            sleepDelay();
        }

        return new SymbolExportSummary(symbol, exported);
    }

    private List<BybitCandleDto> fetchWithRetry(String symbol, Instant start, Instant end) {
        int attempts = 0;
        while (attempts < properties.getRetry().getMaxAttempts()) {
            try {
                return bybitClient.fetchCandles(
                    properties.getCategory(),
                    symbol,
                    properties.getInterval(),
                    start,
                    end,
                    properties.getLimit()
                );
            } catch (RestClientException ex) {
                attempts++;
                if (attempts >= properties.getRetry().getMaxAttempts()) {
                    throw ex;
                }
                logger.warn("Retry {}/{} for symbol {} after error: {}",
                    attempts,
                    properties.getRetry().getMaxAttempts(),
                    symbol,
                    ex.getMessage());
                sleepBackoff();
            }
        }
        return List.of();
    }

    private BybitKline toEntity(String symbol, BybitCandleDto dto) {
        return new BybitKline(
            null,
            symbol,
            properties.getInterval(),
            dto.openTime(),
            dto.closeTime(),
            dto.openPrice(),
            dto.highPrice(),
            dto.lowPrice(),
            dto.closePrice(),
            dto.volume(),
            dto.turnover(),
            "bybit"
        );
    }

    private void validateProperties() {
        if (properties.getSymbols() == null || properties.getSymbols().isEmpty()) {
            throw new IllegalArgumentException("bybit.exporter.symbols must not be empty");
        }
        if (properties.getFrom().isAfter(properties.getTo())) {
            throw new IllegalArgumentException("bybit.exporter.from must be before bybit.exporter.to");
        }
        if (properties.getLimit() <= 0) {
            throw new IllegalArgumentException("bybit.exporter.limit must be greater than 0");
        }
        if (properties.getMaxParallelism() <= 0) {
            throw new IllegalArgumentException("bybit.exporter.max-parallelism must be greater than 0");
        }
    }

    private Duration resolveIntervalDuration(String interval) {
        return switch (interval) {
            case "D" -> Duration.ofDays(1);
            case "W" -> Duration.ofDays(7);
            case "M" -> Duration.ofDays(30);
            default -> Duration.ofMinutes(Integer.parseInt(interval));
        };
    }

    private void sleepDelay() {
        sleepMillis(properties.getRequestDelayMs());
    }

    private void sleepBackoff() {
        sleepMillis(properties.getRetry().getBackoffMs());
    }

    private void sleepMillis(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
