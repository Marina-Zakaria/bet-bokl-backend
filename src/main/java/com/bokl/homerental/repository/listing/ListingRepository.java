package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    Optional<Listing> findByApplicationId(Long applicationId);
}
