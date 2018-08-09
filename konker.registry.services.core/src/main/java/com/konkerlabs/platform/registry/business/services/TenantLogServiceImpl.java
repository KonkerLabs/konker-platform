package com.konkerlabs.platform.registry.business.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantLogService;

@Service
public class TenantLogServiceImpl implements TenantLogService {

	private Logger LOGGER = LoggerFactory.getLogger(TenantLogServiceImpl.class);

    @Autowired
	private TenantLogRepository tenantLogRepository;

	@Override
	public ServiceResponse<List<TenantLog>> findByTenant(Tenant tenant, boolean ascending) {

		if (!Optional.ofNullable(tenant).isPresent()) {
			Device device = Device.builder().guid("NULL")
					.tenant(Tenant.builder().domainName("unknown_domain").build()).build();
			
			LOGGER.debug(CommonValidations.TENANT_NULL.getCode(), device.toURI(),
					device.getLogLevel());
			return ServiceResponseBuilder.<List<TenantLog>>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode(), null).build();
		}

		try {
			List<TenantLog> all = tenantLogRepository.findAll(tenant);

			if (ascending) {
				all.sort(Comparator.comparing(TenantLog::getTime));
			} else {
				all.sort((p1, p2) -> -p1.getTime().compareTo(p2.getTime()));
			}

			return ServiceResponseBuilder.<List<TenantLog>>ok().withResult(all).build();
		} catch (Exception e) {
			return ServiceResponseBuilder.<List<TenantLog>>error().withMessage(e.getMessage()).build();
		}

	}

}
