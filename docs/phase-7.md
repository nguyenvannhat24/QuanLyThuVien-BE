# Phase 7: Báo Cáo và Xuất File

## Tổng Quan

**Ngày bắt đầu:** 16/03/2026  
**Dự kiến hoàn thành:** 16/03/2026  
**Mục tiêu:** Triển khai xuất báo cáo ra file CSV và Excel  
**Branch:** `phase-7`

---

## Mục Tiêu Chi Tiết

1. Thêm dependency Apache POI để hỗ trợ xuất file Excel
2. Tạo Export Service với các method xuất CSV và Excel
3. Thêm các API endpoints xuất file cho các báo cáo hiện có
4. Tối ưu định dạng file xuất ra

---

## Hiện Trạng Module Statistics

Module statistics hiện đã có đầy đủ các báo cáo:

### API Endpoints hiện có:
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/reports/dashboard` | Dashboard tổng quan |
| GET | `/api/reports/borrow-history` | Lịch sử mượn (có date range) |
| GET | `/api/reports/overdue` | Báo cáo sách quá hạn |
| GET | `/api/reports/penalties` | Báo cáo phạt (filter theo status) |
| GET | `/api/reports/popular-books` | Top sách phổ biến |

### DTOs hiện có:
- `DashboardMetricsResponse.java`
- `BookStatisticsResponse.java`
- `BorrowTrendResponse.java`
- `OverdueReportResponse.java`
- `PenaltyReportResponse.java`

### Chức năng cần thêm:
- Xuất file CSV cho tất cả các báo cáo
- Xuất file Excel (.xlsx) cho tất cả các báo cáo

---

## Tính Năng Cần Triển Khai

### 1. Thêm Apache POI Dependency

**pom.xml** - Thêm dependency:
```xml
<!-- Apache POI for Excel export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

---

### 2. Tạo Export Service

**ExportFormat enum** - Định dạng export:
```java
public enum ExportFormat {
    CSV,
    EXCEL
}
```

**ExportService.java** - Interface:
```java
public interface ExportService {
    
    byte[] exportBorrowHistory(List<Borrow> borrows, ExportFormat format);
    
    byte[] exportOverdueReport(List<OverdueReportResponse> overdueList, ExportFormat format);
    
    byte[] exportPenaltyReport(List<PenaltyReportResponse> penalties, ExportFormat format);
    
    byte[] exportPopularBooks(List<BookStatisticsResponse> books, ExportFormat format);
}
```

**ExportServiceImpl.java** - Implementation:

#### CSV Export:
- Sử dụng Apache Commons CSV (đã có trong pom.xml)
- Header với UTF-8 BOM để hiển thị tiếng Việt đúng trên Excel
- Phân cách bằng dấu phẩy

#### Excel Export:
- Sử dụng Apache POI
- Tạo workbook với các sheet riêng
- Định dạng header (in đậm, màu nền)
- Auto-fit column width
- Định dạng ngày tháng (dd/MM/yyyy)
- Freeze header row

---

### 3. Cập Nhật ReportsController

Thêm các endpoints xuất file:

```java
@GetMapping("/borrow-history/export")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public ResponseEntity<?> exportBorrowHistory(
        @RequestParam(defaultValue = "CSV") ExportFormat format,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    // Lấy dữ liệu và gọi export service
    // Trả về file với Content-Disposition: attachment
}

@GetMapping("/overdue/export")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public ResponseEntity<?> exportOverdueReport(
        @RequestParam(defaultValue = "CSV") ExportFormat format) {
    // Xuất báo cáo quá hạn
}

@GetMapping("/penalties/export")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public ResponseEntity<?> exportPenaltyReport(
        @RequestParam(defaultValue = "CSV") ExportFormat format,
        @RequestParam(required = false) String status) {
    // Xuất báo cáo phạt
}

@GetMapping("/popular-books/export")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public ResponseEntity<?> exportPopularBooks(
        @RequestParam(defaultValue = "CSV") ExportFormat format,
        @RequestParam(required = false, defaultValue = "10") int limit) {
    // Xuất top sách phổ biến
}
```

