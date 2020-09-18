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
public class KonkerPaymentCustomer {

	private String tokenCard;
	private String planName;
	private String dateFirstPayment;
	private String customerName;
	private String email;
	private String tenantDomain;
	private String tenantName;
	@JsonProperty("zipcode")
	private String zipCode;
	private String street;
	private String city;
	private String state;
	private String country;


}
