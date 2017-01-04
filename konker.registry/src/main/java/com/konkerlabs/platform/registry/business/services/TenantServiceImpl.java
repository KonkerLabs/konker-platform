package com.konkerlabs.platform.registry.business.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;

@Service
public class TenantServiceImpl implements TenantService {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TenantRepository tenantRepository;

	@Override
	public ServiceResponse<Tenant> updateLogLevel(Tenant tenant, LogLevel newLogLevel) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<Tenant>error()
                    .withMessage(Validations.TENANT_NULL.getCode())
                    .build();
        }

		Tenant fromStorage = tenantRepository.findOne(tenant.getId());

        if (!Optional.ofNullable(fromStorage).isPresent()) {
            return ServiceResponseBuilder.<Tenant>error()
                    .withMessage(Validations.NO_EXIST_TENANT.getCode())
                    .build();
        }

		try {

			fromStorage.setLogLevel(newLogLevel);

			tenantRepository.save(fromStorage);

			return ServiceResponseBuilder.<Tenant>ok().withResult(fromStorage).build();

		} catch (Exception e) {
			return ServiceResponseBuilder.<Tenant>error().withMessage(Errors.ERROR_SAVE_TENANT.getCode()).build();
		}

	}

}
