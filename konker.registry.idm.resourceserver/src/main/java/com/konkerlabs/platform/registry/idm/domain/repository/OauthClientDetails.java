package com.konkerlabs.platform.registry.idm.domain.repository;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;


@Data
@Builder
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
    private String authorizedGrantTypes = "authorization_code,refresh_token,client_credentials";
    private String webServerRedirectUri;
    private String authorities;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private String additionalInformation;
    private boolean trusted = false;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private Boolean active;

    public String getName(){
        return clientId;
    }

    public void setName(String val){
        setClientId(val);
    }

    @Tolerate
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
}