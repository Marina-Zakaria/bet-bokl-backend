package com.bokl.homerental.repository.unit;

import com.bokl.homerental.entity.unit.UnitUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UnitUnavailabilityRepository extends JpaRepository<UnitUnavailability, Long> {

    List<UnitUnavailability> findByUnitIdOrderByStartDateAsc(Long unitId);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UnitUnavailability u
            WHERE u.unit.id = :unitId
              AND u.startDate <= :endDate
              AND u.endDate >= :startDate
            """)
    boolean existsOverlapping(@Param("unitId") Long unitId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT u FROM UnitUnavailability u
            WHERE u.unit.id = :unitId
              AND u.startDate <= :to
              AND u.endDate >= :from
            ORDER BY u.startDate
            """)
    List<UnitUnavailability> findOverlapping(@Param("unitId") Long unitId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);
}
