package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.PropertyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyApplicationRepository extends JpaRepository<PropertyApplication, Long> {

    Optional<PropertyApplication> findByIdAndUserId(Long id, Long userId);
}
