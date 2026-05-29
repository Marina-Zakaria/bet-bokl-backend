package com.bokl.homerental.repository;

import com.bokl.homerental.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findByPhone(String phone);

    /** Searches username first, then falls back to phone — used for login. */
    @Query("SELECT u FROM AuthUser u WHERE u.username = :identifier OR u.phone = :identifier")
    Optional<AuthUser> findByUsernameOrPhone(String identifier);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    Optional<AuthUser> findByRegistrationToken(String tokenHash);
}
