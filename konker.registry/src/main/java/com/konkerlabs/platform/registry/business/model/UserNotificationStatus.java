package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
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
}
