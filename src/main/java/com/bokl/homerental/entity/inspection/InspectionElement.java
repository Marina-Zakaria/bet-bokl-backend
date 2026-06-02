package com.bokl.homerental.entity.inspection;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inspection_elements")
public class InspectionElement {

    public enum ElementType {
        BOOLEAN,
        STRING,
        NUMBER,
        PHOTO_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "element_key", nullable = false, unique = true, length = 255)
    private String elementKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false, length = 50)
    private ElementType elementType;

    @Column(name = "is_required", nullable = false)
    private Boolean required = true;

    @Column(name = "label_ar", nullable = false, length = 255)
    private String labelAr;

    @Column(name = "label_en", nullable = false, length = 255)
    private String labelEn;

    @Column(length = 255)
    private String placeholder;

    @Column(name = "min_value", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InspectionElement() {
    }

    public Long getId() {
        return id;
    }

    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String elementKey) {
        this.elementKey = elementKey;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getLabelAr() {
        return labelAr;
    }

    public void setLabelAr(String labelAr) {
        this.labelAr = labelAr;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
