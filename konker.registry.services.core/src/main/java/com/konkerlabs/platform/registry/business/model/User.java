package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;


@Document(collection = "users")
@Data
@Builder
public class User implements URIDealer, UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
    private String email;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private Location location;
    private String password;

    private String phone;
    private TimeZone zoneId = TimeZone.AMERICA_SAO_PAULO;
    private String name;
    private String avatar;
    private Language language = Language.PT_BR;
    private DateFormat dateFormat = DateFormat.YYYYMMDD;
    private boolean notificationViaEmail;
    private Instant registrationDate;
    private JobEnum job;
    private boolean active;
    private boolean acceptedTerms;

    @Tolerate
    public User() {
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase();
    }

    @DBRef
    private List<Role> roles;

    public static final String URI_SCHEME = "user";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return tenant.getDomainName();
    }

    @Override
    public String getGuid() {
        return email;
    }


    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName())));
        roles.forEach(r -> r.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName()))));
        return authorities;
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

    public String getFirstName() {
    	if (StringUtils.isNotBlank(name)) {
	    	int spaceIndex = name.indexOf(' ');
	    	if (spaceIndex > 0) {
	    		return name.substring(0, spaceIndex);
	    	}
    	} else {
    		return email;
    	}

    	return name;
    }

	public enum JobEnum {
		CEO("model.user.job.ceo"),
		CIO("model.user.job.cio"),
		ENTERPRENUER("model.user.job.entrepreneur"),
		MARKETING("model.user.job.marketing"),
		SALES("model.user.job.sales"),
		STARTUP("model.user.job.startup"),
		OTHER("model.user.job.other");

		public String getCode() {
			return code;
		}

		private String code;

		JobEnum(String code) {
			this.code = code;
		}
	}

}
