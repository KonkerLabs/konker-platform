package com.konkerlabs.platform.registry.business.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Builder;
import lombok.Data;

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
    
    public enum LoginAuditEvent {
    	LOGIN, WRONG_PASSWD
    }

}