package com.bokl.homerental.repository;

import com.bokl.homerental.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {

    List<Area> findAllByGovernorateIdOrderByNameEn(Integer governorateId);
}
