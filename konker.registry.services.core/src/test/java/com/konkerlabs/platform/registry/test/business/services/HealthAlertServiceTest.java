package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.HealthAlertRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Messages;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
		PubServerConfig.class,
        EventStorageConfig.class,
        SpringMailTestConfiguration.class,
        EmailConfig.class,
        MongoBillingTestConfiguration.class,
        MessageSouceTestConfiguration.class})
public class HealthAlertServiceTest extends BusinessLayerTestSupport {

    private static final String DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

	private static final String TRIGGER_GUID = "7d51c242-81db-11e6-a8c2-0746f976f666";

	@Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

	@Autowired
	private DeviceRepository deviceRepository;

    private AlertTrigger trigger;
    private AlertTrigger triggerNoExists;
    private HealthAlert healthAlert;
    private HealthAlert tempHealthAlert;
    private HealthAlert newHealthAlert;
    private Application application;
    private Application otherApplication;
    private Tenant currentTenant;
    private Tenant otherTenant;
    private Device device;
    private Device deviceNoExists;

    @Before
    public void setUp() {
    	currentTenant = tenantRepository.findByName("Konker");
    	otherTenant = Tenant.builder()
    			.id("71fb0d48-674b-4f64-a3e5-0256ff3a0000")
    			.name("MyCompany")
    			.domainName("MyCompany")
    			.build();

    	application = Application.builder()
    					.name("smartffkonker")
    					.friendlyName("Konker Smart Frig")
    					.description("Konker Smart Frig - take pic, tells temperatue")
    					.tenant(currentTenant)
                        .qualifier("konker")
                        .registrationDate(Instant.ofEpochMilli(1453320973747L))
                        .build();

    	otherApplication = Application.builder()
				.name("smartffkonkerother")
				.friendlyName("Konker Smart Frig")
				.description("Konker Smart Frig - take pic, tells temperatue")
				.tenant(currentTenant)
                .qualifier("konker")
                .registrationDate(Instant.ofEpochMilli(1453320973747L))
				.build();

    	device = Device
					.builder()
					.tenant(currentTenant)
					.application(application)
					.guid("1fbbecc7-8566-40c4-8f7b-830287535970")
					.deviceId("1fbbecc7")
					.build();
        device = deviceRepository.save(device);

        deviceNoExists = Device
                .builder()
                .guid("794348a3-1b3c-4a33-a518-2eb7e7f4d8ff")
                .deviceId("794348a3")
                .build();

        trigger = AlertTrigger.builder().build();
        trigger.setGuid(TRIGGER_GUID);
        trigger.setApplication(application);
        trigger.setTenant(currentTenant);
        trigger.setMinutes(1);
        trigger.setType(AlertTrigger.AlertTriggerType.SILENCE);
        trigger = alertTriggerRepository.save(trigger);

        triggerNoExists = AlertTrigger
                .builder()
                .guid("2d3f52c4-0a9e-4d30-ba4e-2048e0867faa")
                .build();

        healthAlert = HealthAlert.builder()
    			.id("67014de6-81db-11e6-a5bc-3f99b78823c9")
                .alertId("silence-id")
    			.guid("7d51c242-81db-11e6-a8c2-0746f976f224")
                .alertTrigger(trigger)
    			.description("No message received from the device for a long time.")
    			.severity(HealthAlertSeverity.WARN)
    			.type(AlertTrigger.AlertTriggerType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747L))
    			.device(device)
    			.alertTrigger(trigger)
    			.application(application)
    			.tenant(currentTenant)
    			.build();
        healthAlert = healthAlertRepository.save(healthAlert);

