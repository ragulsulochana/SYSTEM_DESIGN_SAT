package com.library.service;

import com.library.dto.CatalogBookResponse;
import com.library.exception.BadRequestException;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CirculationService {

    private final BookTitleRepository bookTitleRepository;
    private final BookItemRepository bookItemRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public CirculationService(
            BookTitleRepository bookTitleRepository,
            BookItemRepository bookItemRepository,
            MemberRepository memberRepository,
            LoanRepository loanRepository,
            ReservationRepository reservationRepository) {
        this.bookTitleRepository = bookTitleRepository;
        this.bookItemRepository = bookItemRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Search catalog titles by title or author, including all copies and active statuses
     */
    @Transactional(readOnly = true)
    public List<CatalogBookResponse> searchCatalog(String query) {
        List<BookTitle> titles;
        if (query == null || query.trim().isEmpty()) {
            titles = bookTitleRepository.findAll();
        } else {
            titles = bookTitleRepository.searchByTitleOrAuthorOrIsbn(query.trim());
        }

        List<CatalogBookResponse> results = new ArrayList<>();
        for (BookTitle title : titles) {
            List<BookItem> items = bookItemRepository.findByIsbn(title.getIsbn());
            results.add(new CatalogBookResponse(title, items));
        }
        return results;
    }

    /**
     * Checkout a physical copy:
     * Crucial Requirement: Uses Optimistic Locking on BookItem via @Version attribute.
     * When two concurrent transactions attempt to checkout the same copy (barcode) simultaneously,
     * the second commit encounters a version mismatch and triggers an OptimisticLockingFailureException,
     * mapped cleanly to 409 Conflict.
     */
    public Loan checkout(String memberId, String barcode) {
        // 1. Validate Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID '" + memberId + "' not found"));

        if (member.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Member account is " + member.getAccountStatus() + ". Borrowing privileges suspended.");
        }

        // 2. Fetch BookItem copy
        BookItem item = bookItemRepository.findById(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy with barcode '" + barcode + "' not found"));

        // 3. Check current availability
        if (item.getStatus() == BookStatus.LOANED) {
            throw new ConflictException("Book copy '" + barcode + "' is currently checked out by another patron.");
        }
        if (item.getStatus() == BookStatus.LOST) {
            throw new ConflictException("Book copy '" + barcode + "' is marked as lost in catalog.");
        }

        // 4. Update status to LOANED (Optimistic Locking @Version will be verified upon commit)
        item.setStatus(BookStatus.LOANED);
        bookItemRepository.save(item);

        // 5. Create new Loan record (21-day loan period)
        LocalDateTime issueDate = LocalDateTime.now();
        LocalDateTime dueDate = issueDate.plusDays(21);
        Loan loan = new Loan(memberId, barcode, issueDate, dueDate, LoanStatus.ACTIVE);
        Loan savedLoan = loanRepository.save(loan);

        // 6. Increment member loan counter
        member.setTotalCheckedOut(member.getTotalCheckedOut() + 1);
        memberRepository.save(member);

        return savedLoan;
    }

    /**
     * Return a checked-out book item by barcode
     */
    public Loan returnBook(String barcode) {
        // 1. Fetch BookItem copy
        BookItem item = bookItemRepository.findById(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy with barcode '" + barcode + "' not found"));

        // 2. Find active loan
        Loan loan = loanRepository.findFirstByBarcodeAndStatus(barcode, LoanStatus.ACTIVE)
                .orElseThrow(() -> new ConflictException("No active loan record found for barcode '" + barcode + "'"));

        // 3. Mark loan returned
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());
        Loan updatedLoan = loanRepository.save(loan);

        // 4. Decrement member totalCheckedOut
        memberRepository.findById(loan.getMemberId()).ifPresent(member -> {
            if (member.getTotalCheckedOut() > 0) {
                member.setTotalCheckedOut(member.getTotalCheckedOut() - 1);
                memberRepository.save(member);
            }
        });

        // 5. Check if there are pending reservations for this ISBN
        List<Reservation> pendingReservations = reservationRepository.findByIsbnAndStatusOrderByDateAsc(item.getIsbn(), ReservationStatus.PENDING);
        if (!pendingReservations.isEmpty()) {
            // Reserve for next patron in queue
            item.setStatus(BookStatus.RESERVED);
        } else {
            // Return to available stack
            item.setStatus(BookStatus.AVAILABLE);
        }
        bookItemRepository.save(item);

        return updatedLoan;
    }

    /**
     * Reserve / Hold a book title by ISBN
     */
    public Reservation reserveBook(String memberId, String isbn) {
        // 1. Validate Member
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID '" + memberId + "' not found"));

        // 2. Validate BookTitle
        bookTitleRepository.findById(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ISBN '" + isbn + "' not found"));

        // 3. Check for existing active reservation by this member
        if (reservationRepository.existsByMemberIdAndIsbnAndStatus(memberId, isbn, ReservationStatus.PENDING)) {
            throw new ConflictException("You already have an active hold on this title.");
        }

        // 4. Create and save reservation
        Reservation reservation = new Reservation(memberId, isbn, LocalDateTime.now(), ReservationStatus.PENDING);
        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. If any copy is AVAILABLE, hold it
        List<BookItem> availItems = bookItemRepository.findByIsbnAndStatus(isbn, BookStatus.AVAILABLE);
        if (!availItems.isEmpty()) {
            BookItem firstAvail = availItems.get(0);
            firstAvail.setStatus(BookStatus.RESERVED);
            bookItemRepository.save(firstAvail);
        }

        return savedReservation;
    }
}
