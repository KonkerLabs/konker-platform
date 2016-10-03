package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.web.controllers.DeviceController;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceRegisterServiceImpl implements DeviceRegisterService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EventRouteRepository eventRouteRepository;

    @Autowired
    private DataEnrichmentExtensionRepository dataEnrichmentExtensionRepository;

    @Autowired @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public ServiceResponse<Device> register(Tenant tenant, Device device) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode(), null)
                    .build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(), null)
                    .build();

        device.onRegistration();
        device.setGuid(UUID.randomUUID().toString());

        if (Optional.ofNullable(deviceRepository.findByApiKey(device.getApiKey())).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.GENERIC_ERROR.getCode(), null)
                    .build();
        }

        device.setTenant(tenant);

        Optional<Map<String, Object[]>> validations = device.applyValidations();

        if (validations.isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();

        if (deviceRepository.findByTenantIdAndDeviceId(tenant.getId(), device.getDeviceId()) != null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(), null)
                    .build();
        }

        Device saved = deviceRepository.save(device);

        return ServiceResponseBuilder.<Device>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<List<Device>> findAll(Tenant tenant) {
        List<Device> all = deviceRepository.findAllByTenant(tenant.getId());
        return ServiceResponseBuilder.<List<Device>>ok().withResult(all).build();
    }


    @Override
    public Device findByApiKey(String apiKey) {
        return deviceRepository.findByApiKey(apiKey);
    }

    @Override
    public Device findByTenantDomainNameAndDeviceGuid(String tenantDomainName, String deviceGuid) {
        return deviceRepository.findByTenantAndGuid(
                tenantRepository.findByDomainName(tenantDomainName).getId(),
                deviceGuid
        );
    }


    @Override
    public ServiceResponse<Device> switchEnabledDisabled(Tenant tenant, String guid) {
        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

        Device found = getByDeviceGuid(tenant, guid).getResult();

        if (!Optional.ofNullable(found).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null)
                    .build();

        found.setActive(!found.isActive());

        Device updated = deviceRepository.save(found);

        return ServiceResponseBuilder.<Device>ok()
                .withResult(updated)
                .build();
    }

    @Override
    public ServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, String guid) {
        ServiceResponse<Device> serviceResponse = getByDeviceGuid(tenant, guid);

        if (serviceResponse.isOk()) {
            try {
                Device existingDevice = serviceResponse.getResult();
                PasswordManager passwordManager = new PasswordManager();
                String randomPassword = passwordManager.generateRandomPassword(12);
                existingDevice.setSecurityHash(passwordManager.createHash(randomPassword));
                Device saved = deviceRepository.save(existingDevice);
                return ServiceResponseBuilder.<DeviceSecurityCredentials>ok() 
                        .withResult(new DeviceSecurityCredentials(saved,randomPassword)).build();
            } catch (SecurityException e) {
                return ServiceResponseBuilder.<DeviceSecurityCredentials>error()
                        .withMessage(CommonValidations.GENERIC_ERROR.getCode(), null).build();
            }

        } else
            return ServiceResponseBuilder.<DeviceSecurityCredentials>error()
                    .withMessages(serviceResponse.getResponseMessages()).build();
    }

    
    @Override
    public ServiceResponse<Device> update(Tenant tenant, String guid, Device updatingDevice) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(updatingDevice).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode(), null)
                    .build();

        Device deviceFromDB = getByDeviceGuid(tenant, guid).getResult();
        if (deviceFromDB == null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null)
                    .build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());
        deviceFromDB.setActive(updatingDevice.isActive());

        Optional<Map<String, Object[]>> validations = deviceFromDB.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();
        }

        Device saved = deviceRepository.save(deviceFromDB);

        return ServiceResponseBuilder.<Device>ok()
                .withResult(saved)
                .build();
    }

    @Override
    public ServiceResponse<Device> remove(Tenant tenant, String guid) {

        if(!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode(), null)
                    .build();

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode(), null)
                    .build();

        //find device
        Device device = deviceRepository.findByTenantAndGuid(tenant.getId(), guid);

        if(!Optional.ofNullable(device).isPresent()){
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }
        //find dependencies
        List<EventRoute> eventRoutes =
                eventRouteRepository.findByIncomingUri(device.toURI());
        List<DataEnrichmentExtension> enrichmentExtensions =
                dataEnrichmentExtensionRepository
                        .findByTenantIdAndIncoming(tenant.getId(), device.toURI());

        ServiceResponse<Device> response = null;

        if(Optional.ofNullable(eventRoutes).isPresent() && eventRoutes.size() > 0) {
            if(response == null){
                response = ServiceResponseBuilder.<Device>error()
                        .withMessage(Validations.DEVICE_HAVE_EVENTROUTES.getCode())
                        .build();
            } else {
                response.setStatus(ServiceResponse.Status.ERROR);
                response.getResponseMessages().put(Validations.DEVICE_HAVE_EVENTROUTES.getCode(), null);
            }

        }

        if(Optional.ofNullable(enrichmentExtensions).isPresent() && enrichmentExtensions.size() > 0) {
            if(response == null){
                response = ServiceResponseBuilder.<Device>error()
                        .withMessage(Validations.DEVICE_HAVE_ENRICHMENTS.getCode())
                        .build();
            } else {
                response.setStatus(ServiceResponse.Status.ERROR);
                response.getResponseMessages().put(Validations.DEVICE_HAVE_ENRICHMENTS.getCode(), null);
            }
        }

        if(Optional.ofNullable(response).isPresent()) return response;

        try {
            eventRepository.removeBy(tenant, device.getGuid());
            deviceRepository.delete(device);
        } catch (BusinessException e){
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(DeviceController.Messages.DEVICE_REMOVED_UNSUCCESSFULLY.getCode())
                    .withResult(device)
                    .build();
        }

        return ServiceResponseBuilder.<Device>ok()
                .withMessage(DeviceController.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode())
                .withResult(device)
                .build();
    }


	@Override
	public ServiceResponse<Device> getByDeviceGuid(Tenant tenant, String guid) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<Device> error().withMessage(CommonValidations.TENANT_NULL.getCode(), null)
					.build();

        if (!Optional.ofNullable(guid).isPresent())
			return ServiceResponseBuilder.<Device> error().withMessage(Validations.DEVICE_GUID_NULL.getCode(), null)
					.build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<Device> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(), null).build();

		Device device = deviceRepository.findByTenantAndGuid(existingTenant.getId(), guid);
		if (!Optional.ofNullable(device).isPresent()) {
			return ServiceResponseBuilder.<Device> error()
					.withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null).build();
		}

		return ServiceResponseBuilder.<Device> ok().withResult(device).build();
	}

}
