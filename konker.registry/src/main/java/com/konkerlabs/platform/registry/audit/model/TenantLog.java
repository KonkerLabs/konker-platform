package com.konkerlabs.platform.registry.audit.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantLog {

	private Instant time;
	private String message;

}
