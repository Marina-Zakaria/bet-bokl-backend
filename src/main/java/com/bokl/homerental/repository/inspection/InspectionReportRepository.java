package com.bokl.homerental.repository.inspection;

import com.bokl.homerental.entity.inspection.InspectionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InspectionReportRepository extends JpaRepository<InspectionReport, Long> {

    Optional<InspectionReport> findByScheduleId(Long scheduleId);

    Optional<InspectionReport> findFirstByScheduleApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
