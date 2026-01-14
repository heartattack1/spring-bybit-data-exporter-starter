package com.example.bybitexporter.config;

import com.example.bybitexporter.client.BybitClient;
import com.example.bybitexporter.controller.BybitExportController;
import com.example.bybitexporter.repository.BybitKlineJdbcRepository;
import com.example.bybitexporter.service.BybitExportService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(prefix = "bybit.exporter", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BybitExporterProperties.class)
public class BybitExporterAutoConfiguration {
    @Bean
    public RestClient bybitRestClient(BybitExporterProperties properties, RestClient.Builder builder) {
        return builder.baseUrl(properties.getBaseUrl()).build();
    }

    @Bean
    public BybitClient bybitClient(RestClient bybitRestClient, BybitExporterProperties properties) {
        return new BybitClient(bybitRestClient, properties);
    }

    @Bean
    public BybitKlineJdbcRepository bybitKlineJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new BybitKlineJdbcRepository(jdbcTemplate);
    }

    @Bean
    public BybitExportService bybitExportService(BybitExporterProperties properties,
                                                 BybitClient bybitClient,
                                                 BybitKlineJdbcRepository repository,
                                                 PlatformTransactionManager transactionManager) {
        return new BybitExportService(properties, bybitClient, repository, transactionManager);
    }

    @Bean
    public BybitExportController bybitExportController(BybitExportService exportService) {
        return new BybitExportController(exportService);
    }
}
