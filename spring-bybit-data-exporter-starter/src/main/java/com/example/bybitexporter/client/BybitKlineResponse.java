package com.example.bybitexporter.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BybitKlineResponse(
    @JsonProperty("retCode") int retCode,
    @JsonProperty("retMsg") String retMsg,
    @JsonProperty("result") BybitKlineResult result
) {
}
