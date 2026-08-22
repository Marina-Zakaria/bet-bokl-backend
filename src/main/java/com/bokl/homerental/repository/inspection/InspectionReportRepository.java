package com.bokl.homerental.repository.inspection;

import com.bokl.homerental.entity.inspection.InspectionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface InspectionReportRepository extends JpaRepository<InspectionReport, Long> {

    Optional<InspectionReport> findByScheduleId(Long scheduleId);

    Optional<InspectionReport> findFirstByScheduleApplicationIdOrderByCreatedAtDesc(Long applicationId);

        @Query("""
            SELECT r FROM InspectionReport r
            WHERE (:inspectorId IS NULL OR r.inspector.id = :inspectorId)
              AND (:fromDate IS NULL OR r.createdAt >= :fromDate)
              AND (:toDate IS NULL OR r.createdAt <= :toDate)
            """)
        Page<InspectionReport> searchReports(
            @Param("inspectorId") Long inspectorId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
        );
}
