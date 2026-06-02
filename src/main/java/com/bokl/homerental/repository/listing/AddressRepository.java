package com.bokl.homerental.repository.listing;

import com.bokl.homerental.entity.listing.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
