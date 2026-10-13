package com.library.repository;

import com.library.model.BookTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookTitleRepository extends JpaRepository<BookTitle, String> {

    @Query("SELECT b FROM BookTitle b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "b.isbn LIKE CONCAT('%', :query, '%')")
    List<BookTitle> searchByTitleOrAuthorOrIsbn(@Param("query") String query);
}
