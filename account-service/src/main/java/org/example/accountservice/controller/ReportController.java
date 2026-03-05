package org.example.accountservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.MovementReportDto;
import org.example.accountservice.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for generating reports.
 * Provides endpoints for account statements and other reports.
 */
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Report Controller", description = "API for generating reports")
public class ReportController {
    
    private final ReportService reportService;
    
    /** Generates an account statement for a client. */
    @GetMapping("/{client-id}")
    @Operation(summary = "Generate account statement", 
               description = "Generates an account statement for a client within a date range. Returns a flat list of movements with all account details. Supports JSON and Excel formats. Shows ALL accounts of the client with their movements.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public Mono<ResponseEntity<?>> generateAccountStatement(
            @Parameter(description = "Client ID") @PathVariable("client-id") Long clientId,
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Response format: json or excel") 
            @RequestParam(value = "format", defaultValue = "json") String format) {
        
        log.info("GET /reports/{} - Generating account statement from {} to {} in format {}", 
                clientId, startDate, endDate, format);
        
        if ("excel".equalsIgnoreCase(format)) {
            return reportService.generateAccountStatementExcel(clientId, startDate, endDate)
                    .map(excelBytes -> {
                        String filename = "estado_cuenta_cliente_" + clientId + "_" + 
                                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .body(excelBytes);
                    });
        } else {
            return reportService.generateAccountStatement(clientId, startDate, endDate)
                    .map(report -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(report));
        }
    }
    
    @GetMapping("/{client-id}/movements")
    @Operation(summary = "Generate movement report by date and user", 
               description = "Generates a detailed movement report for a client within a date range. Each row includes client name, account info, and movement details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movement report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public Mono<ResponseEntity<List<MovementReportDto>>> generateMovementReport(
            @Parameter(description = "Client ID") @PathVariable("client-id") Long clientId,
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("GET /reports/{}/movements - Generating movement report from {} to {}", 
                clientId, startDate, endDate);
        
        return reportService.generateMovementReport(clientId, startDate, endDate)
                .map(report -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(report));
    }
}

