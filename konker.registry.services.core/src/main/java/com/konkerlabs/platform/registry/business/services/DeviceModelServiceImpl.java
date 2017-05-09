package com.konkerlabs.platform.registry.business.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceModelServiceImpl implements DeviceModelService {

    private Logger LOGGER = LoggerFactory.getLogger(DeviceModelServiceImpl.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private DeviceModelRepository deviceModelRepository;

    
    private ServiceResponse<DeviceModel> basicValidate(Tenant tenant, Application application, DeviceModel deviceModel) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(Tenant.builder().domainName("unknow_domain").build())
					.build();

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

		if (!tenantRepository.exists(tenant.getId())) {
			LOGGER.debug("device cannot exists",
					Application.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!Optional.ofNullable(application).isPresent()) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}
		
		if (!applicationRepository.exists(application.getName())) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
					.build();
		}
		
		if (!Optional.ofNullable(deviceModel).isPresent()) {
			DeviceModel app = DeviceModel.builder()
					.name("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(Validations.DEVICE_MODEL_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(Validations.DEVICE_MODEL_NULL.getCode())
					.build();
		}

		return null;
	}

	@Override
	public ServiceResponse<DeviceModel> register(Tenant tenant, Application application, DeviceModel deviceModel) {
		ServiceResponse<DeviceModel> response = basicValidate(tenant, application, deviceModel);

		if (Optional.ofNullable(response).isPresent())
			return response;

		Optional<Map<String,Object[]>> validations = deviceModel.applyValidations();

		if (validations.isPresent()) {
			LOGGER.debug("error saving device model",
					DeviceModel.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessages(validations.get())
					.build();
		}

		if (deviceModelRepository
				.findByTenantIdApplicationNameAndName(tenant.getId(), application.getName(), deviceModel.getName()) != null) {
			LOGGER.debug("error saving device model",
					DeviceModel.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
            return ServiceResponseBuilder.<DeviceModel>error()
                    .withMessage(Validations.DEVICE_MODEL_ALREADY_REGISTERED.getCode())
                    .build();
		}

		deviceModel.setTenant(tenant);
		DeviceModel save = deviceModelRepository.save(deviceModel);
		LOGGER.info("DeviceModel created. Name: {}", save.getName(), tenant.toURI(), tenant.getLogLevel());

		return ServiceResponseBuilder.<DeviceModel>ok().withResult(save).build();
	}

	@Override
	public ServiceResponse<DeviceModel> update(Tenant tenant, Application application, String name, DeviceModel updatingDeviceModel) {
		ServiceResponse<DeviceModel> response = basicValidate(tenant, application, updatingDeviceModel);

		if (Optional.ofNullable(response).isPresent())
			return response;

		if (!Optional.ofNullable(name).isPresent())
            return ServiceResponseBuilder.<DeviceModel>error()
                    .withMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode())
                    .build();

		DeviceModel devModelFromDB = getByTenantApplicationName(tenant, application, name).getResult();
		if (!Optional.ofNullable(devModelFromDB).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
                    .withMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode())
                    .build();
		}

		devModelFromDB.setDescription(updatingDeviceModel.getDescription());
		devModelFromDB.setDefaultModel(updatingDeviceModel.isDefaultModel());

		Optional<Map<String, Object[]>> validations = devModelFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessages(validations.get())
					.build();
		}

		DeviceModel updated = deviceModelRepository.save(devModelFromDB);

		LOGGER.info("DeviceModel updated. Name: {}", devModelFromDB.getName(), tenant.toURI(), tenant.getLogLevel());

		return ServiceResponseBuilder.<DeviceModel>ok().withResult(updated).build();
	}

	@Override
	public ServiceResponse<DeviceModel> remove(Tenant tenant, Application application, String name) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}
		
		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(name).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
                    .withMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode())
                    .build();
		}

		DeviceModel deviceModel = deviceModelRepository.findByTenantIdApplicationNameAndName(tenant.getId(), application.getName(), name);

		if (!Optional.ofNullable(deviceModel).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
                    .withMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode())
                    .build();
		}

		deviceModelRepository.delete(deviceModel);

		return ServiceResponseBuilder.<DeviceModel>ok()
				.withMessage(Messages.DEVICE_MODEL_REMOVED_SUCCESSFULLY.getCode())
				.withResult(deviceModel)
				.build();
	}

	@Override
	public ServiceResponse<List<DeviceModel>> findAll(Tenant tenant, Application application) {
		List<DeviceModel> all = deviceModelRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
		return ServiceResponseBuilder.<List<DeviceModel>>ok().withResult(all).build();
	}

	@Override
	public ServiceResponse<DeviceModel> getByTenantApplicationName(Tenant tenant, Application application, String name) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(name).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel>error()
					.withMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode())
					.build();
		}

		Tenant tenantFromDB = tenantRepository.findByName(tenant.getName());
		if (!Optional.ofNullable(tenantFromDB).isPresent())
			return ServiceResponseBuilder.<DeviceModel> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		Application appFromDB = applicationRepository.findByTenantAndName(tenantFromDB.getId(), application.getName());
		if (!Optional.ofNullable(appFromDB).isPresent())
			return ServiceResponseBuilder.<DeviceModel> error()
					.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build();
		
		DeviceModel deviceModel = deviceModelRepository.findByTenantIdApplicationNameAndName(tenantFromDB.getId(), appFromDB.getName(), name);
		if (!Optional.ofNullable(deviceModel).isPresent()) {
			return ServiceResponseBuilder.<DeviceModel> error()
					.withMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<DeviceModel>ok().withResult(deviceModel).build();
	}

}
