package com.konkerlabs.platform.registry.business.model;

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

}
