package com.konkerlabs.platform.registry.business.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "roles")
@Data
@Builder
public class Role {

	@Id
	private String id;
	private String name;
	
	@DBRef
	private List<Privilege> privileges;
}
