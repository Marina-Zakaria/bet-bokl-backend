package com.bokl.homerental.repository;

import com.bokl.homerental.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GovernorateRepository extends JpaRepository<Governorate, Integer> {

    List<Governorate> findAllByOrderByNameEn();
}
