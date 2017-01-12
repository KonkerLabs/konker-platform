package com.konkerlabs.platform.registry.audit.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantLog {

	private Date time;
	private String message;

}
