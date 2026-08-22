package com.bokl.homerental.repository;

import com.bokl.homerental.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {

    List<Area> findAllByGovernorateIdOrderByNameEn(Integer governorateId);

    @Query("""
            SELECT a FROM Area a
            WHERE LOWER(a.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.nameAr) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY a.nameEn
            """)
    List<Area> searchByText(@Param("q") String q);
}
