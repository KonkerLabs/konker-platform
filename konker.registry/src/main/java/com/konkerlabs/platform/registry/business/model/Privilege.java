package com.konkerlabs.platform.registry.business.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "privileges")
@Data
@Builder
public class Privilege {

	@Id
	private String id;
	private String name;
	
}
