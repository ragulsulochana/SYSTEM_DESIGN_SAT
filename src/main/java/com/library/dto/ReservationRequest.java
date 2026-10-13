package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class ReservationRequest {

    @NotBlank(message = "memberId cannot be blank")
    private String memberId;

    @NotBlank(message = "isbn cannot be blank")
    private String isbn;

    public ReservationRequest() {}

    public ReservationRequest(String memberId, String isbn) {
        this.memberId = memberId;
        this.isbn = isbn;
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
}
