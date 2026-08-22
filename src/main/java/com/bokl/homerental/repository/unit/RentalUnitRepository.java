package com.bokl.homerental.repository.unit;

import com.bokl.homerental.entity.unit.RentalUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentalUnitRepository extends JpaRepository<RentalUnit, Long>, JpaSpecificationExecutor<RentalUnit> {

    List<RentalUnit> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Page<RentalUnit> findByOwnerId(Long ownerId, Pageable pageable);

    Page<RentalUnit> findByStatus(RentalUnit.Status status, Pageable pageable);

    @Query("""
            SELECT u FROM RentalUnit u
            JOIN u.governorate g
            JOIN u.area a
            WHERE u.status = com.bokl.homerental.entity.unit.RentalUnit.Status.ACTIVE
              AND (
                LOWER(g.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(g.nameAr) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(a.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(a.nameAr) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.streetName) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.title) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    Page<RentalUnit> searchByText(@Param("q") String q, Pageable pageable);
}
