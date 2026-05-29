package com.bokl.homerental.security;

import com.bokl.homerental.entity.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Wraps {@link AuthUser} as a Spring Security {@link UserDetails}.
 * Kept separate from the entity so the entity has no framework dependency.
 */
public class UserDetailsImpl implements UserDetails {

    private final AuthUser user;

    public UserDetailsImpl(AuthUser user) {
        this.user = user;
    }

    /** Expose the underlying entity for callers that need domain fields. */
    public AuthUser getAuthUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override public String getPassword()   { return user.getHashedPassword(); }
    @Override public String getUsername()   { return user.getUsername(); }

    @Override
    public boolean isAccountNonLocked() {
        Instant lockedUntil = user.getAccountLockedUntil();
        return lockedUntil == null || Instant.now().isAfter(lockedUntil);
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Account must be both active and verified to be usable. */
    @Override
    public boolean isEnabled() {
        return user.isActive() && user.isVerified();
    }
}
