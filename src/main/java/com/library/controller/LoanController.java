package com.library.controller;

import com.library.dto.CheckoutRequest;
import com.library.dto.ReturnRequest;
import com.library.model.Loan;
import com.library.service.CirculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final CirculationService circulationService;

    public LoanController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    /**
     * POST /api/loans/checkout
     * Payload: { "memberId": "LIB-1001", "barcode": "BC-1001-A" }
     * Enforces optimistic locking via @Version on BookItem.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Loan> checkoutBook(@Valid @RequestBody CheckoutRequest request) {
        Loan loan = circulationService.checkout(request.getMemberId(), request.getBarcode());
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }

    /**
     * PUT /api/loans/return
     * Payload: { "barcode": "BC-1001-A" }
     */
    @PutMapping("/return")
    public ResponseEntity<Loan> returnBook(@Valid @RequestBody ReturnRequest request) {
        Loan loan = circulationService.returnBook(request.getBarcode());
        return ResponseEntity.ok(loan);
    }
}
