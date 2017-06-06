package com.konkerlabs.platform.registry.business.services;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.HealthAlertRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;
import com.konkerlabs.platform.registry.business.services.api.UserService;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HealthAlertServiceImpl implements HealthAlertService {

    private Logger LOGGER = LoggerFactory.getLogger(HealthAlertServiceImpl.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserNotificationService userNotificationService;


    private ServiceResponse<HealthAlert> basicValidate(Tenant tenant, Application application, HealthAlert healthAlert) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			HealthAlert alert = HealthAlert.builder()
					.guid("NULL")
					.tenant(Tenant.builder().domainName("unknow_domain").build())
					.build();

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
						alert.toURI(),
						alert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

		if (!tenantRepository.exists(tenant.getId())) {
			LOGGER.debug("HealthAlert cannot exists",
					HealthAlert.builder()
					.guid("NULL")
					.tenant(tenant)
					.build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!Optional.ofNullable(application).isPresent()) {
			HealthAlert alert = HealthAlert.builder()
					.guid("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
						alert.toURI(),
						alert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}

		if (!applicationRepository.exists(application.getName())) {
			HealthAlert alert = HealthAlert.builder()
					.guid("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode(),
						alert.toURI(),
						alert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!Optional.ofNullable(healthAlert).isPresent()) {
			HealthAlert app = HealthAlert.builder()
					.guid("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(Validations.HEALTH_ALERT_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(healthAlert.getDeviceGuid()).isPresent()
				|| healthAlert.getDeviceGuid().isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				healthAlert.setGuid("NULL");
				LOGGER.debug(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(),
						healthAlert.toURI(),
						healthAlert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode())
					.build();
		}

		Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), healthAlert.getDeviceGuid());
		if (!Optional.ofNullable(device).isPresent()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(),
						healthAlert.toURI(),
						healthAlert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!Optional.ofNullable(healthAlert.getTriggerGuid()).isPresent()
				|| healthAlert.getTriggerGuid().isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				healthAlert.setGuid("NULL");
				LOGGER.debug(Validations.HEALTH_ALERT_TRIGGER_GUID_NULL.getCode(),
						healthAlert.toURI(),
						healthAlert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_TRIGGER_GUID_NULL.getCode())
					.build();
		}

		AlertTrigger trigger = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(tenant.getId(), application.getName(), healthAlert.getTriggerGuid());
		if (!Optional.ofNullable(trigger).isPresent()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(Validations.HEALTH_ALERT_TRIGGER_NOT_EXIST.getCode(),
						healthAlert.toURI(),
						healthAlert.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_TRIGGER_NOT_EXIST.getCode())
					.build();
		}


		return null;
	}

	@Override
	public ServiceResponse<HealthAlert> register(Tenant tenant, Application application, HealthAlert healthAlert) {
		ServiceResponse<HealthAlert> response = basicValidate(tenant, application, healthAlert);

		if (Optional.ofNullable(response).isPresent())
			return response;

		Optional<Map<String,Object[]>> validations = healthAlert.applyValidations();

		if (validations.isPresent()) {
			LOGGER.debug("error saving health alert",
					HealthAlert.builder().guid("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessages(validations.get())
					.build();
		}

		Instant now = Instant.now();

		healthAlert.setTenant(tenant);
		healthAlert.setApplication(application);
		healthAlert.setGuid(UUID.randomUUID().toString());
		healthAlert.setRegistrationDate(now);
		healthAlert.setLastChange(now);
		HealthAlert save = healthAlertRepository.save(healthAlert);

		ServiceResponse<HealthAlert> serviceResponse = getLastHightServerityByDeviceGuid(tenant, application, healthAlert.getDeviceGuid());
		sendNotification(tenant, serviceResponse.getResult());

		LOGGER.info("HealthAlert created. Guid: {}", save.getGuid(), tenant.toURI(), tenant.getLogLevel());
		return ServiceResponseBuilder.<HealthAlert>ok().withResult(save).build();
	}

	private void sendNotification(Tenant tenant, HealthAlert healthAlert) {
		ServiceResponse<List<User>> serviceResponse = userService.findAll(tenant);
		Device device = deviceRepository.findByTenantAndGuid(tenant.getId(), healthAlert.getDeviceGuid());

		if (serviceResponse.isOk() && !serviceResponse.getResult().isEmpty()) {
			serviceResponse.getResult().forEach(u -> {
				String body = MessageFormat.format("{0} - {1}", healthAlert.getSeverity().name(), healthAlert.getDescription());

				userNotificationService.postNotification(u, UserNotification.buildFresh(u.getEmail(),
						MessageFormat.format("Health of device {0} - {1}", device.getDeviceId(), healthAlert.getSeverity().name()),
						u.getLanguage().getLanguage(),
						"text/plain",
						Instant.now(),
						null,
						body));

			});
		}
	}

	@Override
	public ServiceResponse<HealthAlert> update(Tenant tenant, Application application, String healthAlertGuid, HealthAlert updatingHealthAlert) {
		ServiceResponse<HealthAlert> response = basicValidate(tenant, application, updatingHealthAlert);

		if (Optional.ofNullable(response).isPresent())
			return response;

		if (!Optional.ofNullable(healthAlertGuid).isPresent())
            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
                    .build();

		HealthAlert healthAlertFromDB = healthAlertRepository.findByTenantIdApplicationNameAndGuid(
				tenant.getId(),
				application.getName(),
				healthAlertGuid);

		if (!Optional.ofNullable(healthAlertFromDB).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())
                    .build();
		}

		healthAlertFromDB.setDescription(updatingHealthAlert.getDescription());
		healthAlertFromDB.setSeverity(updatingHealthAlert.getSeverity());
		healthAlertFromDB.setType(updatingHealthAlert.getType());
		healthAlertFromDB.setLastChange(Instant.now());

		Optional<Map<String, Object[]>> validations = healthAlertFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessages(validations.get())
					.build();
		}

		HealthAlert updated = healthAlertRepository.save(healthAlertFromDB);

		LOGGER.info("HealthAlert updated. Guid: {}", healthAlertFromDB.getGuid(), tenant.toURI(), tenant.getLogLevel());
		return ServiceResponseBuilder.<HealthAlert>ok().withResult(updated).build();
	}

	@Override
	public ServiceResponse<HealthAlert> remove(Tenant tenant, Application application, String healthAlertGuid, Solution solution) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(healthAlertGuid).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
                    .build();
		}

		HealthAlert healthAlertFromDB = healthAlertRepository.findByTenantIdApplicationNameAndGuid(tenant.getId(), application.getName(), healthAlertGuid);

		if (!Optional.ofNullable(healthAlertFromDB).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())
                    .build();
		}

		healthAlertFromDB.setSolved(true);
		healthAlertFromDB.setLastChange(Instant.now());
		healthAlertFromDB.setSolution(solution);
		HealthAlert updated = healthAlertRepository.save(healthAlertFromDB);

		ServiceResponse<HealthAlert> serviceResponse = getLastHightServerityByDeviceGuid(tenant, application, healthAlertFromDB.getDeviceGuid());
		if (serviceResponse.isOk()) {
		    sendNotification(tenant, serviceResponse.getResult());
		} else {
	        return ServiceResponseBuilder.<HealthAlert>error()
	                .withMessages(serviceResponse.getResponseMessages())
	                .withResult(updated)
	                .build();
		}

		return ServiceResponseBuilder.<HealthAlert>ok()
				.withMessage(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode())
				.withResult(updated)
				.build();
	}

	@Override
	public ServiceResponse<List<HealthAlert>> findAllByTenantAndApplication(Tenant tenant, Application application) {
		List<HealthAlert> all = healthAlertRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
		return ServiceResponseBuilder.<List<HealthAlert>>ok().withResult(all).build();
	}

	@Override
	public ServiceResponse<List<HealthAlert>> findAllByTenantApplicationAndDeviceGuid(Tenant tenant,
			Application application,
			String deviceGuid) {

		if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(deviceGuid).isPresent()) {
			return ServiceResponseBuilder.<List<HealthAlert>>error()
					.withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode())
					.build();
		}

        Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), deviceGuid);
        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        List<HealthAlert> healthAlerts = healthAlertRepository
        		.findAllByTenantIdApplicationNameAndDeviceGuid(tenant.getId(), application.getName(), deviceGuid);

        if (healthAlerts.isEmpty()) {
        	return ServiceResponseBuilder.<List<HealthAlert>>error()
					.withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())
					.build();
        }

        return ServiceResponseBuilder.<List<HealthAlert>>ok()
                .withResult(healthAlerts)
                .build();
	}

	@Override
	public ServiceResponse<HealthAlert> getByTenantApplicationAndHealthAlertGuid(Tenant tenant, Application application, String healthAlertGuid) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(healthAlertGuid).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
					.build();
		}

		Tenant tenantFromDB = tenantRepository.findByName(tenant.getName());
		if (!Optional.ofNullable(tenantFromDB).isPresent())
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		Application appFromDB = applicationRepository.findByTenantAndName(tenantFromDB.getId(), application.getName());
		if (!Optional.ofNullable(appFromDB).isPresent())
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build();

		HealthAlert healthAlert = healthAlertRepository
				.findByTenantIdApplicationNameAndGuid(tenantFromDB.getId(), appFromDB.getName(), healthAlertGuid);

		if (!Optional.ofNullable(healthAlert).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<HealthAlert>ok().withResult(healthAlert).build();
	}

    @Override
    public ServiceResponse<List<HealthAlert>> removeAlertsFromTrigger(Tenant tenant, Application application,
            String triggerGuid) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }
        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }
        if (!Optional.ofNullable(triggerGuid).isPresent()) {
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
                    .build();
        }

        List<HealthAlert> alerts = healthAlertRepository.findAllByTenantIdApplicationNameAndTriggerGuid(tenant.getId(), application.getName(), triggerGuid);

        for (HealthAlert healthAlertFromDB : alerts) {
            healthAlertFromDB.setSolved(true);
            healthAlertFromDB.setLastChange(Instant.now());
            healthAlertFromDB.setSolution(Solution.TRIGGER_DELETED);
            healthAlertRepository.save(healthAlertFromDB);

            ServiceResponse<HealthAlert> serviceResponse = getLastHightServerityByDeviceGuid(tenant, application, healthAlertFromDB.getDeviceGuid());
            if (serviceResponse.isOk()) {
                sendNotification(tenant, serviceResponse.getResult());
            }
        }

        return ServiceResponseBuilder.<List<HealthAlert>>ok()
                .withMessage(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode())
                .withResult(alerts)
                .build();

    }

	@Override
	public ServiceResponse<HealthAlert> getLastHightServerityByDeviceGuid(Tenant tenant, Application application,
			String deviceGuid) {

		ServiceResponse<List<HealthAlert>> serviceResponse = findAllByTenantApplicationAndDeviceGuid(tenant, application, deviceGuid);

		if (!serviceResponse.isOk()) {
			if (serviceResponse.getResponseMessages().containsKey(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())) {
				return ServiceResponseBuilder.<HealthAlert> ok()
							.withResult(HealthAlert.builder()
									.severity(HealthAlertSeverity.OK)
									.description("Health of device is ok.")
									.deviceGuid(deviceGuid)
									.lastChange(Instant.now())
									.build())
							.build();
			}

			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessages(serviceResponse.getResponseMessages())
                    .build();
		}

		List<HealthAlert> healths = serviceResponse.getResult();

		healths.sort(
				Comparator
				.comparing((HealthAlert health) -> health.getSeverity().getPrior())
				.thenComparing(
						Comparator.comparing(health -> health.getLastChange())
						));

		return ServiceResponseBuilder.<HealthAlert> ok()
				.withResult(healths.get(0))
				.build();
	}

}
