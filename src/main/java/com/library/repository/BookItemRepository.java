package com.library.repository;

import com.library.model.BookItem;
import com.library.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookItemRepository extends JpaRepository<BookItem, String> {
    List<BookItem> findByIsbn(String isbn);
    List<BookItem> findByIsbnAndStatus(String isbn, BookStatus status);
    long countByStatus(BookStatus status);
}
