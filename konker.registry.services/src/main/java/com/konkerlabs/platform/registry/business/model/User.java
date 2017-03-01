package com.konkerlabs.platform.registry.business.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetails;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;


@Document(collection = "users")
@Data
@Builder
public class User implements URIDealer, UserDetails, ClientDetails {

	private static final long serialVersionUID = 1L;

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
    private boolean notificationViaEmail;

    @Tolerate
    public User() {

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

    //methods of ClientDetails OAuth2
	@Override
	public String getClientId() {
		return getEmail();
	}

	@Override
	public Set<String> getResourceIds() {
		return Collections.emptySet();
	}

	@Override
	public boolean isSecretRequired() {
		return true;
	}

	@Override
	public String getClientSecret() {
		return getPassword();
	}

	@Override
	public boolean isScoped() {
		return true;
	}

	@Override
	public Set<String> getScope() {
		Set<String> scopes = new HashSet<>();
		scopes.add("trust");
		scopes.add("read");
		scopes.add("write");
		return scopes;
	}

	@Override
	public Set<String> getAuthorizedGrantTypes() {
		Set<String> grantTypes = new HashSet<>();
		grantTypes.add("client_credentials");
		grantTypes.add("password");
		return grantTypes;
	}

	@Override
	public Set<String> getRegisteredRedirectUri() {
		return Collections.singleton("/");
	}

	@Override
	public Integer getAccessTokenValiditySeconds() {
		return 0;
	}

	@Override
	public Integer getRefreshTokenValiditySeconds() {
		return 0;
	}

	@Override
	public boolean isAutoApprove(String scope) {
		return true;
	}

	@Override
	public Map<String, Object> getAdditionalInformation() {
		return Collections.emptyMap();
	}
	//methods of ClientDetails OAuth2

}