package com.konkerlabs.platform.registry.audit.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TenantLog {

	private Date time;
	private String message;
	private String level;
	
}
