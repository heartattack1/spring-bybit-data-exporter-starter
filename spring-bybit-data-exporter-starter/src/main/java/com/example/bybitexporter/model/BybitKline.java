package com.example.bybitexporter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("bybit_kline")
public class BybitKline {
    @Id
    private Long id;
    private String symbol;
    private String interval;
    private Instant openTime;
    private Instant closeTime;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal volume;
    private BigDecimal turnover;
    private String source;

    public BybitKline(Long id,
                      String symbol,
                      String interval,
                      Instant openTime,
                      Instant closeTime,
                      BigDecimal openPrice,
                      BigDecimal highPrice,
                      BigDecimal lowPrice,
                      BigDecimal closePrice,
                      BigDecimal volume,
                      BigDecimal turnover,
                      String source) {
        this.id = id;
        this.symbol = symbol;
        this.interval = interval;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.turnover = turnover;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getInterval() {
        return interval;
    }

    public Instant getOpenTime() {
        return openTime;
    }

    public Instant getCloseTime() {
        return closeTime;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public String getSource() {
        return source;
    }
}
