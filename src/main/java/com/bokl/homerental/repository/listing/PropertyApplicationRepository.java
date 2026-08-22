package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.PropertyApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyApplicationRepository extends JpaRepository<PropertyApplication, Long> {

    Optional<PropertyApplication> findByIdAndUserId(Long id, Long userId);

    Page<PropertyApplication> findByUserId(Long userId, Pageable pageable);

    Page<PropertyApplication> findByUserIdAndStatus(Long userId, PropertyApplication.Status status, Pageable pageable);

    Page<PropertyApplication> findByStatus(PropertyApplication.Status status, Pageable pageable);

    Page<PropertyApplication> findByInspectorId(Long inspectorId, Pageable pageable);

    Page<PropertyApplication> findByInspectorIdAndStatus(Long inspectorId, PropertyApplication.Status status, Pageable pageable);

    Optional<PropertyApplication> findByIdAndInspectorId(Long id, Long inspectorId);
}
