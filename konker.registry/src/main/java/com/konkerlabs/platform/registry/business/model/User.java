package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Document(collection = "users")
@Data
@Builder
public class User implements UserDetails {

    @Id
    private String email;
    @DBRef
    private Tenant tenant;
    private String password;
    private String phone;
    private TimeZone zoneId = TimeZone.AMERICA_SAO_PAULO;
    private String name;
    private String avatar;
    private Language language = Language.PT_BR;
    private DateFormat dateFormat = DateFormat.YYYYMMDD;

    @Tolerate
    public User(){

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
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

    @Data
    @Builder
    @Document(collection = "passwordBlacklist")
    public static class PasswordBlacklist {
        @Id
        private String value;
    }
}