package com.konkerlabs.platform.registry.billing.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "tenantDailyUsage")
public class TenantDailyUsage {

	@Id
    private String id;
    private String tenantDomain;
	private Instant date;
	private int incomingEventsCount;
	private int incomingPayloadSize;
	private int incomingDevices;
    private int outgoingEventsCount;
    private int outgoingPayloadSize;
    private int outgoingDevices;
    private Instant processedAt;
	
}
