package com.bokl.homerental.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public UserRole() {}

    public UserRole(String roleName) {
        this.roleName = roleName;
    }

    public Long getId()                        { return id; }
    public String getRoleName()                { return roleName; }
    public void setRoleName(String roleName)   { this.roleName = roleName; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public Instant getCreatedAt()              { return createdAt; }
    public Instant getUpdatedAt()              { return updatedAt; }
}
