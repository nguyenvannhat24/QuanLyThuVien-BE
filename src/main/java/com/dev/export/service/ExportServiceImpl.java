package com.dev.export.service;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import com.dev.export.model.ExportFormat;
import com.dev.statistics.dto.BookStatisticsResponse;
import com.dev.statistics.dto.OverdueReportResponse;
import com.dev.statistics.dto.PenaltyReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] exportBorrowHistory(List<Borrow> borrows, ExportFormat format) {
        if (format == ExportFormat.EXCEL) {
            return exportBorrowHistoryToExcel(borrows);
        }
        return exportBorrowHistoryToCsv(borrows);
    }

    @Override
    public byte[] exportOverdueReport(List<OverdueReportResponse> overdueList, ExportFormat format) {
        if (format == ExportFormat.EXCEL) {
            return exportOverdueReportToExcel(overdueList);
        }
        return exportOverdueReportToCsv(overdueList);
    }

    @Override
    public byte[] exportPenaltyReport(List<PenaltyReportResponse> penalties, ExportFormat format) {
        if (format == ExportFormat.EXCEL) {
            return exportPenaltyReportToExcel(penalties);
        }
        return exportPenaltyReportToCsv(penalties);
    }

    @Override
    public byte[] exportPopularBooks(List<BookStatisticsResponse> books, ExportFormat format) {
        if (format == ExportFormat.EXCEL) {
            return exportPopularBooksToExcel(books);
        }
        return exportPopularBooksToCsv(books);
    }

    @Override
    public String getFileName(String prefix, ExportFormat format) {
        String timestamp = LocalDate.now().toString();
        String extension = format == ExportFormat.EXCEL ? "xlsx" : "csv";
        return String.format("%s-%s.%s", prefix, timestamp, extension);
    }

    @Override
    public String getContentType(ExportFormat format) {
        return format == ExportFormat.EXCEL 
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "text/csv; charset=UTF-8";
    }

    // ==================== CSV Export Methods ====================

    private byte[] exportBorrowHistoryToCsv(List<Borrow> borrows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), CSVFormat.DEFAULT
                .withHeader("Mã mượn", "Tên độc giả", "Email", "Tên sách", "Mã bản sao", "Ngày mượn", "Ngày hạn trả", "Ngày trả", "Trạng thái", "Tiền phạt"))) {
            
            for (Borrow borrow : borrows) {
                printer.printRecord(
                        borrow.getId(),
                        borrow.getUser().getFullName(),
                        borrow.getUser().getEmail(),
                        borrow.getBookCopy().getBook().getTitle(),
                        borrow.getBookCopy().getCopyCode(),
                        borrow.getBorrowDate() != null ? borrow.getBorrowDate().format(DATE_FORMATTER) : "",
                        borrow.getDueDate() != null ? borrow.getDueDate().format(DATE_FORMATTER) : "",
                        borrow.getReturnDate() != null ? borrow.getReturnDate().format(DATE_FORMATTER) : "",
                        borrow.getStatus() != null ? borrow.getStatus().name() : "",
                        borrow.getFineAmount() != null ? borrow.getFineAmount().toString() : "0"
                );
            }
        } catch (IOException e) {
            log.error("Error exporting borrow history to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
        return out.toByteArray();
    }

    private byte[] exportOverdueReportToCsv(List<OverdueReportResponse> overdueList) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), CSVFormat.DEFAULT
                .withHeader("Mã mượn", "Mã độc giả", "Tên độc giả", "Email", "Mã sách", "Tên sách", "ISBN", "Ngày mượn", "Ngày hạn trả", "Số ngày quá hạn"))) {
            
            for (OverdueReportResponse overdue : overdueList) {
                printer.printRecord(
                        overdue.getBorrowId(),
                        overdue.getReaderId(),
                        overdue.getReaderName(),
                        overdue.getReaderEmail(),
                        overdue.getBookCopyId(),
                        overdue.getBookTitle(),
                        overdue.getIsbn(),
                        overdue.getBorrowDate() != null ? overdue.getBorrowDate().format(DATE_FORMATTER) : "",
                        overdue.getDueDate() != null ? overdue.getDueDate().format(DATE_FORMATTER) : "",
                        overdue.getDaysOverdue()
                );
            }
        } catch (IOException e) {
            log.error("Error exporting overdue report to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
        return out.toByteArray();
    }

    private byte[] exportPenaltyReportToCsv(List<PenaltyReportResponse> penalties) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), CSVFormat.DEFAULT
                .withHeader("Mã phạt", "Loại phạt", "Số tiền", "Trạng thái", "Mã độc giả", "Tên độc giả", "Ngày tạo", "Ngày thanh toán"))) {
            
            for (PenaltyReportResponse penalty : penalties) {
                printer.printRecord(
                        penalty.getPenaltyId(),
                        penalty.getPenaltyType(),
                        penalty.getAmount(),
                        penalty.getStatus(),
                        penalty.getReaderId(),
                        penalty.getReaderName(),
                        penalty.getCreatedDate() != null ? penalty.getCreatedDate().format(DATE_FORMATTER) : "",
                        penalty.getPaidDate() != null ? penalty.getPaidDate().format(DATE_FORMATTER) : ""
                );
            }
        } catch (IOException e) {
            log.error("Error exporting penalty report to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
        return out.toByteArray();
    }

    private byte[] exportPopularBooksToCsv(List<BookStatisticsResponse> books) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), CSVFormat.DEFAULT
                .withHeader("STT", "Mã sách", "Tên sách", "ISBN", "Số lần mượn"))) {
            
            int index = 1;
            for (BookStatisticsResponse book : books) {
                printer.printRecord(
                        index++,
                        book.getBookId(),
                        book.getTitle(),
                        book.getIsbn(),
                        book.getBorrowCount()
                );
            }
        } catch (IOException e) {
            log.error("Error exporting popular books to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
        return out.toByteArray();
    }

    // ==================== Excel Export Methods ====================

    private byte[] exportBorrowHistoryToExcel(List<Borrow> borrows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Lịch sử mượn sách");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            String[] headers = {"Mã mượn", "Tên độc giả", "Email", "Tên sách", "Mã bản sao", "Ngày mượn", "Ngày hạn trả", "Ngày trả", "Trạng thái", "Tiền phạt"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (Borrow borrow : borrows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(borrow.getId());
                row.createCell(1).setCellValue(borrow.getUser().getFullName());
                row.createCell(2).setCellValue(borrow.getUser().getEmail());
                row.createCell(3).setCellValue(borrow.getBookCopy().getBook().getTitle());
                row.createCell(4).setCellValue(borrow.getBookCopy().getCopyCode());
                row.createCell(5).setCellValue(borrow.getBorrowDate() != null ? borrow.getBorrowDate().format(DATE_FORMATTER) : "");
                row.createCell(6).setCellValue(borrow.getDueDate() != null ? borrow.getDueDate().format(DATE_FORMATTER) : "");
                row.createCell(7).setCellValue(borrow.getReturnDate() != null ? borrow.getReturnDate().format(DATE_FORMATTER) : "");
                row.createCell(8).setCellValue(borrow.getStatus() != null ? borrow.getStatus().name() : "");
                row.createCell(9).setCellValue(borrow.getFineAmount() != null ? borrow.getFineAmount().doubleValue() : 0);
                
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting borrow history to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    private byte[] exportOverdueReportToExcel(List<OverdueReportResponse> overdueList) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Báo cáo quá hạn");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            String[] headers = {"Mã mượn", "Mã độc giả", "Tên độc giả", "Email", "Mã sách", "Tên sách", "ISBN", "Ngày mượn", "Ngày hạn trả", "Số ngày quá hạn"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (OverdueReportResponse overdue : overdueList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(overdue.getBorrowId());
                row.createCell(1).setCellValue(overdue.getReaderId());
                row.createCell(2).setCellValue(overdue.getReaderName());
                row.createCell(3).setCellValue(overdue.getReaderEmail());
                row.createCell(4).setCellValue(overdue.getBookCopyId());
                row.createCell(5).setCellValue(overdue.getBookTitle());
                row.createCell(6).setCellValue(overdue.getIsbn());
                row.createCell(7).setCellValue(overdue.getBorrowDate() != null ? overdue.getBorrowDate().format(DATE_FORMATTER) : "");
                row.createCell(8).setCellValue(overdue.getDueDate() != null ? overdue.getDueDate().format(DATE_FORMATTER) : "");
                row.createCell(9).setCellValue(overdue.getDaysOverdue());
                
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting overdue report to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    private byte[] exportPenaltyReportToExcel(List<PenaltyReportResponse> penalties) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Báo cáo phạt");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            
            String[] headers = {"Mã phạt", "Loại phạt", "Số tiền", "Trạng thái", "Mã độc giả", "Tên độc giả", "Ngày tạo", "Ngày thanh toán"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (PenaltyReportResponse penalty : penalties) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(penalty.getPenaltyId());
                row.createCell(1).setCellValue(penalty.getPenaltyType());
                row.createCell(2).setCellValue(penalty.getAmount().doubleValue());
                row.createCell(2).setCellStyle(moneyStyle);
                row.createCell(3).setCellValue(penalty.getStatus());
                row.createCell(4).setCellValue(penalty.getReaderId());
                row.createCell(5).setCellValue(penalty.getReaderName());
                row.createCell(6).setCellValue(penalty.getCreatedDate() != null ? penalty.getCreatedDate().format(DATE_FORMATTER) : "");
                row.createCell(7).setCellValue(penalty.getPaidDate() != null ? penalty.getPaidDate().format(DATE_FORMATTER) : "");
                
                for (int i = 0; i < headers.length; i++) {
                    if (i != 2) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting penalty report to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    private byte[] exportPopularBooksToExcel(List<BookStatisticsResponse> books) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Sách phổ biến");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            String[] headers = {"STT", "Mã sách", "Tên sách", "ISBN", "Số lần mượn"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            int index = 1;
            for (BookStatisticsResponse book : books) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(book.getBookId());
                row.createCell(2).setCellValue(book.getTitle());
                row.createCell(3).setCellValue(book.getIsbn());
                row.createCell(4).setCellValue(book.getBorrowCount());
                
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting popular books to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    // ==================== Helper Methods ====================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}