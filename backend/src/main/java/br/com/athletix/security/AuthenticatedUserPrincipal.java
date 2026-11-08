package br.com.athletix.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AuthenticatedUserPrincipal(Long usuarioId, String email, String passwordHash, boolean active)
        implements UserDetails {
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return active; }
}
