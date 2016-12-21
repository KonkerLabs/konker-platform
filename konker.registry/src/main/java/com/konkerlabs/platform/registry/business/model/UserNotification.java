package com.konkerlabs.platform.registry.business.model;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Document(collection = "userNotifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_notications_ts", def = "{'destination': 1, 'date': -1}", unique = false)
})
@EqualsAndHashCode(of={"uuid", "subject", "date", "destination", "unread"})
@Value
@Builder
public class UserNotification implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 641917038573358275L;

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

    
    @Getter
    private String subject;
    private String body;
    
    /**
     * Content type of the message
     */
    private String contentType;

    /**
     * Language of the message
     */
    private String contentLanguage;
    
    @NonFinal
    private Boolean unread = Boolean.TRUE;
    
    @NonFinal
    private Instant lastReadDate;

    /**
     * Groups messages so they can be correlated later. Suppose we have sent 
     * a wrong message to a million users. All of these messages should have the same 
     * correlationUuuid
     */
    @Indexed
    private String correlationUuid;
}
