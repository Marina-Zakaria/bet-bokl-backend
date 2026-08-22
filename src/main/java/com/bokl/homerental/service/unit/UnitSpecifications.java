package com.bokl.homerental.service.unit;

import com.bokl.homerental.entity.unit.Booking;
import com.bokl.homerental.entity.unit.RentalUnit;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class UnitSpecifications {

    private UnitSpecifications() {
    }

    public static Specification<RentalUnit> withFilters(
            String q,
            BigDecimal minRent,
            BigDecimal maxRent,
            RentalUnit.Category category,
            RentalUnit.Furnishing furnishing,
            Integer roomsCount,
            Boolean hasElevator,
            Boolean hasWashingMachine,
            Boolean hasWifi,
            Boolean hasAirConditioning,
            Boolean hasParking,
            Boolean hasPool,
            Boolean hasTv,
            Boolean hasKitchen,
            Boolean hasBalcony,
            Boolean hasWaterHeater,
            Boolean verified,
            Integer governorateId,
            Integer areaId,
            Integer adultsCount,
            Integer childrenCount,
            LocalDate availableFrom,
            LocalDate availableTo) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), RentalUnit.Status.ACTIVE));

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("streetName")), pattern),
                        cb.like(cb.lower(root.get("governorate").get("nameEn")), pattern),
                        cb.like(cb.lower(root.get("governorate").get("nameAr")), pattern),
                        cb.like(cb.lower(root.get("area").get("nameEn")), pattern),
                        cb.like(cb.lower(root.get("area").get("nameAr")), pattern)
                ));
            }

            if (minRent != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rentPerDay"), minRent));
            }
            if (maxRent != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rentPerDay"), maxRent));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (furnishing != null) {
                predicates.add(cb.equal(root.get("furnishing"), furnishing));
            }
            if (roomsCount != null) {
                predicates.add(cb.equal(root.get("roomsCount"), roomsCount));
            }
            if (governorateId != null) {
                predicates.add(cb.equal(root.get("governorate").get("id"), governorateId));
            }
            if (areaId != null) {
                predicates.add(cb.equal(root.get("area").get("id"), areaId));
            }
            if (verified != null) {
                predicates.add(cb.equal(root.get("verified"), verified));
            }
            if (adultsCount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxAdults"), adultsCount));
            }
            if (childrenCount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxChildren"), childrenCount));
            }
            addAmenity(predicates, cb, root, "hasElevator", hasElevator);
            addAmenity(predicates, cb, root, "hasWashingMachine", hasWashingMachine);
            addAmenity(predicates, cb, root, "hasWifi", hasWifi);
            addAmenity(predicates, cb, root, "hasAirConditioning", hasAirConditioning);
            addAmenity(predicates, cb, root, "hasParking", hasParking);
            addAmenity(predicates, cb, root, "hasPool", hasPool);
            addAmenity(predicates, cb, root, "hasTv", hasTv);
            addAmenity(predicates, cb, root, "hasKitchen", hasKitchen);
            addAmenity(predicates, cb, root, "hasBalcony", hasBalcony);
            addAmenity(predicates, cb, root, "hasWaterHeater", hasWaterHeater);

            if (availableFrom != null && availableTo != null) {
                // Exclude units with owner unavailability overlapping the range
                Subquery<Long> unavailSq = query.subquery(Long.class);
                Root<?> unavail = unavailSq.from(com.bokl.homerental.entity.unit.UnitUnavailability.class);
                unavailSq.select(unavail.get("unit").get("id"));
                unavailSq.where(
                        cb.equal(unavail.get("unit").get("id"), root.get("id")),
                        // Search leave date is exclusive.
                        cb.lessThan(unavail.get("startDate"), availableTo),
                        cb.greaterThanOrEqualTo(unavail.get("endDate"), availableFrom)
                );
                predicates.add(cb.not(cb.exists(unavailSq)));

                // Exclude units with active bookings overlapping the range
                Subquery<Long> bookingSq = query.subquery(Long.class);
                Root<?> booking = bookingSq.from(Booking.class);
                bookingSq.select(booking.get("unit").get("id"));
                bookingSq.where(
                        cb.equal(booking.get("unit").get("id"), root.get("id")),
                        booking.get("status").in(
                                Booking.Status.PENDING_OWNER_APPROVAL,
                                Booking.Status.PENDING_PAYMENT,
                                Booking.Status.PAID,
                                Booking.Status.CHECKED_IN,
                                Booking.Status.CHECKED_OUT,
                                Booking.Status.COMPLETED
                        ),
                        cb.lessThan(booking.get("checkInDate"), availableTo),
                        cb.greaterThan(booking.get("checkOutDate"), availableFrom)
                );
                predicates.add(cb.not(cb.exists(bookingSq)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addAmenity(List<Predicate> predicates,
                                   jakarta.persistence.criteria.CriteriaBuilder cb,
                                   Root<RentalUnit> root,
                                   String field,
                                   Boolean value) {
        if (Boolean.TRUE.equals(value)) {
            predicates.add(cb.isTrue(root.get(field)));
        }
    }
}
