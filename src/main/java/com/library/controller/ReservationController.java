package com.library.controller;

import com.library.dto.ReservationRequest;
import com.library.model.Reservation;
import com.library.service.CirculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final CirculationService circulationService;

    public ReservationController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    /**
     * POST /api/reservations
     * Payload: { "memberId": "LIB-1001", "isbn": "978-1449373320" }
     */
    @PostMapping
    public ResponseEntity<Reservation> reserveBook(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = circulationService.reserveBook(request.getMemberId(), request.getIsbn());
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }
}