---

### 4. Định Dạng File Xuất

#### CSV Format:
```
Mã mượn,Tên độc giả,Email,Tên sách,Mã bản sao,Ngày mượn,Ngày hạn trả,Ngày trả,Trạng thái
1,Nguyễn Văn A,email@example.com,Java Programming,CP001,15/01/2026,22/01/2026,21/01/2026,Đã trả
```

#### Excel Format:
- Header row với màu nền xanh (#4472C4), chữ trắng
- Các row dữ liệu xen kẽ màu (white, #E7E6E6)
- Border cho các cells
- Căn lề phải cho các cột số
- Định dạng tiền tệ cho cột số tiền

---

## Files Cần Tạo Mới

### Service:
1. `src/main/java/com/dev/export/model/ExportFormat.java`
2. `src/main/java/com/dev/export/service/ExportService.java`
3. `src/main/java/com/dev/export/service/ExportServiceImpl.java`

---

## Files Cần Sửa Đổi

1. **pom.xml** - Thêm Apache POI dependency
2. **ReportsController.java** - Thêm các endpoints xuất file

---

## API Endpoints Mới

### Export Endpoints:
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/reports/borrow-history/export?format=CSV&startDate=&endDate=` | Xuất lịch sử mượn |
| GET | `/api/reports/overdue/export?format=CSV` | Xuất báo cáo quá hạn |
| GET | `/api/reports/penalties/export?format=CSV&status=` | Xuất báo cáo phạt |
| GET | `/api/reports/popular-books/export?format=CSV&limit=` | Xuất top sách phổ biến |

### Request Parameters:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| format | String | No | CSV | Định dạng: CSV hoặc EXCEL |
| startDate | Date | No | -1 month | Ngày bắt đầu (cho borrow-history) |
| endDate | Date | No | today | Ngày kết thúc (cho borrow-history) |
| status | String | No | all | Lọc theo status (cho penalties) |
| limit | Integer | No | 10 | Số lượng sách (cho popular-books) |

### Response Headers:
```
Content-Type: text/csv hoặc application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=borrow-history-2026-03-16.csv
```

---

## Error Handling

1. **Invalid date range**: Trả về 400 Bad Request
2. **Empty data**: Trả về file với header only, thông báo "No data to export"
3. **Export failed**: Ghi log lỗi, trả về 500 Internal Server Error với message

---

## Testing

### Manual Testing Checklist:
- [ ] Xuất borrow-history thành CSV, mở bằng Excel hiển thị tiếng Việt đúng
- [ ] Xuất borrow-history thành Excel, kiểm tra định dạng đẹp
- [ ] Xuất overdue report với dữ liệu quá hạn
- [ ] Xuất penalty report với filter UNPAID/PAID
- [ ] Xuất popular-books với limit khác nhau
- [ ] Test date range validation (không quá 1 năm)
- [ ] Test empty data export

---

## Dependencies Mới

| Dependency | Version | Purpose |
|------------|---------|---------|
| apache-poi | 5.2.5 | Xuất file Excel .xlsx |

---

## Estimated Time

- Thêm dependency: 5 phút
- Tạo Export Service (CSV): 15 phút
- Tạo Export Service (Excel): 25 phút
- Cập nhật Controller: 15 phút
- Testing & Fixes: 15 phút

**Tổng:** ~75 phút (1 giờ 15 phút)

---

## Next Steps (Phase 8)

Sau khi hoàn thành Phase 7, các tính năng tiềm năng cho Phase 8:

1. **Admin Dashboard**: Trang dashboard tổng quan cho admin
2. **User Management**: Quản lý người dùng (activate/deactivate, reset password)
3. **System Configuration**: Cấu hình hệ thống qua UI
4. **Audit Logs**: Ghi log các hoạt động quan trọng
5. **Backup/Restore**: Sao lưu và khôi phục dữ liệu

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-16  
**Author:** Development Team