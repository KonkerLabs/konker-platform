package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import lombok.Builder;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Builder
@Document(collection = "oauthClientDetails")
public class OauthClientDetails implements Serializable, ClientDetails {

    @Id
    private String clientId;
    @CreatedDate
    private Instant createTime = Instant.now();
    @Version
    private Long version;
    private boolean archived = false;
    private Set<String> resourceIds = Collections.emptySet();
    private String clientSecret;
    private String scope;
    private String authorizedGrantTypes = "authorization_code,refresh_token,client_credentials";
    private String webServerRedirectUri;
    private String authorities;
    private Language language = Language.PT_BR;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private Map<String, Object> additionalInformation = Collections.emptyMap();
    private boolean trusted = false;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private Boolean active;
    @DBRef
    private List<Role> roles;
    @DBRef
    private User parentUser;

    public String getName(){
        return clientId;
    }

    public void setName(String val){
        this.clientId = val;
    }

    @Tolerate
    public OauthClientDetails() {
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    public String getWebServerRedirectUri() {
        return webServerRedirectUri;
    }

    public void setWebServerRedirectUri(String webServerRedirectUri) {
        this.webServerRedirectUri = webServerRedirectUri;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public Integer getAccessTokenValidity() {
        return accessTokenValidity;
    }

    public void setAccessTokenValidity(Integer accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }

    public Integer getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public void setAdditionalInformation(Map<String, Object> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public User getParentUser() {
        return parentUser;
    }

    public void setParentUser(User parentUser) {
        this.parentUser = parentUser;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OauthClientDetails");
        sb.append("{createTime=").append(createTime);
        sb.append(", archived=").append(archived);
        sb.append(", clientId='").append(clientId).append('\'');
        sb.append(", resourceIds='").append(resourceIds).append('\'');
        sb.append(", clientSecret='").append(clientSecret).append('\'');
        sb.append(", scope='").append(scope).append('\'');
        sb.append(", authorizedGrantTypes='").append(authorizedGrantTypes).append('\'');
        sb.append(", webServerRedirectUri='").append(webServerRedirectUri).append('\'');
        sb.append(", authorities='").append(authorities).append('\'');
        sb.append(", accessTokenValidity=").append(accessTokenValidity);
        sb.append(", refreshTokenValidity=").append(refreshTokenValidity);
        sb.append(", additionalInformation='").append(additionalInformation).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", trusted=").append(trusted);
        sb.append('}');
        return sb.toString();
    }

    public ClientDetails toClientDetails() {
        BaseClientDetails clientDetails =
                new BaseClientDetails(
                        getClientId(),
                        getResourceIdsAsString(),
                        getScopeAsString(),
                        getAuthorizedGrantTypesAsString(),
                        getAuthoritiesAsString(),
                        getWebServerRedirectUri());
        clientDetails.setClientSecret(clientSecret);
        clientDetails.setAdditionalInformation(additionalInformation);
        clientDetails.setAccessTokenValiditySeconds(accessTokenValidity);
        clientDetails.setRefreshTokenValiditySeconds(refreshTokenValidity);

        return clientDetails;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public Set<String> getResourceIds() {
        return this.resourceIds;
    }

    @Override
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public Set<String> getScope() {
        Set<String> scopes = new HashSet<>();
        scopes.add("trust");
        scopes.add("read");
        scopes.add("write");
        return scopes;
    }

    public String getResourceIdsAsString() {
        StringBuffer str = new StringBuffer();
        for(String item : getResourceIds()){
            str.append(item);
            str.append(",");
        }
        return str.toString();
    }

    public String getScopeAsString() {
        StringBuffer str = new StringBuffer();
        for(String item : getScope()){
            str.append(item);
            str.append(",");
        }
        return "trust,read,write";
        //return str.toString();
    }

    public String getAuthorizedGrantTypesAsString(){
        StringBuffer str = new StringBuffer();
        for(String item : getAuthorizedGrantTypes()){
            str.append(item);
            str.append(",");
        }
        return str.toString();
    }

    public String getRegisteredUriAsString(){
        StringBuffer str = new StringBuffer();
        for(String item : getRegisteredRedirectUri()){
            str.append(item);
            str.append(",");
        }
        return str.toString();
    }

    public String getAuthoritiesAsString(){
        StringBuffer str = new StringBuffer();
        for(GrantedAuthority item : getAuthorities()){
            str.append(item.getAuthority());
            str.append(",");
        }
        return str.toString();
    }

    public Language getLanguage() {
        if (parentUser != null) {
            return parentUser.getLanguage();
        } else {
            return Language.EN;
        }
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        Set<String> grantTypes = new HashSet<>();
        grantTypes.add("client_credentials");
        return grantTypes;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        Set<String> uris = new HashSet<>();
        uris.add(webServerRedirectUri);
        return uris;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName())));
        roles.forEach(r -> r.getPrivileges().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName()))));
        return authorities;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String s) {
        return false;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }

    public OauthClientDetails setUserProperties(User user) {
        this.setParentUser(user);
        this.setTenant(user.getTenant());
        this.setClientId(user.getEmail());
        this.setRoles(user.getRoles());
        this.setResourceIds(Collections.emptySet());
        this.setClientSecret(user.getPassword());
        this.setAdditionalInformation(Collections.emptyMap());

        return this;
    }

}