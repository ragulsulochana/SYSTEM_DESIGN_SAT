package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class ReturnRequest {

    @NotBlank(message = "barcode cannot be blank")
    private String barcode;

    public ReturnRequest() {}

    public ReturnRequest(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
