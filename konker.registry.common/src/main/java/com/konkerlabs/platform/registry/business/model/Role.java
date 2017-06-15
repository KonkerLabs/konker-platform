package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "roles")
@Data
@Builder
public class Role implements Serializable {

	@Id
	private String id;
	private String name;
	
	@DBRef
	private List<Privilege> privileges;
}
