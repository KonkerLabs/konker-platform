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
public class KonkerIuguPlan {

	private String tenantDomain;
	private String tenantName;
	private String iuguPlanIdentifier;
	private String iuguCustomerId;
	private String iuguSubscriptionId;

	private String email;
	private String name;

	@JsonProperty("zipcode")
	private String zipCode;
	private String street;
	private String city;
	private String state;
	private String country;

}
