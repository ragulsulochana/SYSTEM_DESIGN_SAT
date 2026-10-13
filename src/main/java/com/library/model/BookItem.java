package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "book_items", indexes = {
    @Index(name = "idx_book_items_isbn", columnList = "isbn"),
    @Index(name = "idx_book_items_status", columnList = "status")
})
public class BookItem {

    @Id
    @Column(name = "barcode", length = 64, nullable = false)
    private String barcode;

    @NotBlank(message = "ISBN reference is required")
    @Column(name = "isbn", length = 32, nullable = false)
    private String isbn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;

    /**
     * Optimistic Locking Version Attribute:
     * Guarantees race-condition prevention across concurrent checkout calls.
     * When two transactions read version V and attempt checkout concurrently,
     * the second commit encounters an OptimisticLockingFailureException.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "shelf_location", length = 128)
    private String shelfLocation;

    public BookItem() {}

    public BookItem(String barcode, String isbn, BookStatus status, String shelfLocation) {
        this.barcode = barcode;
        this.isbn = isbn;
        this.status = status;
        this.shelfLocation = shelfLocation;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }
}
