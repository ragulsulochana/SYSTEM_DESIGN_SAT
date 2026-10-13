package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservations_isbn", columnList = "isbn"),
    @Index(name = "idx_reservations_member", columnList = "member_id")
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Member ID is required")
    @Column(name = "member_id", length = 64, nullable = false)
    private String memberId;

    @NotBlank(message = "ISBN reference is required")
    @Column(name = "isbn", length = 32, nullable = false)
    private String isbn;

    @NotNull
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    public Reservation() {}

    public Reservation(String memberId, String isbn, LocalDateTime date, ReservationStatus status) {
        this.memberId = memberId;
        this.isbn = isbn;
        this.date = date;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
