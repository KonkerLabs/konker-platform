package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "tokens")
@Data
@Builder
public class Token {
    @Id
    private String token;
    private String userEmail;
    private Boolean isExpired;
    private String purpose;
    private Instant expirationDateTime;
    private Instant creationDateTime;
    private Instant useDateTime;

    @Tolerate
    public Token() {
    }
}
