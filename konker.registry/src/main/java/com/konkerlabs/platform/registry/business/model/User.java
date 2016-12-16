package com.konkerlabs.platform.registry.business.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Data;

@Document(collection = "users")
@Data
@Builder
public class User implements UserDetails {

    @Id
    private String email;
    @DBRef
    private Tenant tenant;
    private String password;
    private final String zoneId = "America/Sao_Paulo"; 
    
    @DBRef
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    	List<GrantedAuthority> authorities = new ArrayList<>();
    	roles.forEach(r -> r.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName()))));
        return authorities;//Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}