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
public class KonkerIuguCharge {

	@JsonProperty("next_charge")
	private String nextCharge;

	@JsonProperty("next_charge_value")
	private String nextChargeValue;

	@JsonProperty("masked_card_number")
	private String maskedCardNumber;

}
