package com.bokl.homerental.controller.dto.unit;

import com.bokl.homerental.entity.unit.Booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class BookingResponse {

    private Long id;
    private Long unitId;
    private BookingUnitSummaryDto unit;
    private OwnerSummaryDto guest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer adultsCount;
    private Integer childrenCount;
    private BigDecimal totalAmount;
    private Booking.Status status;
    private boolean guestReviewed;
    private boolean ownerReviewed;
    private Instant paidAt;
    private Instant checkedInAt;
    private Instant checkedOutAt;
    private Instant approvalExpiresAt;
    private Instant ownerDecidedAt;
    private String ownerRejectionReason;
    private Instant createdAt;

    // Flat aliases
    private String unitTitle;
    private String unitPhoto;
    private String unitLocation;
    private BigDecimal rentPerDay;
    private Long guestId;
    private String guestName;
    private Long ownerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public BookingUnitSummaryDto getUnit() { return unit; }
    public void setUnit(BookingUnitSummaryDto unit) { this.unit = unit; }

    public OwnerSummaryDto getGuest() { return guest; }
    public void setGuest(OwnerSummaryDto guest) { this.guest = guest; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getAdultsCount() { return adultsCount; }
    public void setAdultsCount(Integer adultsCount) { this.adultsCount = adultsCount; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Booking.Status getStatus() { return status; }
    public void setStatus(Booking.Status status) { this.status = status; }

    public boolean isGuestReviewed() { return guestReviewed; }
    public void setGuestReviewed(boolean guestReviewed) { this.guestReviewed = guestReviewed; }

    public boolean isOwnerReviewed() { return ownerReviewed; }
    public void setOwnerReviewed(boolean ownerReviewed) { this.ownerReviewed = ownerReviewed; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }

    public Instant getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(Instant checkedOutAt) { this.checkedOutAt = checkedOutAt; }
    public Instant getApprovalExpiresAt() { return approvalExpiresAt; }
    public void setApprovalExpiresAt(Instant approvalExpiresAt) { this.approvalExpiresAt = approvalExpiresAt; }
    public Instant getOwnerDecidedAt() { return ownerDecidedAt; }
    public void setOwnerDecidedAt(Instant ownerDecidedAt) { this.ownerDecidedAt = ownerDecidedAt; }
    public String getOwnerRejectionReason() { return ownerRejectionReason; }
    public void setOwnerRejectionReason(String ownerRejectionReason) {
        this.ownerRejectionReason = ownerRejectionReason;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getUnitTitle() { return unitTitle; }
    public void setUnitTitle(String unitTitle) { this.unitTitle = unitTitle; }

    public String getUnitPhoto() { return unitPhoto; }
    public void setUnitPhoto(String unitPhoto) { this.unitPhoto = unitPhoto; }

    public String getUnitLocation() { return unitLocation; }
    public void setUnitLocation(String unitLocation) { this.unitLocation = unitLocation; }

    public BigDecimal getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(BigDecimal rentPerDay) { this.rentPerDay = rentPerDay; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public static class BookingUnitSummaryDto {
        private Long id;
        private String title;
        private BigDecimal rentPerDay;
        private Integer maxAdults;
        private Integer maxChildren;
        private List<String> photos;
        private NamedLocationDto governorate;
        private NamedLocationDto area;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public BigDecimal getRentPerDay() { return rentPerDay; }
        public void setRentPerDay(BigDecimal rentPerDay) { this.rentPerDay = rentPerDay; }

        public Integer getMaxAdults() { return maxAdults; }
        public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

        public Integer getMaxChildren() { return maxChildren; }
        public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

        public List<String> getPhotos() { return photos; }
        public void setPhotos(List<String> photos) { this.photos = photos; }

        public NamedLocationDto getGovernorate() { return governorate; }
        public void setGovernorate(NamedLocationDto governorate) { this.governorate = governorate; }

        public NamedLocationDto getArea() { return area; }
        public void setArea(NamedLocationDto area) { this.area = area; }
    }
}
