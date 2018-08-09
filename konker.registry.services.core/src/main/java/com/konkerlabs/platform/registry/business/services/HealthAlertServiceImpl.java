package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

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

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private MessageSource messageSource;

	@Override
	public ServiceResponse<HealthAlert> register(Tenant tenant, Application application, HealthAlert healthAlert) {
		ServiceResponse<HealthAlert> response = basicValidate(tenant, application, healthAlert);
		if (!response.isOk()) {
            return response;
        }

        // if alert id is null, use device id as default
		if (StringUtils.isBlank(healthAlert.getAlertId())) {
		    healthAlert.setAlertId(healthAlert.getDevice().getDeviceId());
        }

		Optional<Map<String,Object[]>> validations = healthAlert.applyValidations();

		if (validations.isPresent()) {
			LOGGER.debug("error saving health alert",
					HealthAlert.builder().guid("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessages(validations.get())
					.build();
		}

		HealthAlert healthAlertDB = healthAlertRepository.findByTenantIdApplicationNameTriggerAndAlertId(
                tenant.getId(),
                application.getName(),
                healthAlert.getAlertTrigger().getId(),
                healthAlert.getAlertId()
        );
        if (healthAlertDB != null) {
            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_ALREADY_EXISTS.getCode())
                    .build();
        }

        // do not create a alert with severity OK
        if (HealthAlertSeverity.OK == healthAlert.getSeverity()) {
            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_WITH_STATUS_OK.getCode())
                    .withResult(healthAlert)
                    .build();
        }

        Instant now = Instant.now();

		healthAlert.setTenant(tenant);
		healthAlert.setApplication(application);
		healthAlert.setGuid(UUID.randomUUID().toString());
		healthAlert.setRegistrationDate(now);
		healthAlert.setLastChange(now);

		HealthAlert save = healthAlertRepository.save(healthAlert);

		ServiceResponse<HealthAlert> serviceResponse = getLastHighestSeverityByDeviceGuid(tenant, application, healthAlert.getDevice().getGuid());
		sendNotification(tenant, serviceResponse.getResult(), healthAlert.getDevice());

		LOGGER.info("HealthAlert created. Guid: {}", save.getGuid(), tenant.toURI(), tenant.getLogLevel());
		return ServiceResponseBuilder.<HealthAlert>ok().withResult(save).build();
	}

	private void sendNotification(Tenant tenant, HealthAlert healthAlert, final Device device) {
		ServiceResponse<List<User>> serviceResponse = userService.findAll(tenant);

		if (serviceResponse.isOk() && !serviceResponse.getResult().isEmpty()) {
			serviceResponse.getResult().forEach(u -> {
                String body = getBody(healthAlert, u);
				String severity = messageSource.getMessage(healthAlert.getSeverity().getCode(), null, u.getLanguage().getLocale());

				userNotificationService.postNotification(u, UserNotification.buildFresh(u.getEmail(),
						messageSource.getMessage("controller.healthalert.email.subject",
						        new Object[] {device.getDeviceId(), severity},
						        u.getLanguage().getLocale()),
						u.getLanguage().getLanguage(),
						"text/plain",
						Instant.now(),
						null,
						body));

			});
		}
	}

    private String getBody(HealthAlert healthAlert, User u) {

        String body;
        String severityName = healthAlert.getSeverity().name();

        if (StringUtils.isBlank(healthAlert.getDescription())) {
            body = MessageFormat.format("{0}", severityName);
        } else {
            if (HealthAlert.Description.fromCode(healthAlert.getDescription()) == null) {
                body = MessageFormat.format("{0} - {1}", severityName, healthAlert.getDescription());
            } else {
                String i18Description = messageSource.getMessage(healthAlert.getDescription(), null, u.getLanguage().getLocale());
                body = MessageFormat.format("{0} - {1}", severityName, i18Description);
            }
        }

        return body;
    }

    @Override
	public ServiceResponse<HealthAlert> update(Tenant tenant, Application application, String healthAlertGuid, HealthAlert updatingHealthAlert) {

        ServiceResponse<HealthAlert> response = validate(tenant, application);
        if (!response.isOk()) {
            return response;
        }

		if (!Optional.ofNullable(updatingHealthAlert).isPresent()) {
            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(healthAlertGuid).isPresent()) {
            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
                    .build();
        }

        HealthAlert healthAlertFromDB = healthAlertRepository.findByTenantIdApplicationNameAndGuid(
				tenant.getId(),
				application.getName(),
				healthAlertGuid);

		if (!Optional.ofNullable(healthAlertFromDB).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())
                    .build();
		}

        HealthAlertSeverity oldStatus = healthAlertFromDB.getSeverity();
        HealthAlertSeverity newStatus = updatingHealthAlert.getSeverity();

        // if alert is resolved, remove it
        if (HealthAlertSeverity.OK.equals(newStatus)) {
            return remove(tenant, application, healthAlertFromDB.getGuid(), Solution.MARKED_AS_RESOLVED);
        }

		healthAlertFromDB.setDescription(updatingHealthAlert.getDescription());
		healthAlertFromDB.setSeverity(updatingHealthAlert.getSeverity());
		healthAlertFromDB.setLastChange(Instant.now());

		Optional<Map<String, Object[]>> validations = healthAlertFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessages(validations.get())
					.build();
		}

		HealthAlert updated = healthAlertRepository.save(healthAlertFromDB);

		// notify new status
		if (!newStatus.equals(oldStatus)) {
		    sendNotification(tenant, updated, updated.getDevice());
        }

		LOGGER.info("HealthAlert updated. Guid: {}", healthAlertFromDB.getGuid(), tenant.toURI(), tenant.getLogLevel());
		return ServiceResponseBuilder.<HealthAlert>ok().withResult(updated).build();
	}

	@Override
	public ServiceResponse<HealthAlert> remove(Tenant tenant, Application application, String healthAlertGuid, Solution solution) {

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
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

		ServiceResponse<HealthAlert> serviceResponse = getLastHighestSeverityByDeviceGuid(tenant, application, healthAlertFromDB.getDevice().getGuid());
		if (serviceResponse.isOk()) {
		    sendNotification(tenant, serviceResponse.getResult(), updated.getDevice());
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
	public ServiceResponse<List<HealthAlert>> findAllByTenantApplicationAndTrigger(Tenant tenant, Application application, AlertTrigger alertTrigger) {

		ServiceResponse<List<HealthAlert>> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

		if (!Optional.ofNullable(alertTrigger).isPresent()) {
			return ServiceResponseBuilder.<List<HealthAlert>>error()
					.withMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode())
					.build();
		}

		List<HealthAlert> healthAlerts = healthAlertRepository.findAllByTenantIdApplicationNameAndTriggerId(tenant.getId(), application.getName(), alertTrigger.getId());

		return ServiceResponseBuilder.<List<HealthAlert>>ok()
				.withResult(healthAlerts)
				.build();

	}

	@Override
	public ServiceResponse<List<HealthAlert>> findAllByTenantApplicationAndDeviceGuid(Tenant tenant,
			Application application,
			String deviceGuid) {

		ServiceResponse<List<HealthAlert>> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

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
        		.findAllByTenantIdApplicationNameAndDeviceId(tenant.getId(), application.getName(), device.getId());

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

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

		if (!Optional.ofNullable(healthAlertGuid).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode())
					.build();
		}

		Tenant tenantFromDB = tenantRepository.findByDomainName(tenant.getDomainName());
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
	public ServiceResponse<HealthAlert> findByTenantApplicationTriggerAndAlertId(Tenant tenant, Application application, AlertTrigger alertTrigger, String alertId) {

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

		if (!Optional.ofNullable(alertTrigger).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(alertId).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(Validations.HEALTH_ALERT_NULL_ALERT_ID.getCode())
					.build();
		}

		Tenant tenantFromDB = tenantRepository.findByDomainName(tenant.getDomainName());
		if (!Optional.ofNullable(tenantFromDB).isPresent())
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		Application appFromDB = applicationRepository.findByTenantAndName(tenantFromDB.getId(), application.getName());
		if (!Optional.ofNullable(appFromDB).isPresent())
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build();

		HealthAlert healthAlert = healthAlertRepository
				.findByTenantIdApplicationNameTriggerAndAlertId(tenantFromDB.getId(), appFromDB.getName(), alertTrigger.getId(), alertId);

		if (!Optional.ofNullable(healthAlert).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert> error()
					.withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<HealthAlert>ok().withResult(healthAlert).build();
	}

    @Override
    public ServiceResponse<List<HealthAlert>> removeAlertsFromTrigger(Tenant tenant, Application application,
            AlertTrigger alertTrigger) {

		ServiceResponse<List<HealthAlert>> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

        if (!Optional.ofNullable(alertTrigger).isPresent()) {
            return ServiceResponseBuilder.<List<HealthAlert>>error()
                    .withMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode())
                    .build();
        }

        List<HealthAlert> alerts = healthAlertRepository.findAllByTenantIdApplicationNameAndTriggerId(tenant.getId(), application.getName(), alertTrigger.getId());

        for (HealthAlert healthAlertFromDB : alerts) {
            healthAlertFromDB.setSolved(true);
            healthAlertFromDB.setLastChange(Instant.now());
            healthAlertFromDB.setSolution(Solution.TRIGGER_DELETED);
            healthAlertRepository.save(healthAlertFromDB);

            ServiceResponse<HealthAlert> serviceResponse = getLastHighestSeverityByDeviceGuid(tenant, application, healthAlertFromDB.getDevice().getGuid());
            if (serviceResponse.isOk()) {
                sendNotification(tenant, serviceResponse.getResult(), healthAlertFromDB.getDevice());
            }
        }

        return ServiceResponseBuilder.<List<HealthAlert>>ok()
                .withMessage(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode())
                .withResult(alerts)
                .build();

    }

	@Override
	public ServiceResponse<HealthAlert> getLastHighestSeverityByDeviceGuid(Tenant tenant, Application application,
			String deviceGuid) {

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

        ServiceResponse<List<HealthAlert>> serviceResponse = findAllByTenantApplicationAndDeviceGuid(tenant, application, deviceGuid);

		if (!serviceResponse.isOk()) {
			if (serviceResponse.getResponseMessages().containsKey(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode())) {
				return ServiceResponseBuilder.<HealthAlert> ok()
							.withResult(HealthAlert.builder()
									.severity(HealthAlertSeverity.OK)
									.lastChange(Instant.now())
									.build())
							.build();
			}

			return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessages(serviceResponse.getResponseMessages())
                    .build();
		}

		List<HealthAlert> healths = serviceResponse.getResult();

		// sort by severity (descending)
		healths.sort(
				Comparator
				.comparing((HealthAlert health) -> health.getSeverity().getPrior())
				.thenComparing((HealthAlert health) -> health.getLastChange() == null ? health.getRegistrationDate() : health.getLastChange()));

		return ServiceResponseBuilder.<HealthAlert> ok()
				.withResult(healths.get(0))
				.build();
	}

	@Override
	public ServiceResponse<HealthAlert> getCurrentHealthByGuid(Tenant tenant,
                                                               Application application,
															   String deviceGuid) {

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
        }

		if (!Optional.ofNullable(deviceGuid).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode())
					.build();
		}

		Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), deviceGuid);
		if (!Optional.ofNullable(device).isPresent()) {
			return ServiceResponseBuilder.<HealthAlert>error()
					.withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!device.isActive()) {
			return ServiceResponseBuilder.<HealthAlert>ok()
					.withResult(HealthAlert.builder().severity(HealthAlertSeverity.DISABLED).build())
					.build();
		}

		ServiceResponse<List<Event>> incomingResponse = deviceEventService.findIncomingBy(tenant, application, deviceGuid, null, null, null, false, 1);
		if (incomingResponse.isOk() && incomingResponse.getResult().isEmpty()) {
			return ServiceResponseBuilder.<HealthAlert>ok()
					.withResult(HealthAlert.builder().severity(HealthAlertSeverity.NODATA).build())
					.build();
		}

		return getLastHighestSeverityByDeviceGuid(tenant, application, deviceGuid);

	}

    private <T> ServiceResponse<T>  validate(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            HealthAlert alert = HealthAlert.builder()
                    .guid("NULL")
                    .tenant(Tenant.builder().domainName("unknown_domain").build())
                    .build();

            if(LOGGER.isDebugEnabled()){
                LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                        alert.toURI(),
                        alert.getTenant().getLogLevel());
            }

            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!tenantRepository.exists(tenant.getId())) {
            LOGGER.debug("HealthAlert do not exists",
                    HealthAlert.builder()
                            .guid("NULL")
                            .tenant(tenant)
                            .build().toURI(),
                    tenant.getLogLevel());
            return ServiceResponseBuilder.<T>error()
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

            return ServiceResponseBuilder.<T>error()
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

            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<T>ok().build();

    }

    private ServiceResponse<HealthAlert> basicValidate(Tenant tenant, Application application, HealthAlert healthAlert) {

        ServiceResponse<HealthAlert> validationsResponse = validate(tenant, application);
        if (!validationsResponse.isOk()) {
            return validationsResponse;
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

        if (healthAlert.getDevice() == null) {
            if(LOGGER.isDebugEnabled()){
                healthAlert.setGuid("NULL");
                LOGGER.debug(Validations.HEALTH_ALERT_DEVICE_NULL.getCode(),
                        healthAlert.toURI(),
                        healthAlert.getTenant().getLogLevel());
            }

            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_DEVICE_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(healthAlert.getDevice().getGuid()).isPresent()
                || healthAlert.getDevice().getGuid().isEmpty()) {
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

        Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), healthAlert.getDevice().getGuid());
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

        if (healthAlert.getAlertTrigger() == null) {
            if(LOGGER.isDebugEnabled()){
                healthAlert.setGuid("NULL");
                LOGGER.debug(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode(),
                        healthAlert.toURI(),
                        healthAlert.getTenant().getLogLevel());
            }

            return ServiceResponseBuilder.<HealthAlert>error()
                    .withMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(healthAlert.getAlertTrigger().getGuid()).isPresent()
                || healthAlert.getAlertTrigger().getGuid().isEmpty()) {
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

        AlertTrigger trigger = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(tenant.getId(), application.getName(), healthAlert.getAlertTrigger().getGuid());
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

        return ServiceResponseBuilder.<HealthAlert>ok().build();

    }


}
