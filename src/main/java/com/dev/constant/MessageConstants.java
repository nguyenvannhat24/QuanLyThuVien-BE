package com.dev.constant;

public class MessageConstants {
    
    // ============= Auth Messages =============
    public static final String REGISTER_SUCCESS = "Đăng ký tài khoản thành công";
    public static final String REGISTER_FAILED = "Đăng ký tài khoản thất bại";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String LOGIN_FAILED = "Tên đăng nhập hoặc mật khẩu không đúng";
    public static final String LOGOUT_SUCCESS = "Đăng xuất thành công";
    public static final String REFRESH_TOKEN_SUCCESS = "Làm mới token thành công";
    public static final String INVALID_TOKEN = "Token không hợp lệ";
    public static final String TOKEN_EXPIRED = "Token đã hết hạn";
    public static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
    public static final String USERNAME_EXISTS = "Tên đăng nhập đã tồn tại";
    public static final String EMAIL_EXISTS = "Email đã được sử dụng";
    
    // ============= Book Messages =============
    public static final String BOOK_CREATED = "Tạo sách thành công";
    public static final String BOOK_UPDATED = "Cập nhật sách thành công";
    public static final String BOOK_DELETED = "Xóa sách thành công";
    public static final String BOOK_NOT_FOUND = "Không tìm thấy sách";
    public static final String BOOK_COPY_NOT_FOUND = "Không tìm thấy bản sao sách";
    public static final String NO_AVAILABLE_COPIES = "Không còn bản sao sách nào";
    
    // ============= Borrow Messages =============
    public static final String BORROW_SUCCESS = "Mượn sách thành công";
    public static final String BORROW_FAILED = "Mượn sách thất bại";
    public static final String RETURN_SUCCESS = "Trả sách thành công";
    public static final String RETURN_FAILED = "Trả sách thất bại";
    public static final String RENEW_SUCCESS = "Gia hạn sách thành công";
    public static final String RENEW_FAILED = "Gia hạn sách thất bại";
    public static final String BORROW_NOT_FOUND = "Không tìm thấy phiếu mượn";
    public static final String ALREADY_BORROWED = "Bạn đang mượn sách này";
    public static final String MAX_BORROW_LIMIT_REACHED = "Bạn đã mượn tối đa số sách cho phép";
    public static final String BORROW_EXPIRED = "Phiếu mượn đã hết hạn";
    
    // ============= Reservation Messages =============
    public static final String RESERVATION_SUCCESS = "Đặt chỗ thành công";
    public static final String RESERVATION_CANCELLED = "Hủy đặt chỗ thành công";
    public static final String RESERVATION_FAILED = "Đặt chỗ thất bại";
    public static final String RESERVATION_NOT_FOUND = "Không tìm thấy đặt chỗ";
    public static final String ALREADY_RESERVED = "Bạn đã đặt chỗ sách này";
    public static final String RESERVATION_EXPIRED = "Đặt chỗ đã hết hạn";
    public static final String RESERVATION_QUEUE_FULL = "Hàng đợi đặt chỗ đã đầy";
    
    // ============= Penalty Messages =============
    public static final String PENALTY_CREATED = "Tạo phiếu phạt thành công";
    public static final String PENALTY_PAID = "Thanh toán phạt thành công";
    public static final String PENALTY_NOT_FOUND = "Không tìm thấy phiếu phạt";
    public static final String PENALTY_ALREADY_PAID = "Phiếu phạt đã thanh toán";
    public static final String OUTSTANDING_PENALTY = "Bạn có phiếu phạt chưa thanh toán";
    
    // ============= Category/Author/Publisher Messages =============
    public static final String CATEGORY_CREATED = "Tạo thể loại thành công";
    public static final String CATEGORY_UPDATED = "Cập nhật thể loại thành công";
    public static final String CATEGORY_DELETED = "Xóa thể loại thành công";
    public static final String AUTHOR_CREATED = "Tạo tác giả thành công";
    public static final String AUTHOR_UPDATED = "Cập nhật tác giả thành công";
    public static final String AUTHOR_DELETED = "Xóa tác giả thành công";
    public static final String PUBLISHER_CREATED = "Tạo nhà xuất bản thành công";
    public static final String PUBLISHER_UPDATED = "Cập nhật nhà xuất bản thành công";
    public static final String PUBLISHER_DELETED = "Xóa nhà xuất bản thành công";
    
    // ============= General Messages =============
    public static final String OPERATION_SUCCESS = "Thao tác thành công";
    public static final String OPERATION_FAILED = "Thao tác thất bại";
    public static final String VALIDATION_ERROR = "Dữ liệu không hợp lệ";
    public static final String UNAUTHORIZED = "Vui lòng đăng nhập";
    public static final String FORBIDDEN = "Bạn không có quyền thực hiện thao tác này";
    public static final String INTERNAL_ERROR = "Đã xảy ra lỗi hệ thống";
}
