package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "privileges")
@Data
@Builder
public class Privilege {

	@Id
	private String id;
	private String name;
	
}
