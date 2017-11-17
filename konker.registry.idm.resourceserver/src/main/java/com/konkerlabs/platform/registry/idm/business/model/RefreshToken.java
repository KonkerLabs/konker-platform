package com.konkerlabs.platform.registry.idm.business.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.time.Instant;

@Document(collection = "refreshTokens")
public class RefreshToken implements Serializable {

    @Id
    private String tokenId;

    @CreatedDate
    private Instant createTime = Instant.now();

    @Version
    private Long version;


    private byte[] token;

    private byte[] authentication;


    public RefreshToken() {
    }

    public String tokenId() {
        return tokenId;
    }

    public RefreshToken tokenId(String tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    public Instant createTime() {
        return createTime;
    }


    public Long version() {
        return version;
    }


    public OAuth2RefreshToken token() {
        return SerializationUtils.deserialize(token);
    }

    public RefreshToken token(OAuth2RefreshToken token) {
        this.token = SerializationUtils.serialize(token);
        return this;
    }

    public OAuth2Authentication authentication() {
        return SerializationUtils.deserialize(authentication);
    }

    public RefreshToken authentication(OAuth2Authentication authentication) {
        this.authentication = SerializationUtils.serialize(authentication);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "tokenId='" + tokenId + '\'' +
                ", createTime=" + createTime +
                ", version=" + version +
                '}';
    }
}
