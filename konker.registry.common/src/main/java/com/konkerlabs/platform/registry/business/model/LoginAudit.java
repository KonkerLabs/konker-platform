package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

@Data
@Builder
public class LoginAudit {

	@Id
	private String id;
	private Date time;
	@DBRef
	private Tenant tenant;
	@DBRef
	private User user;
    private String event;
    
    public static enum LoginAuditEvent {
    	LOGIN, WRONG_PASSWD;
    }

}