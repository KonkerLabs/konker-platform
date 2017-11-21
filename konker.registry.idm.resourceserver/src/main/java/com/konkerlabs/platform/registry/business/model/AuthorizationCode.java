package com.konkerlabs.platform.registry.business.model;


import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Data
@Builder
@Document(collection = "authorizationCodes")
public class AuthorizationCode implements Serializable {

    @Id
    private String code;

    @CreatedDate
    private Date createTime = Date.from(Instant.now());

    @Version
    private Long version;

    private byte[] authenticationBytes;

    @Tolerate
    public AuthorizationCode() {
    }

    public OAuth2Authentication authentication() {
        return SerializationUtils.deserialize(authenticationBytes);
    }

    public AuthorizationCode authentication(OAuth2Authentication authentication) {
        this.authenticationBytes = SerializationUtils.serialize(authentication);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "code='" + code + '\'' +
                ", createTime=" + createTime +
                ", version=" + version +
                ", authenticationBytes=" + authenticationBytes +
                '}';
    }
}