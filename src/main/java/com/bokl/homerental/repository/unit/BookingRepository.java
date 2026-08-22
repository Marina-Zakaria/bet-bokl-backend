package com.bokl.homerental.repository.unit;

import com.bokl.homerental.entity.unit.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuestIdOrderByCreatedAtDesc(Long guestId);

    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    List<Booking> findByUnitOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Page<Booking> findByUnitOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByUnitIdAndStatusIn(Long unitId, Collection<Booking.Status> statuses);

    List<Booking> findByStatusAndApprovalExpiresAtLessThanEqual(
            Booking.Status status, Instant expiresAt);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.unit.id = :unitId
              AND b.status IN :statuses
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    boolean existsOverlappingActive(@Param("unitId") Long unitId,
                                    @Param("checkIn") LocalDate checkIn,
                                    @Param("checkOut") LocalDate checkOut,
                                    @Param("statuses") Collection<Booking.Status> statuses);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.unit.id = :unitId
              AND b.status IN :statuses
              AND b.checkInDate < :to
              AND b.checkOutDate > :from
            ORDER BY b.checkInDate
            """)
    List<Booking> findOverlapping(@Param("unitId") Long unitId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  @Param("statuses") Collection<Booking.Status> statuses);
}
