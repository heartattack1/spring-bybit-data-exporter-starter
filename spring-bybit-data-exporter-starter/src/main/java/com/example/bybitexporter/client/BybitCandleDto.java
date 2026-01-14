package com.example.bybitexporter.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BybitCandleDto(
    Instant openTime,
    BigDecimal openPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal closePrice,
    BigDecimal volume,
    BigDecimal turnover,
    Instant closeTime
) {
    public static BybitCandleDto fromRaw(List<String> raw) {
        if (raw.size() < 7) {
            return null;
        }
        Instant openTime = Instant.ofEpochMilli(Long.parseLong(raw.get(0)));
        BigDecimal open = new BigDecimal(raw.get(1));
        BigDecimal high = new BigDecimal(raw.get(2));
        BigDecimal low = new BigDecimal(raw.get(3));
        BigDecimal close = new BigDecimal(raw.get(4));
        BigDecimal volume = new BigDecimal(raw.get(5));
        BigDecimal turnover = new BigDecimal(raw.get(6));
        Instant closeTime = raw.size() > 7
            ? Instant.ofEpochMilli(Long.parseLong(raw.get(7)))
            : openTime;
        return new BybitCandleDto(openTime, open, high, low, close, volume, turnover, closeTime);
    }
}
