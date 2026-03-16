package com.dev.export.service;

import com.dev.borrow.model.Borrow;
import com.dev.export.model.ExportFormat;
import com.dev.statistics.dto.BookStatisticsResponse;
import com.dev.statistics.dto.OverdueReportResponse;
import com.dev.statistics.dto.PenaltyReportResponse;

import java.util.List;

public interface ExportService {
    
    byte[] exportBorrowHistory(List<Borrow> borrows, ExportFormat format);
    
    byte[] exportOverdueReport(List<OverdueReportResponse> overdueList, ExportFormat format);
    
    byte[] exportPenaltyReport(List<PenaltyReportResponse> penalties, ExportFormat format);
    
    byte[] exportPopularBooks(List<BookStatisticsResponse> books, ExportFormat format);
    
    String getFileName(String prefix, ExportFormat format);
    
    String getContentType(ExportFormat format);
}