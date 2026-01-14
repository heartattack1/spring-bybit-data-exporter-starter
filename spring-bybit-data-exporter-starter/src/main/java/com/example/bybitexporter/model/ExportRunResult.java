package com.example.bybitexporter.model;

import java.time.Instant;
import java.util.List;

public class ExportRunResult {
    private final ExportStatus status;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final long exportedCandles;
    private final List<SymbolExportSummary> perSymbol;
    private final String errorMessage;

    public ExportRunResult(ExportStatus status,
                           Instant startedAt,
                           Instant finishedAt,
                           long exportedCandles,
                           List<SymbolExportSummary> perSymbol,
                           String errorMessage) {
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.exportedCandles = exportedCandles;
        this.perSymbol = perSymbol;
        this.errorMessage = errorMessage;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public long getExportedCandles() {
        return exportedCandles;
    }

    public List<SymbolExportSummary> getPerSymbol() {
        return perSymbol;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
