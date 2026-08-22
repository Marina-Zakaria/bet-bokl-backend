package com.bokl.homerental.repository;

import com.bokl.homerental.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GovernorateRepository extends JpaRepository<Governorate, Integer> {

    List<Governorate> findAllByOrderByNameEn();

    @Query("""
            SELECT g FROM Governorate g
            WHERE LOWER(g.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(g.nameAr) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY g.nameEn
            """)
    List<Governorate> searchByText(@Param("q") String q);
}
