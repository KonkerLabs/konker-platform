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
public class IuguPaymentWay {

	private String id;

	@JsonProperty("customer_id")
	private String customerId;
	private String description;
	private String token;

	@JsonProperty("set_as_default")
	private boolean setAsDefault;

	@JsonProperty("item_type")
	private String itemType;

	private DataPaymentWay data;


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DataPaymentWay {

		@JsonProperty("holder_name")
		private String holderName;

		@JsonProperty("display_number")
		private String displayNumber;
		private String brand;
		private Long month;
		private Long year;
	}

}