        tempHealthAlert = HealthAlert.builder()
    			.id("67014de6-81db-11e6-a5bc-3f99b788249c")
                .alertId("temp-silence-id")
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
                .alertTrigger(trigger)
				.description("No message received from the device for a long time.")
    			.severity(HealthAlertSeverity.FAIL)
                .type(AlertTrigger.AlertTriggerType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747L))
                .device(device)
                .alertTrigger(trigger)
    			.application(application)
				.tenant(currentTenant)
				.build();
        tempHealthAlert = healthAlertRepository.save(tempHealthAlert);

    	newHealthAlert = HealthAlert.builder()
    			.guid("7d51c242-81db-11e6-a8c2-0746f976f225")
                .alertId("newHealthAlert-id")
                .alertTrigger(trigger)
    			.description("Health of device is ok.")
    			.severity(HealthAlertSeverity.OK)
    			.type(AlertTrigger.AlertTriggerType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747L))
                .device(device)
                .alertTrigger(trigger)
				.application(application)
				.tenant(currentTenant)
				.build();

    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTenantIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(null, application, healthAlert);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingHealthAlertTenantNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(otherTenant, application, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingHealthAlertAppIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, null, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertAppNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, otherApplication, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, null);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertDeviceGuidNullOrEmpty() {
    	healthAlert.setDevice(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.DEVICE_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertDeviceNotExists() {
    	healthAlert.setDevice(deviceNoExists);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertTriggerGuidNullOrEmpty() {
    	healthAlert.setAlertTrigger(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertTriggerNotExists() {
    	healthAlert.setAlertTrigger(triggerNoExists);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldSaveHealthAlert() {
        ServiceResponse<HealthAlert> response = healthAlertService.register(currentTenant, application, newHealthAlert);

        assertThat(response, isResponseOk());
    }

    /****************** update ******************/

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertTenantIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
        		null,
        		application,
        		"7d51c242-81db-11e6-a8c2-0746f976f224",
        		healthAlert);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingHealthAlertTenantNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			otherTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingHealthAlertAppIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			null,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertAppNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			otherApplication,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertDeviceGuidNullOrEmpty() {
    	healthAlert.setDevice(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			healthAlert.getGuid(),
    			healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceEventService.Validations.DEVICE_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertDeviceNotExists() {
    	healthAlert.setDevice(deviceNoExists);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			healthAlert.getGuid(),
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid(),
    			newHealthAlert);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldTryUpdateHealthAlertWithNullSeverity() {
        tempHealthAlert.setSeverity(null);

        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
                application.getTenant(),
                application,
                tempHealthAlert.getGuid(),
                tempHealthAlert);

        assertThat(serviceResponse, hasErrorMessage(HealthAlert.Validations.SEVERITY_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldUpdateHealthAlert() {
    	tempHealthAlert.setDescription("Health of device is ok.");
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			application.getTenant(),
    			application,
    			tempHealthAlert.getGuid(),
    			tempHealthAlert);

        assertThat(serviceResponse, isResponseOk());
        assertThat(serviceResponse.getResult().getDescription(), equalTo(tempHealthAlert.getDescription()));
    }

    /****************** remove ******************/

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertTenantIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
        		null,
        		application,
        		healthAlert.getGuid(),
        		Solution.ALERT_DELETED
                );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertAppIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
        		currentTenant,
        		null,
        		healthAlert.getGuid(),
                Solution.ALERT_DELETED);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertGuidNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			null,
                Solution.ALERT_DELETED);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfRemovingHealthAlertNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    	        currentTenant,
    	        application,
    	        otherApplication.getName(),
                Solution.ALERT_DELETED
    	        );

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldRemoveHealthAlert() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			tempHealthAlert.getGuid(),
                Solution.ALERT_DELETED);

    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResult().isSolved(), equalTo(true));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnAllHealthAlert() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantAndApplication(currentTenant, application);

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), notNullValue());
    	assertThat(response.getResult(), hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantNullAppDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			null,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantAppNullDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantAppDeviceGuidNull() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnHealthNotExistFindAllHealthAlertByTenantAppDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			"8363c556-84ea-11e6-92a2-4b01fea7e243");

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnHealthNotExistGuidFindAllHealthAlertByTenantAppDeviceGuid() {
        ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
                currentTenant,
                application,
                "7d51c242-81db-11e6-a8c2-0746f010e949");

        assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldFindAllHealthAlertByTenantAppDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidTenantNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			null,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorGetHealthAlertByGuidAppNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			null,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidTenantNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			otherTenant,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidAppNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			otherApplication,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidHealthAlertNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid());

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldGetHealthAlertByGuid() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(healthAlert));
    }

    ////////////////// removeAlertsFromTrigger //////////////////

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidTenantIsNull() {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                null,
                application,
                trigger);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidAppIsNull() {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                null,
                trigger);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidGuidNull() {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                null);

        assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldRemovingHealthAlertByTriggerGuidGuidNull() {

        List<HealthAlert> healthAlerts = healthAlertService.findAllByTenantAndApplication(currentTenant, application).getResult();
        assertThat(healthAlerts.size(), equalTo(2));
        assertThat(healthAlerts.get(0).getDevice(), equalTo(device));
        assertThat(healthAlerts.get(1).getDevice(), equalTo(device));

        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                trigger);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));

        healthAlerts = healthAlertService.findAllByTenantAndApplication(currentTenant, application).getResult();
        assertThat(healthAlerts.size(), equalTo(0));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnTenantNullGetLastHightSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			null,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnAppNullGetLastHightSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnDeviceNullGetLastHightSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldGetLastHightSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(tempHealthAlert));
    }

    /****************** findAllByTenantApplicationAndTrigger ******************/

	@Test
	@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
	public void shouldGetFindAllByTenantApplicationAndTriggerGuid() {
		ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndTrigger(
				currentTenant,
				application,
				trigger);

		assertThat(response, isResponseOk());
        assertThat(response.getResult().size(), is(2));
		assertThat(response.getResult().get(0), equalTo(healthAlert));
	}

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldTryGetFindAllByTenantApplicationAndNullTriggerGuid() {
        ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndTrigger(
                currentTenant,
                application,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    /****************** findByTenantApplicationTriggerAndAlertId ******************/

	@Test
	@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
	public void shouldTryFindByTenantApplicationTriggerAndNullAlertId() {
		ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
				currentTenant,
				application,
                trigger,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_NULL_ID.getCode()));
	}

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldTryFindByTenantApplicationNullTriggerAndAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                null,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldFindByTenantApplicationTriggerAndAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                trigger,
                healthAlert.getAlertId());

        assertThat(response, isResponseOk());
        assertThat(response.getResult(), equalTo(healthAlert));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldTryFindByTenantApplicationTriggerAndNonExistingAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                trigger,
                "non-existing-alert-id");

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

}