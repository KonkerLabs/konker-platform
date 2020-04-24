package com.konkerlabs.platform.registry.business.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IuguCustomer {

	private String id;
	private String email;
	private String name;

	@JsonProperty("zip-code")
	private String zipCode;
	private String street;
	private String city;
	private String state;
	private String country;


}
