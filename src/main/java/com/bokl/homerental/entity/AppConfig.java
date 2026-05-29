package com.bokl.homerental.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Column is named config_key to avoid the reserved SQL keyword 'key'. */
    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 255)
    private String value;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public AppConfig() {}

    public Long getId()                        { return id; }
    public String getKey()                     { return key; }
    public void setKey(String key)             { this.key = key; }
    public String getValue()                   { return value; }
    public void setValue(String value)         { this.value = value; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public Instant getCreatedAt()              { return createdAt; }
    public Instant getUpdatedAt()              { return updatedAt; }
}
