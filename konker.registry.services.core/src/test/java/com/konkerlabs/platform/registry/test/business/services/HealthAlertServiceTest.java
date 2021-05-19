package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.Solution;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Messages;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        //EventRepositoryTestConfiguration.class,
        HealthAlertServiceTest.HealthAlertServiceTestConfig.class,
		PubServerConfig.class,
        EventStorageConfig.class,
        SpringMailTestConfiguration.class,
        EmailConfig.class,
        MongoBillingTestConfiguration.class,
        MessageSouceTestConfiguration.class})
@UsingDataSet(locations = {"/fixtures/tenants.json",
        "/fixtures/applications.json",
        "/fixtures/devices.json",
        "/fixtures/locations.json",
        "/fixtures/healthAlerts.json",
        "/fixtures/alertTriggers.json"})
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
    private ApplicationRepository applicationRepository;

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

	@Autowired
	private DeviceRepository deviceRepository;

    @Autowired
    private LocationRepository locationRepository;

    private AlertTrigger trigger;
    private AlertTrigger triggerNoExists;
    private AlertTrigger triggerToRemove;
    private HealthAlert healthAlert;
    private HealthAlert tempHealthAlert;
    private HealthAlert newHealthAlert;
    private HealthAlert savedHealth;
    private Application application;
    private Application otherApplication;
    private Tenant currentTenant;
    private Tenant otherTenant;
    private Device device;
    private Device deviceNoExists;
    private Location location;

    @Before
    public void setUp() {
    	currentTenant = tenantRepository.findByDomainName("konker");
    	otherTenant = Tenant.builder()
    			.id("71fb0d48-674b-4f64-a3e5-0256ff3a0000")
    			.name("MyCompany")
    			.domainName("MyCompany")
    			.build();

        application = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        otherApplication = Application.builder()
                .name("smartffkonkerother")
                .friendlyName("Konker Smart Frig")
                .description("Konker Smart Frig - take pic, tells temperature")
                .tenant(currentTenant)
                .qualifier("konker")
                .registrationDate(Instant.ofEpochMilli(1453320973747L))
                .build();

        location = locationRepository.findByTenantAndApplicationAndName(currentTenant.getId(), application.getName(), "br");

    	device = Device
					.builder()
					.tenant(currentTenant)
					.application(application)
                    .location(location)
					.guid("1fbbecc7-8566-40c4-8f7b-830287535970")
					.deviceId("1fbbecc7")
                    .name("1fbbecc7")
                    .active(true)
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
        trigger.setMappedLocations(new HashSet<>());
        trigger = alertTriggerRepository.save(trigger);

        triggerNoExists = AlertTrigger
                .builder()
                .guid("2d3f52c4-0a9e-4d30-ba4e-2048e0867faa")
                .build();

        triggerToRemove = alertTriggerRepository.findByTenantIdAndApplicationNameAndGuid(currentTenant.getId(),
                application.getName(),
                "dfe8ab59-98e3-4da3-9e4c-56cee35136d7");

        healthAlert = HealthAlert.builder()
    			.id("67014de6-81db-11e6-a5bc-3f99b78823c9")
                .alertId("silence-id")
    			.guid("7d51c242-81db-11e6-a8c2-0746f976f224")
                .alertTrigger(trigger)
    			.description("No message received from the device for a long time.")
    			.severity(HealthAlertSeverity.OK)
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
    			.severity(HealthAlertSeverity.WARN)
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
    			.registrationDate(Instant.ofEpochMilli(1453320973747L))
                .device(device)
                .alertTrigger(trigger)
				.application(application)
				.tenant(currentTenant)
				.build();

        savedHealth = healthAlertRepository.findByTenantIdApplicationNameAndGuid(currentTenant.getId(),
                application.getName(),
                "c77c0fdf-1067-4e6c-88ed-91b524e15f60");

    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTenantIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(null, application, healthAlert);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTenantNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(otherTenant, application, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertAppIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, null, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertAppNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, otherApplication, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, null);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertDeviceGuidNullOrEmpty() {
    	healthAlert.setDevice(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DEVICE_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertDeviceNotExists() {
    	healthAlert.setDevice(deviceNoExists);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTriggerGuidNullOrEmpty() {
    	healthAlert.setAlertTrigger(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTriggerNotExists() {
    	healthAlert.setAlertTrigger(triggerNoExists);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldSaveHealthAlertWithSeverityOK() {
        ServiceResponse<HealthAlert> response = healthAlertService.register(currentTenant, application, newHealthAlert);
        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_WITH_STATUS_OK.getCode()));
    }

    @Test
    public void shouldSaveHealthAlertWithSeverityWarn() {
        newHealthAlert.setSeverity(HealthAlertSeverity.WARN);
        ServiceResponse<HealthAlert> response = healthAlertService.register(currentTenant, application, newHealthAlert);

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getSeverity(), is(HealthAlertSeverity.WARN));

        response = healthAlertService.findByTenantApplicationTriggerAndAlertId(currentTenant, application, newHealthAlert.getAlertTrigger(), newHealthAlert.getAlertId());
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getSeverity(), is(HealthAlertSeverity.WARN));

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
    public void shouldReturnErrorIfUpdatingHealthAlertTenantNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			otherTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertAppIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			null,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertAppNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			otherApplication,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertIsNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertNotExists() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid(),
    			newHealthAlert);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
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
    public void shouldUpdateHealthAlertWithOk() {
        savedHealth.setDescription("Health of device is ok.");
        savedHealth.setSeverity(HealthAlertSeverity.OK);

        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
                application.getTenant(),
                application,
                savedHealth.getGuid(),
                savedHealth);

        assertThat(serviceResponse.isOk(), is(true));
        Assert.assertEquals(serviceResponse.getResult().getSolution(), Solution.MARKED_AS_RESOLVED);
        Assert.assertEquals(serviceResponse.getResult().isSolved(), true);
    }


    @Test
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
    public void shouldReturnErrorIfRemovingHealthAlertAppIsNull() {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
        		currentTenant,
        		null,
        		healthAlert.getGuid(),
                Solution.ALERT_DELETED);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertGuidNull() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			null,
                Solution.ALERT_DELETED);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
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
    public void shouldRemoveHealthAlert() {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			"c77c0fdf-1067-4e6c-88ed-91b524e15f60",
                Solution.ALERT_DELETED);

    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResult().isSolved(), equalTo(true));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    public void shouldReturnAllHealthAlert() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantAndApplication(currentTenant, application);

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), notNullValue());
    	assertThat(response.getResult(), hasSize(3));
    }

    /****************** findAllByTenantApplicationAndDeviceGuid ******************/

    @Test
    public void shouldReturnErrorFindAllHealthAlertByTenantNullAppDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			null,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorFindAllHealthAlertByTenantAppNullDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorFindAllHealthAlertByTenantAppDeviceGuidNull() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    public void shouldReturnHealthNotExistGuidFindAllHealthAlertByTenantAppDeviceGuid() {
        ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
                currentTenant,
                application,
                "7d51c242-81db-11e6-a8c2-0746f010e949");

        assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldFindAllHealthAlertByTenantAppDeviceGuid() {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), hasSize(2));
    }

    /****************** getByTenantApplicationAndHealthAlertGuid ******************/

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidTenantNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			null,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidAppNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			null,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidNull() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidTenantNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			otherTenant,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidAppNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			otherApplication,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnErrorGetHealthAlertByGuidHealthAlertNotExists() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid());

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldGetHealthAlertByGuid() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			healthAlert.getGuid());

        assertThat(response, isResponseOk());
        healthAlert.getDevice().setLocation(null);
        HealthAlert result = response.getResult();
        result.getDevice().setLocation(null);
        assertThat(result, equalTo(healthAlert));
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
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidAppIsNull() {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                null,
                trigger);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidGuidNull() {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                null);

        assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    @Test
    public void shouldRemovingHealthAlertByTriggerGuidGuidNull() {

        List<HealthAlert> healthAlerts = healthAlertService.findAllByTenantAndApplication(currentTenant, application).getResult();
        assertThat(healthAlerts.size(), equalTo(3));
        device.setLocation(null);
        healthAlerts.get(1).getDevice().setLocation(null);
        healthAlerts.get(2).getDevice().setLocation(null);
        assertThat(healthAlerts.get(1).getDevice(), equalTo(device));
        assertThat(healthAlerts.get(2).getDevice(), equalTo(device));

        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                triggerToRemove);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    public void shouldReturnTenantNullGetLastHighestSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			null,
    			application,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnAppNullGetLastHighestSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDevice().getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnDeviceNullGetLastHighestSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    public void shouldGetLastHighestSeverity() {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHighestSeverityByDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDevice().getGuid());
        assertThat(response, isResponseOk());
        tempHealthAlert.getDevice().setLocation(null);
        HealthAlert result = response.getResult();
        result.getDevice().setLocation(null);
        assertThat(result, equalTo(tempHealthAlert));
    }

    /****************** findAllByTenantApplicationAndTrigger ******************/

	@Test
	public void shouldGetFindAllByTenantApplicationAndTriggerGuid() {
		ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndTrigger(
				currentTenant,
				application,
				trigger);

        healthAlert.getDevice().setLocation(null);
		assertThat(response, isResponseOk());
        assertThat(response.getResult().size(), is(2));
        response.getResult().get(0).getDevice().setLocation(null);
		assertThat(response.getResult().get(0), equalTo(healthAlert));
	}

    @Test
    public void shouldTryGetFindAllByTenantApplicationAndNullTriggerGuid() {
        ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndTrigger(
                currentTenant,
                application,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NULL.getCode()));
    }

    /****************** findByTenantApplicationTriggerAndAlertId ******************/

	@Test
	public void shouldTryFindByTenantApplicationTriggerAndNullAlertId() {
		ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
				currentTenant,
				application,
                trigger,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_NULL_ALERT_ID.getCode()));
	}

    @Test
    public void shouldTryFindByTenantApplicationNullTriggerAndAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                null,
                null);

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    public void shouldFindByTenantApplicationTriggerAndAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                trigger,
                healthAlert.getAlertId());

        assertThat(response, isResponseOk());
        healthAlert.getDevice().setLocation(null);
        HealthAlert result = response.getResult();
        result.getDevice().setLocation(null);
        assertThat(result, equalTo(healthAlert));
    }

    @Test
    public void shouldTryFindByTenantApplicationTriggerAndNonExistingAlertId() {
        ServiceResponse<HealthAlert> response = healthAlertService.findByTenantApplicationTriggerAndAlertId(
                currentTenant,
                application,
                trigger,
                "non-existing-alert-id");

        assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    /****************** getCurrentHealthByGuid ******************/

    @Test
    public void shouldReturnTenantNullGetCurrentHealth() {
        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                null,
                application,
                healthAlert.getDevice().getGuid());

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnAppNullGetCurrentHealth() {
        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                null,
                healthAlert.getDevice().getGuid());

        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldReturnDeviceNullGetCurrentHealth() {
        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                application,
                null);

        assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    public void shouldReturnDeviceGuidNotExistNullGetCurrentHealth() {
        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                application,
                "7d51c242-81db-11e6-a8c2-0746f010e000");

        assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldReturnDeviceDisabledGetCurrentHealth() {
        device.setActive(false);
        device = deviceRepository.save(device);

        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                application,
                device.getGuid());

        assertThat(response, isResponseOk());
        assertThat(response.getResult(), equalTo(HealthAlert.builder().severity(HealthAlertSeverity.DISABLED).build()));
    }

    @Test
    public void shouldReturnDeviceNoDataGetCurrentHealth() {
        List<Event> events = Collections.emptyList();
        when(deviceEventService.findIncomingBy(currentTenant, application, healthAlert.getDevice().getGuid(), null, null, null, null, false, 1))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(events).build());

        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                application,
                healthAlert.getDevice().getGuid());

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getSeverity(), equalTo(HealthAlertSeverity.NODATA));
    }

    @Test
    public void shouldGetCurrentHealth() {
        List<Event> events = new LinkedList<>();
        events.add(Event.builder().build());

        when(deviceEventService.findIncomingBy(currentTenant, application, healthAlert.getDevice().getGuid(), null, null, null, null, false, 1))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(events).build());

        ServiceResponse<HealthAlert> response = healthAlertService.getCurrentHealthByGuid(
                currentTenant,
                application,
                healthAlert.getDevice().getGuid());

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getSeverity(), equalTo(HealthAlertSeverity.WARN));

    }

    static class HealthAlertServiceTestConfig {
        @Bean
        public DeviceEventService deviceEventService() {
            return Mockito.mock(DeviceEventService.class);
        }
    }

}
