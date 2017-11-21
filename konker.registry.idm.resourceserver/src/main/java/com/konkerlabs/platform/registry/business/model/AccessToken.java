package com.konkerlabs.platform.registry.business.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Data
@Builder
@Document(collection = "accessTokens")
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken implements Serializable {

    private static final long serialVersionUID = 7588602587200382326L;

    @Id
    private String tokenId;

    @CreatedDate
    private Date createTime = Date.from(Instant.now());

    @Version
    private Long version;

    private byte[] token;

    private String authenticationId;

    private byte[] authentication;

    private String username;

    private String clientId;

    private String refreshToken;

    @Tolerate
    public OAuth2AccessToken token() {
        return SerializationUtils.deserialize(token);
    }

    @Tolerate
    public AccessToken token(OAuth2AccessToken token) {
        this.token = SerializationUtils.serialize(token);
        return this;
    }

    @Tolerate
    public OAuth2Authentication authentication() {
        return SerializationUtils.deserialize(authentication);
    }

    @Tolerate
    public AccessToken authentication(OAuth2Authentication authentication) {
        this.authentication = SerializationUtils.serialize(authentication);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{")
                .append("tokenId='").append(tokenId).append('\'')
                .append(", createTime=").append(createTime)
                .append(", version=").append(version)
                .append(", authenticationId='").append(authenticationId).append('\'')
                .append(", username='").append(username).append('\'')
                .append(", clientId='").append(clientId).append('\'')
                .append(", refreshToken='").append(refreshToken).append('\'')
                .append('}').toString();
    }

}