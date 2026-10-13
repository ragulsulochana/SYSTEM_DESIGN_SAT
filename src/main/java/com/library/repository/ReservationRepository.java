package com.library.repository;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByIsbnAndStatusOrderByDateAsc(String isbn, ReservationStatus status);
    List<Reservation> findByMemberIdAndStatus(String memberId, ReservationStatus status);
    boolean existsByMemberIdAndIsbnAndStatus(String memberId, String isbn, ReservationStatus status);
    long countByStatus(ReservationStatus status);
}
