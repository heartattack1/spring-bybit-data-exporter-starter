package com.example.bybitexporter.model;

public class SymbolExportSummary {
    private final String symbol;
    private final long exportedCandles;

    public SymbolExportSummary(String symbol, long exportedCandles) {
        this.symbol = symbol;
        this.exportedCandles = exportedCandles;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getExportedCandles() {
        return exportedCandles;
    }
}
