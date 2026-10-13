package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {

    @NotBlank(message = "memberId cannot be blank")
    private String memberId;

    @NotBlank(message = "barcode cannot be blank")
    private String barcode;

    public CheckoutRequest() {}

    public CheckoutRequest(String memberId, String barcode) {
        this.memberId = memberId;
        this.barcode = barcode;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
