package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.AdminDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminDecisionRepository extends JpaRepository<AdminDecision, Long> {

    List<AdminDecision> findByApplicationId(Long applicationId);
}
