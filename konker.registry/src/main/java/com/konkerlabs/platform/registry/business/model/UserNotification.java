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

@Document(collection = "userNotifications")
@Data
@Builder
@CompoundIndexes({
    @CompoundIndex(name = "user_notications_ts", def = "{'destination': 1, 'date': -1}", unique = true)
})
public class UserNotification {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String uuid;
    
    /**
     * Reference to a user
     */
    private String destination;
    
    /**
     * Timestamp (UTC)
     */
    private Instant date;

    
    private String subject;
    private String body;
    
    /**
     * Content type of the message
     */
    private String contentType;

    /**
     * Language of the message
     */
    private String lang;
    
    
    private Boolean unread = Boolean.TRUE;

    /**
     * Groups messages so they can be correlated later. Suppose we have sent 
     * a wrong message to a million users. All of these messages should have the same 
     * correlationUuuid
     */
    @Indexed
    private String correlationUuid;
}
