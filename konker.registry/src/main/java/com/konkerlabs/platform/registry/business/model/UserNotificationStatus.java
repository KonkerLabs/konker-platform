package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "userNotificationStatus")
@Data
@Builder
public class UserNotificationStatus {
    @Id
    private String id;

    @Indexed(unique = true)
    private String destination;

    private Instant lastUpdated;
    private String lastNotificationUUid;

    private Boolean hasNewMessages = Boolean.FALSE;

    public void markHasNewMessages(String uuid) {
        this.hasNewMessages = Boolean.TRUE;
        this.lastUpdated = Instant.now();
        this.lastNotificationUUid = uuid;
    }

    public void unmarkHasNewMessages() {
        this.hasNewMessages = Boolean.FALSE;
        this.lastUpdated = Instant.now();
    }

}
