# spring-bybit-data-exporter-starter

A Spring Boot starter that exports Bybit public candlestick (kline) market data over a multi-year range and persists it into PostgreSQL with TimescaleDB. The starter exposes exactly one unauthenticated endpoint to trigger the export.

## Features

- Fetches Bybit public kline data across multi-year ranges using pagination.
- Saves candles into a TimescaleDB hypertable with idempotent upserts.
- Configurable category, symbols, interval, date range, rate limits, and retries.
- Starter-style auto-configuration with `@ConfigurationProperties`.
- Demo application included.

## Prerequisites

- Docker
- Java 21 (OpenJDK 21 recommended)

## Start the database

```bash
docker compose up -d
```

## Run the demo application

```bash
./gradlew :demo-app:bootRun
```

## Trigger an export

```bash
curl -X POST http://localhost:8080/bybit-export/run
```

## Configuration reference

All configuration is under `bybit.exporter`.

```yaml
bybit:
  exporter:
    enabled: true                # Enable/disable the exporter auto-config
    base-url: https://api.bybit.com  # Bybit public API base URL
    category: spot               # spot, linear, etc.
    symbols:                     # List of symbols to export
      - BTCUSDT
      - ETHUSDT
    interval: "60"               # Bybit interval string (1,3,5,15,60,D,...)
    from: 2020-01-01T00:00:00Z   # ISO-8601 start timestamp
    to: 2020-02-01T00:00:00Z     # ISO-8601 end timestamp
    limit: 1000                  # Max candles per request (Bybit max is 1000)
    max-parallelism: 2           # Parallelism across symbols
    request-delay-ms: 200        # Delay between API calls (ms)
    retry:
      max-attempts: 3            # Retry attempts on transient errors
      backoff-ms: 500            # Backoff between retries (ms)
```

## Endpoint

- `POST /bybit-export/run`
  - Returns JSON with status, timestamps, exported candle counts, per-symbol summary, and errors.
  - Runs synchronously (the HTTP call blocks until completion).

## Database schema

The starter ships Flyway migration SQL that creates the `bybit_kline` table and converts it to a TimescaleDB hypertable.

**Table: `bybit_kline`**

- `id` BIGSERIAL PRIMARY KEY
- `symbol` TEXT
- `interval` TEXT
- `open_time` TIMESTAMPTZ (hypertable time column)
- `close_time` TIMESTAMPTZ
- `open_price` NUMERIC
- `high_price` NUMERIC
- `low_price` NUMERIC
- `close_price` NUMERIC
- `volume` NUMERIC
- `turnover` NUMERIC
- `source` TEXT default `bybit`
- `created_at` TIMESTAMPTZ default `now()`

Indexes:
- `(symbol, interval, open_time desc)`
- `open_time desc`

A unique constraint on `(symbol, interval, open_time)` enables idempotent upserts.

## Notes on Bybit API limits and data completeness

- Bybit public kline APIs typically return up to 1000 candles per request. The exporter paginates across the configured time range.
- If the API returns empty pages, the exporter advances by one interval to avoid infinite loops.
- Configure `request-delay-ms` to be polite and avoid rate limits. Retries are used for transient failures.

## Module layout

- `spring-bybit-data-exporter-starter`: Starter module with auto-configuration, services, client, and Flyway migration.
- `demo-app`: Sample Spring Boot application using the starter.
