package com.library.controller;

import com.library.dto.CatalogBookResponse;
import com.library.service.CirculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
public class CatalogController {

    private final CirculationService circulationService;

    public CatalogController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    /**
     * GET /api/catalog/search?query={text}
     * Real-time search by title, author, or ISBN.
     */
    @GetMapping("/search")
    public ResponseEntity<List<CatalogBookResponse>> searchCatalog(
            @RequestParam(value = "query", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(circulationService.searchCatalog(query));
    }

    /**
     * GET /api/catalog/books - Returns all books
     */
    @GetMapping("/books")
    public ResponseEntity<List<CatalogBookResponse>> getAllBooks() {
        return ResponseEntity.ok(circulationService.searchCatalog(""));
    }
}
