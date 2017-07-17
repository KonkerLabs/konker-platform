package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceCustomDataRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceCustomDataServiceImpl implements DeviceCustomDataService {

    @Autowired
    private DeviceCustomDataRepository deviceCustomDataRepository;

    @Autowired
    private DeviceRepository deviceRepository;

	@Override
	public ServiceResponse<DeviceCustomData> save(Tenant tenant, Application application, Device device, String jsonCustomData) {

        ServiceResponse<DeviceCustomData> validationsResponse = validate(tenant, application, device);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (!isValidJson(jsonCustomData)){
            return ServiceResponseBuilder.<DeviceCustomData>error()
                    .withMessage(Validations.DEVICE_CUSTOM_DATA_INVALID_JSON.getCode())
                    .build();
        }

		if (deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(
		        tenant.getId(),
		        application.getName(),
		        device.getId()) != null) {
            return ServiceResponseBuilder.<DeviceCustomData>error()
                    .withMessage(Validations.DEVICE_CUSTOM_DATA_ALREADY_REGISTERED.getCode())
                    .build();
		}

		DeviceCustomData deviceCustomData = DeviceCustomData.builder()
		                                                    .tenant(tenant)
		                                                    .application(application)
		                                                    .device(device)
		                                                    .json(jsonCustomData)
		                                                    .lastChange(Instant.now())
		                                                    .build();

		DeviceCustomData save = deviceCustomDataRepository.save(deviceCustomData);

		return ServiceResponseBuilder.<DeviceCustomData>ok().withResult(save).build();
	}

    @Override
    public ServiceResponse<DeviceCustomData> update(Tenant tenant, Application application, Device device, String jsonCustomData) {

        ServiceResponse<DeviceCustomData> validationsResponse = validate(tenant, application, device);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (!isValidJson(jsonCustomData)){
            return ServiceResponseBuilder.<DeviceCustomData>error()
                    .withMessage(Validations.DEVICE_CUSTOM_DATA_INVALID_JSON.getCode())
                    .build();
        }

        DeviceCustomData customDataFromDB = deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(
                tenant.getId(),
                application.getName(),
                device.getId());

		if (!Optional.ofNullable(customDataFromDB).isPresent()) {
			return ServiceResponseBuilder.<DeviceCustomData>error()
                    .withMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode())
                    .build();
		}

		customDataFromDB.setJson(jsonCustomData);
		customDataFromDB.setLastChange(Instant.now());

		Optional<Map<String, Object[]>> validations = customDataFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<DeviceCustomData>error()
					.withMessages(validations.get())
					.build();
		}

		DeviceCustomData updated = deviceCustomDataRepository.save(customDataFromDB);

		return ServiceResponseBuilder.<DeviceCustomData>ok().withResult(updated).build();
	}

    @Override
    public ServiceResponse<DeviceCustomData> remove(Tenant tenant, Application application, Device device) {

        ServiceResponse<DeviceCustomData> validationsResponse = validate(tenant, application, device);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

		DeviceCustomData deviceCustomData = deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(
		        tenant.getId(),
		        application.getName(),
		        device.getId());

		if (!Optional.ofNullable(deviceCustomData).isPresent()) {
			return ServiceResponseBuilder.<DeviceCustomData>error()
                    .withMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode())
                    .build();
		}

		deviceCustomDataRepository.delete(deviceCustomData);

		return ServiceResponseBuilder.<DeviceCustomData>ok()
				.withMessage(Messages.DEVICE_CUSTOM_DATA_REMOVED_SUCCESSFULLY.getCode())
				.withResult(deviceCustomData)
				.build();
	}

    @Override
    public ServiceResponse<DeviceCustomData> getByTenantApplicationAndDevice(Tenant tenant, Application application,
            Device device) {

        ServiceResponse<DeviceCustomData> validationsResponse = validate(tenant, application, device);
        if (validationsResponse != null && !validationsResponse.isOk()) {
            return validationsResponse;
        }

		DeviceCustomData deviceCustomData = deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(
		        tenant.getId(),
		        application.getName(),
		        device.getId());

		if (!Optional.ofNullable(deviceCustomData).isPresent()) {
			return ServiceResponseBuilder.<DeviceCustomData> error()
					.withMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<DeviceCustomData>ok().withResult(deviceCustomData).build();
	}


    private <T> ServiceResponse<T> validate(Tenant tenant, Application application, Device device) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<T>error().withMessage(CommonValidations.TENANT_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()).build();
        }

        if (!Optional.ofNullable(
                deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), device.getGuid())).isPresent()) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build();
        }

        return null;
    }

    private boolean isValidJson(String json) {

        if (StringUtils.isBlank(json)) {
            return false;
        } else {
            try {
                JSON.parse(json);
            } catch (JSONParseException e) {
                return false;
            }
        }

        return true;

    }

}
