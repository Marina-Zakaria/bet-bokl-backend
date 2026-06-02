package com.bokl.homerental.repository.inspection;

import com.bokl.homerental.entity.inspection.InspectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionScheduleRepository extends JpaRepository<InspectionSchedule, Long> {

    List<InspectionSchedule> findByApplicationIdOrderBySelectionOrder(Long applicationId);
}
