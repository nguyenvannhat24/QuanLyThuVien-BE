package com.dev.borrow.repository;

import com.dev.borrow.model.Borrow;
import com.dev.borrow.model.BorrowStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
// thừa kế MongoRepository để có các phương thức CRUD cơ bản và các hàm được định nghĩa theo tên phương thức để truy vấn dữ liệu
public interface BorrowRepository extends MongoRepository<Borrow, String> {

    long countByUserIdAndStatus(String userId, BorrowStatus status);

    boolean existsByUserIdAndStatus(String userId, BorrowStatus status);

    List<Borrow> findByUserId(String userId);

    List<Borrow> findByStatus(BorrowStatus status);

    long countByBookIdAndStatus(String bookId, BorrowStatus status);

    long countByStatus(BorrowStatus status);



}