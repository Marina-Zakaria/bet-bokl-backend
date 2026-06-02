package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.TermsDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsDefinitionRepository extends JpaRepository<TermsDefinition, Long> {

    Optional<TermsDefinition> findFirstByActiveTrue();
}
