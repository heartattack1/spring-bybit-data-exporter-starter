package com.example.bybitexporter.client;

import com.example.bybitexporter.config.BybitExporterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class BybitClient {
    private static final Logger logger = LoggerFactory.getLogger(BybitClient.class);

    private final RestClient restClient;
    private final BybitExporterProperties properties;

    public BybitClient(RestClient restClient, BybitExporterProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public List<BybitCandleDto> fetchCandles(String category,
                                             String symbol,
                                             String interval,
                                             Instant start,
                                             Instant end,
                                             int limit) {
        BybitKlineResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v5/market/kline")
                .queryParam("category", category)
                .queryParam("symbol", symbol)
                .queryParam("interval", interval)
                .queryParam("start", start.toEpochMilli())
                .queryParam("end", end.toEpochMilli())
                .queryParam("limit", limit)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, result) -> {
                throw new RestClientException("Bybit API responded with status " + result.getStatusCode());
            })
            .body(BybitKlineResponse.class);

        if (response == null) {
            throw new RestClientException("Bybit API response was empty");
        }

        if (response.retCode() != 0) {
            throw new RestClientException("Bybit API error: " + response.retMsg());
        }

        BybitKlineResult result = response.result();
        if (result == null || result.list() == null) {
            logger.warn("Bybit API returned no data for symbol {}", symbol);
            return List.of();
        }

        return result.list().stream()
            .filter(list -> list.size() >= 7)
            .map(BybitCandleDto::fromRaw)
            .filter(Objects::nonNull)
            .toList();
    }
}
