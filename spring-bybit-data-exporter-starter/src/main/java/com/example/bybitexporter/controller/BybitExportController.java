package com.example.bybitexporter.controller;

import com.example.bybitexporter.model.ExportRunResult;
import com.example.bybitexporter.service.BybitExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bybit-export")
public class BybitExportController {
    private static final Logger logger = LoggerFactory.getLogger(BybitExportController.class);

    private final BybitExportService exportService;

    public BybitExportController(BybitExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/run")
    public ExportRunResult runExport() {
        logger.info("Bybit export triggered");
        return exportService.runExport();
    }
}
