package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantBillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantBillingServiceImpl implements TenantBillingService {

	private Logger LOG = LoggerFactory.getLogger(TenantBillingServiceImpl.class);
	
	@Autowired
	private TenantDailyUsageRepository tenantDailyUsageRepository;

	@Override
	public ServiceResponse<List<TenantDailyUsage>> findTenantDailyUsage(Tenant tenant) {
		 List<TenantDailyUsage> usages = tenantDailyUsageRepository.findAllByTenantDomain(tenant.getDomainName());
		
		 return ServiceResponseBuilder.<List<TenantDailyUsage>> ok().withResult(usages).build();
	}

}
