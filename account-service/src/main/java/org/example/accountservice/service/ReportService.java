package org.example.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.accountservice.dto.MovementReportDto;
import org.example.accountservice.entity.Movement;
import org.example.accountservice.repository.AccountRepository;
import org.example.accountservice.repository.MovementRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final CustomerClientService customerClientService;
    
    public Mono<List<MovementReportDto>> generateAccountStatement(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating account statement for client ID: {} from {} to {}", clientId, startDate, endDate);
        
        return generateMovementReport(clientId, startDate, endDate)
                .doOnSuccess(r -> log.info("Account statement generated successfully for client ID: {}", clientId))
                .doOnError(error -> log.error("Error generating account statement: {}", error.getMessage()));
    }
    
    public Mono<byte[]> generateAccountStatementExcel(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating Excel account statement for client ID: {} from {} to {}", clientId, startDate, endDate);
        
        return generateMovementReport(clientId, startDate, endDate)
                .map(movements -> {
                    try (Workbook workbook = new XSSFWorkbook()) {
                        Sheet sheet = workbook.createSheet("Estado de Cuenta");
                        
                        CellStyle headerStyle = workbook.createCellStyle();
                        Font headerFont = workbook.createFont();
                        headerFont.setBold(true);
                        headerFont.setFontHeightInPoints((short) 12);
                        headerStyle.setFont(headerFont);
                        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        headerStyle.setBorderBottom(BorderStyle.THIN);
                        headerStyle.setBorderTop(BorderStyle.THIN);
                        headerStyle.setBorderLeft(BorderStyle.THIN);
                        headerStyle.setBorderRight(BorderStyle.THIN);
                        headerStyle.setAlignment(HorizontalAlignment.CENTER);
                        
                        CellStyle dataStyle = workbook.createCellStyle();
                        dataStyle.setBorderBottom(BorderStyle.THIN);
                        dataStyle.setBorderTop(BorderStyle.THIN);
                        dataStyle.setBorderLeft(BorderStyle.THIN);
                        dataStyle.setBorderRight(BorderStyle.THIN);
                        
                        CellStyle dateStyle = workbook.createCellStyle();
                        dateStyle.cloneStyleFrom(dataStyle);
                        CreationHelper createHelper = workbook.getCreationHelper();
                        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));
                        
                        CellStyle numberStyle = workbook.createCellStyle();
                        numberStyle.cloneStyleFrom(dataStyle);
                        numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
                        
                        CellStyle booleanStyle = workbook.createCellStyle();
                        booleanStyle.cloneStyleFrom(dataStyle);
                        booleanStyle.setAlignment(HorizontalAlignment.CENTER);
                        
                        int rowNum = 0;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        
                        Row headerRow = sheet.createRow(rowNum++);
                        String[] headers = {"Fecha", "Cliente", "Número Cuenta", "Tipo", "Saldo Inicial", 
                                           "Estado", "Valor movimiento", "Tipo Movimiento", "Saldo Disponible"};
                        for (int i = 0; i < headers.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellValue(headers[i]);
                            cell.setCellStyle(headerStyle);
                        }
                        
                        for (MovementReportDto movement : movements) {
                            Row movementRow = sheet.createRow(rowNum++);
                            int colNum = 0;
                            
                            Cell dateCell = movementRow.createCell(colNum++);
                            if (movement.getFecha() != null) {
                                dateCell.setCellValue(movement.getFecha().format(formatter));
                            }
                            dateCell.setCellStyle(dateStyle);
                            
                            Cell clienteCell = movementRow.createCell(colNum++);
                            clienteCell.setCellValue(movement.getCliente() != null ? movement.getCliente() : "");
                            clienteCell.setCellStyle(dataStyle);
                            
                            Cell numeroCuentaCell = movementRow.createCell(colNum++);
                            numeroCuentaCell.setCellValue(movement.getNumeroCuenta() != null ? movement.getNumeroCuenta() : "");
                            numeroCuentaCell.setCellStyle(dataStyle);
                            
                            Cell tipoCell = movementRow.createCell(colNum++);
                            tipoCell.setCellValue(movement.getTipo() != null ? movement.getTipo() : "");
                            tipoCell.setCellStyle(dataStyle);
                            
                            Cell saldoInicialCell = movementRow.createCell(colNum++);
                            if (movement.getSaldoInicial() != null) {
                                saldoInicialCell.setCellValue(movement.getSaldoInicial().doubleValue());
                            }
                            saldoInicialCell.setCellStyle(numberStyle);
                            
                            Cell estadoCell = movementRow.createCell(colNum++);
                            if (movement.getEstado() != null) {
                                estadoCell.setCellValue(movement.getEstado() ? "True" : "False");
                            }
                            estadoCell.setCellStyle(booleanStyle);
                            
                            Cell valorMovimientoCell = movementRow.createCell(colNum++);
                            if (movement.getValorMovimiento() != null) {
                                valorMovimientoCell.setCellValue(movement.getValorMovimiento().doubleValue());
                            }
                            valorMovimientoCell.setCellStyle(numberStyle);
                            
                            Cell tipoMovimientoCell = movementRow.createCell(colNum++);
                            tipoMovimientoCell.setCellValue(movement.getTipoMovimiento() != null ? movement.getTipoMovimiento() : "");
                            tipoMovimientoCell.setCellStyle(dataStyle);
                            
                            Cell saldoDisponibleCell = movementRow.createCell(colNum++);
                            if (movement.getSaldoDisponible() != null) {
                                saldoDisponibleCell.setCellValue(movement.getSaldoDisponible().doubleValue());
                            }
                            saldoDisponibleCell.setCellStyle(numberStyle);
                        }
                        
                        for (int i = 0; i < headers.length; i++) {
                            sheet.autoSizeColumn(i);
                        }
                        
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        workbook.write(outputStream);
                        return outputStream.toByteArray();
                    } catch (IOException e) {
                        log.error("Error generating Excel file: {}", e.getMessage(), e);
                        throw new RuntimeException("Error generating Excel file", e);
                    }
                })
                .doOnSuccess(bytes -> log.info("Excel account statement generated successfully for client ID: {}", clientId))
                .doOnError(error -> log.error("Error generating Excel account statement: {}", error.getMessage()));
    }
    
    public Mono<List<MovementReportDto>> generateMovementReport(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating movement report for client ID: {} from {} to {}", clientId, startDate, endDate);
        
        return customerClientService.getCustomerById(clientId)
                .flatMap(customerInfo -> 
                    accountRepository.findByCustomerId(clientId)
                            .collectList()
                            .flatMap(accounts -> {
                                if (accounts.isEmpty()) {
                                    return Mono.error(new RuntimeException("No accounts found for customer ID: " + clientId));
                                }
                                
                                List<Mono<List<MovementReportDto>>> movementReports = accounts.stream()
                                        .map(account -> 
                                            movementRepository.findByAccountIdAndMovementDateBetween(account.getId(), startDate, endDate)
                                                    .collectList()
                                                    .map(movements -> {
                                                        if (!movements.isEmpty()) {
                                                            List<Movement> sortedMovements = movements.stream()
                                                                    .sorted(Comparator.comparing(Movement::getMovementDate))
                                                                    .collect(Collectors.toList());
                                                            
                                                            return sortedMovements.stream()
                                                                    .map(movement -> {
                                                                        String tipoMovimiento = "DEBIT".equalsIgnoreCase(movement.getMovementType()) 
                                                                                ? "Débito" 
                                                                                : "Crédito";
                                                                        
                                                                        BigDecimal saldoInicial;
                                                                        if ("CREDIT".equalsIgnoreCase(movement.getMovementType())) {
                                                                            saldoInicial = movement.getBalance().subtract(movement.getValue());
                                                                        } else {
                                                                            saldoInicial = movement.getBalance().add(movement.getValue());
                                                                        }
                                                                        
                                                                        return MovementReportDto.builder()
                                                                                .fecha(movement.getMovementDate())
                                                                                .cliente(customerInfo.getName())
                                                                                .numeroCuenta(account.getAccountNumber())
                                                                                .tipo(account.getAccountType())
                                                                                .saldoInicial(saldoInicial)
                                                                                .estado(account.getStatus())
                                                                                .valorMovimiento(movement.getValue())
                                                                                .tipoMovimiento(tipoMovimiento)
                                                                                .saldoDisponible(movement.getBalance())
                                                                                .build();
                                                                    })
                                                                    .collect(Collectors.toList());
                                                        } else {
                                                            LocalDateTime fecha = account.getCreatedAt() != null ? account.getCreatedAt() : LocalDateTime.now();
                                                            return List.of(MovementReportDto.builder()
                                                                    .fecha(fecha)
                                                                    .cliente(customerInfo.getName())
                                                                    .numeroCuenta(account.getAccountNumber())
                                                                    .tipo(account.getAccountType())
                                                                    .saldoInicial(account.getInitialBalance() != null ? account.getInitialBalance() : account.getCurrentBalance())
                                                                    .estado(account.getStatus())
                                                                    .valorMovimiento(BigDecimal.ZERO)
                                                                    .tipoMovimiento("Sin movimientos")
                                                                    .saldoDisponible(account.getCurrentBalance())
                                                                    .build());
                                                        }
                                                    })
                                        )
                                        .collect(Collectors.toList());
                                
                                return Flux.concat(movementReports)
                                        .collectList()
                                        .map(lists -> lists.stream()
                                                .flatMap(List::stream)
                                                .sorted(Comparator.comparing(MovementReportDto::getFecha).reversed())
                                                .collect(Collectors.toList()));
                            })
                )
                .doOnSuccess(r -> log.info("Movement report generated successfully for client ID: {}", clientId))
                .doOnError(error -> log.error("Error generating movement report: {}", error.getMessage()));
    }
}

