package com.dev.book.model;

/**
 * BookCopyStatus enum defines the possible states of a physical book copy.
 * Nghĩa vụ: Trạng thái của bản sao vật lý
 */
public enum BookCopyStatus {
    /**
     * AVAILABLE: Sẵn sàng để cho mượn
     */
    AVAILABLE,
    
    /**
     * BORROWED: Đang được mượn
     */
    BORROWED,
    
    /**
     * DAMAGED: Bị hư hỏng
     */
    DAMAGED,
    
    /**
     * LOST: Bị mất
     */
    LOST
}
