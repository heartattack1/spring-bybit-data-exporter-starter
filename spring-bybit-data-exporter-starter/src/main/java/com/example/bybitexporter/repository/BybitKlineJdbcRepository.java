package com.example.bybitexporter.repository;

import com.example.bybitexporter.model.BybitKline;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

public class BybitKlineJdbcRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BybitKlineJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int[] upsertBatch(List<BybitKline> rows) {
        if (rows.isEmpty()) {
            return new int[0];
        }
        String sql = """
            INSERT INTO bybit_kline (
                symbol,
                interval,
                open_time,
                close_time,
                open_price,
                high_price,
                low_price,
                close_price,
                volume,
                turnover,
                source
            ) VALUES (
                :symbol,
                :interval,
                :openTime,
                :closeTime,
                :openPrice,
                :highPrice,
                :lowPrice,
                :closePrice,
                :volume,
                :turnover,
                :source
            )
            ON CONFLICT (symbol, interval, open_time)
            DO UPDATE SET
                close_time = EXCLUDED.close_time,
                open_price = EXCLUDED.open_price,
                high_price = EXCLUDED.high_price,
                low_price = EXCLUDED.low_price,
                close_price = EXCLUDED.close_price,
                volume = EXCLUDED.volume,
                turnover = EXCLUDED.turnover,
                source = EXCLUDED.source
            """;

        MapSqlParameterSource[] batch = rows.stream()
            .map(row -> new MapSqlParameterSource(Map.of(
                "symbol", row.getSymbol(),
                "interval", row.getInterval(),
                "openTime", row.getOpenTime(),
                "closeTime", row.getCloseTime(),
                "openPrice", row.getOpenPrice(),
                "highPrice", row.getHighPrice(),
                "lowPrice", row.getLowPrice(),
                "closePrice", row.getClosePrice(),
                "volume", row.getVolume(),
                "turnover", row.getTurnover(),
                "source", row.getSource()
            )))
            .toArray(MapSqlParameterSource[]::new);

        return jdbcTemplate.batchUpdate(sql, batch);
    }
}
