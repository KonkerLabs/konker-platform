package com.konkerlabs.platform.registry.business.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IuguSubscription {

	private String id;

	@JsonProperty("plan_identifier")
	private String planIdentifier;

	@JsonProperty("customer_id")
	private String customerId;

	@JsonProperty("expires_at")
	private String expiresAt;

	@JsonProperty("only_on_charge_success")
	private String onlyOnChargeSuccess;

	@JsonProperty("ignore_due_email")
	private String ignoreDueEmail;

	@JsonProperty("payable_with")
	private String payableWith;

	@JsonProperty("credits_based")
	private String creditsBased;

	@JsonProperty("price_cents")
	private Long priceCents;

	@JsonProperty("credits_cycle")
	private Long creditsCycle;

	@JsonProperty("credits_min")
	private Long creditsMin;

	@JsonProperty("subitems")
	private List<Item> subItems;

	@JsonProperty("two_step")
	private String twoStep;

	@JsonProperty("suspend_on_invoice_expired")
	private String suspendOnInvoiceExpired;


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Item {

		private String description;

		@JsonProperty("price_cents")
		private Long priceCents;
		private Long quantity;
		private boolean recurrent;
	}

}
