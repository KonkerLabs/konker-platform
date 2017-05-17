package com.konkerlabs.platform.registry.idm.domain.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Document(collection = "oauthclientdetails")
public class OauthClientDetails implements Serializable {

    @Id
    private String clientId;

    @CreatedDate
    private Date createTime = Date.from(Instant.now());

    @Version
    private Long version;
    private boolean archived = false;
    private String resourceIds;
    private String clientSecret;
    private String scope;
    private String authorizedGrantTypes = "authorization_code,refresh_token";
    private String webServerRedirectUri;
    private String authorities;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private String additionalInformation;
    private boolean trusted = false;

    public OauthClientDetails() {
    }


    public ClientDetails toClientDetails() {
        BaseClientDetails clientDetails = new BaseClientDetails(clientId, resourceIds, scope, authorizedGrantTypes, authorities, webServerRedirectUri);
        clientDetails.setClientSecret(clientSecret);

        if (StringUtils.isNotEmpty(additionalInformation)) {
            clientDetails.addAdditionalInformation("information", additionalInformation);
        }
        clientDetails.setAccessTokenValiditySeconds(accessTokenValidity);
        clientDetails.setRefreshTokenValiditySeconds(refreshTokenValidity);

        return clientDetails;
    }

    public Long version() {
        return version;
    }

    public boolean trusted() {
        return trusted;
    }

    public Date createTime() {
        return createTime;
    }

    public boolean archived() {
        return archived;
    }

    public OauthClientDetails archived(boolean archived) {
        this.archived = archived;
        return this;
    }

    public String clientId() {
        return clientId;
    }

    public String resourceIds() {
        return resourceIds;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public String scope() {
        return scope;
    }

    public String authorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    public String webServerRedirectUri() {
        return webServerRedirectUri;
    }

    public String authorities() {
        return authorities;
    }

    public Integer accessTokenValidity() {
        return accessTokenValidity;
    }

    public Integer refreshTokenValidity() {
        return refreshTokenValidity;
    }

    public String additionalInformation() {
        return additionalInformation;
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

    public OauthClientDetails clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public OauthClientDetails clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public OauthClientDetails resourceIds(String resourceIds) {
        this.resourceIds = resourceIds;
        return this;
    }

    public OauthClientDetails authorizedGrantTypes(String authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
        return this;
    }

    public OauthClientDetails scope(String scope) {
        this.scope = scope;
        return this;
    }

    public OauthClientDetails webServerRedirectUri(String webServerRedirectUri) {
        this.webServerRedirectUri = webServerRedirectUri;
        return this;
    }

    public OauthClientDetails authorities(String authorities) {
        this.authorities = authorities;
        return this;
    }

    public OauthClientDetails accessTokenValidity(Integer accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
        return this;
    }

    public OauthClientDetails refreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
        return this;
    }

    public OauthClientDetails trusted(boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    public OauthClientDetails additionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }
}