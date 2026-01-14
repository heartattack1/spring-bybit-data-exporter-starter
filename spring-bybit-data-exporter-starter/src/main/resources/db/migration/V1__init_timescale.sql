CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS bybit_kline (
    id BIGSERIAL PRIMARY KEY,
    symbol TEXT NOT NULL,
    interval TEXT NOT NULL,
    open_time TIMESTAMPTZ NOT NULL,
    close_time TIMESTAMPTZ,
    open_price NUMERIC NOT NULL,
    high_price NUMERIC NOT NULL,
    low_price NUMERIC NOT NULL,
    close_price NUMERIC NOT NULL,
    volume NUMERIC NOT NULL,
    turnover NUMERIC NOT NULL,
    source TEXT NOT NULL DEFAULT 'bybit',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_bybit_kline_symbol_interval_open UNIQUE (symbol, interval, open_time)
);

SELECT create_hypertable('bybit_kline', 'open_time', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_bybit_kline_symbol_interval_time_desc
    ON bybit_kline (symbol, interval, open_time DESC);

CREATE INDEX IF NOT EXISTS idx_bybit_kline_open_time_desc
    ON bybit_kline (open_time DESC);
