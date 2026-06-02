package com.bokl.homerental.entity.notification;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "title_ar", length = 255)
    private String titleAr;

    @Column(name = "title_en", length = 255)
    private String titleEn;

    @Column(name = "body_ar", columnDefinition = "text")
    private String bodyAr;

    @Column(name = "body_en", columnDefinition = "text")
    private String bodyEn;

    @Column(columnDefinition = "json")
    private String data;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Notification() {
    }

    public Long getId() {
        return id;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getBodyAr() {
        return bodyAr;
    }

    public void setBodyAr(String bodyAr) {
        this.bodyAr = bodyAr;
    }

    public String getBodyEn() {
        return bodyEn;
    }

    public void setBodyEn(String bodyEn) {
        this.bodyEn = bodyEn;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
