package com.library.repository;

import com.library.model.Loan;
import com.library.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findFirstByBarcodeAndStatus(String barcode, LoanStatus status);
    List<Loan> findByMemberIdAndStatus(String memberId, LoanStatus status);
    long countByStatus(LoanStatus status);
}
