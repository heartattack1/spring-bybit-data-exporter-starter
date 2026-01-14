package com.example.bybitexporter.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BybitKlineResult(
    @JsonProperty("list") List<List<String>> list,
    @JsonProperty("category") String category,
    @JsonProperty("symbol") String symbol
) {
}
