package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantServiceImpl implements TenantService {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private DeviceRepository deviceRepository;

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

			if (!fromStorage.getLogLevel().equals(newLogLevel)) {
				LOG.info("Changed organization log level: {} -> {}", fromStorage.getLogLevel(),
						newLogLevel);

				fromStorage.setLogLevel(newLogLevel);

				tenantRepository.save(fromStorage);

				// change all tenant devices log levels
				List<Device> devices = deviceRepository.findAllByTenant(tenant.getId());

				for (Device device : devices) {
					device.setLogLevel(newLogLevel);
					deviceRepository.save(device);
				}
			}

			return ServiceResponseBuilder.<Tenant>ok().withResult(fromStorage).build();

		} catch (Exception e) {
			return ServiceResponseBuilder.<Tenant>error().withMessage(Errors.ERROR_SAVE_TENANT.getCode()).build();
		}

	}

}
