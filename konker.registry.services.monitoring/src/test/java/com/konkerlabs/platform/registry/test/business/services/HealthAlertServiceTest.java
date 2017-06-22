package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertType;
import com.konkerlabs.platform.registry.business.model.SilenceTrigger;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Messages;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.SpringMailTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
        EmailConfig.class})
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

    private HealthAlert healthAlert;
    private HealthAlert tempHealthAlert;
    private HealthAlert newHealthAlert;
    private Application application;
    private Application otherApplication;
    private Tenant currentTenant;
    private Tenant otherTenant;

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

    	healthAlert = HealthAlert.builder()
    			.id("67014de6-81db-11e6-a5bc-3f99b78823c9")
    			.guid("7d51c242-81db-11e6-a8c2-0746f976f224")
    			.description("Device sem enviar mensagem por mais de 10 minutos")
    			.severity(HealthAlertSeverity.WARN)
    			.type(HealthAlertType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747l))
    			.deviceGuid(DEVICE_GUID)
    			.triggerGuid(TRIGGER_GUID)
    			.application(application)
    			.tenant(currentTenant)
    			.build();

    	tempHealthAlert = HealthAlert.builder()
    			.id("67014de6-81db-11e6-a5bc-3f99b788249c")
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
				.description("Device sem enviar mensagem por mais de 5 minutos")
    			.severity(HealthAlertSeverity.FAIL)
    			.type(HealthAlertType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747l))
    			.deviceGuid(DEVICE_GUID)
    			.triggerGuid(TRIGGER_GUID)
    			.application(application)
				.tenant(currentTenant)
				.build();

    	newHealthAlert = HealthAlert.builder()
    			.guid("7d51c242-81db-11e6-a8c2-0746f976f225")
    			.description("Device estabilizado")
    			.severity(HealthAlertSeverity.OK)
    			.type(HealthAlertType.SILENCE)
    			.registrationDate(Instant.ofEpochMilli(1453320973747l))
    			.deviceGuid(DEVICE_GUID)
    			.triggerGuid(TRIGGER_GUID)
				.application(application)
				.tenant(currentTenant)
				.build();
    	
    	SilenceTrigger trigger = new SilenceTrigger();
    	trigger.setGuid(TRIGGER_GUID);
    	trigger.setApplication(application);
    	trigger.setTenant(currentTenant);
    	trigger.setMinutes(1);
    	trigger.setType(HealthAlertType.SILENCE);
    	trigger = alertTriggerRepository.save(trigger);

    }

    @Test
    public void shouldReturnErrorIfSavingHealthAlertTenantIsNull() throws Exception {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(null, application, healthAlert);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingHealthAlertTenantNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(otherTenant, application, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingHealthAlertAppIsNull() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, null, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertAppNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, otherApplication, healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertIsNull() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingHealthAlertDeviceGuidNullOrEmpty() throws Exception {
    	healthAlert.setDeviceGuid(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));

    	healthAlert.setDeviceGuid("");
    	serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertDeviceNotExists() throws Exception {
    	healthAlert.setDeviceGuid("7d51c242-81db-11e6-a8c2-0746f010e911");
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertTriggerGuidNullOrEmpty() throws Exception {
    	healthAlert.setTriggerGuid(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_GUID_NULL.getCode()));
    	
    	healthAlert.setTriggerGuid("");
    	serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_GUID_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfSavingHealthAlertTriggerNotExists() throws Exception {
    	healthAlert.setTriggerGuid("7d51c242-81db-11e6-a8c2-0746f010e911");
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.register(currentTenant, application, healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_TRIGGER_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json", "/fixtures/health-alert.json"})
    public void shouldSaveHealthAlert() throws Exception {
        ServiceResponse<HealthAlert> response = healthAlertService.register(currentTenant, application, newHealthAlert);

        assertThat(response, isResponseOk());
    }

    @Test
    public void shouldReturnErrorIfUpdatingHealthAlertTenantIsNull() throws Exception {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
        		null,
        		application,
        		"7d51c242-81db-11e6-a8c2-0746f976f224",
        		healthAlert);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingHealthAlertTenantNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			otherTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingHealthAlertAppIsNull() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			null,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertAppNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			otherApplication,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertIsNull() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f976f224",
    			null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertDeviceGuidNullOrEmpty() throws Exception {
    	healthAlert.setDeviceGuid(null);
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			healthAlert.getGuid(),
    			healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));

    	healthAlert.setDeviceGuid("");
    	serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			healthAlert.getGuid(),
    			healthAlert);
    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertDeviceNotExists() throws Exception {
    	healthAlert.setDeviceGuid("7d51c242-81db-11e6-a8c2-0746f010e911");
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			healthAlert.getGuid(),
    			healthAlert);

    	assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorIfUpdatingHealthAlertNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid(),
    			newHealthAlert);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json", "/fixtures/health-alert.json"})
    public void shouldUpdateHealthAlert() throws Exception {
    	tempHealthAlert.setDescription("Updating description");
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.update(
    			application.getTenant(),
    			application,
    			tempHealthAlert.getGuid(),
    			tempHealthAlert);

        assertThat(serviceResponse, isResponseOk());
        assertThat(serviceResponse.getResult().getDescription(), equalTo(tempHealthAlert.getDescription()));
    }

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertTenantIsNull() throws Exception {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
        		null,
        		application,
        		healthAlert.getGuid());

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertAppIsNull() throws Exception {
        ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
        		currentTenant,
        		null,
        		healthAlert.getGuid());

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertGuidNull() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorIfRemovingHealthAlertNotExists() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(currentTenant, application, otherApplication.getName());

    	assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldRemoveHealthAlert() throws Exception {
    	ServiceResponse<HealthAlert> serviceResponse = healthAlertService.remove(
    			currentTenant,
    			application,
    			tempHealthAlert.getGuid());

    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResult().isSolved(), equalTo(true));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnAllHealthAlert() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantAndApplication(currentTenant, application);

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), notNullValue());
    	assertThat(response.getResult(), hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantNullAppDeviceGuid() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			null,
    			application,
    			healthAlert.getDeviceGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantAppNullDeviceGuid() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDeviceGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorFindAllHealthAlertByTenantAppDeviceGuidNull() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnHealthNotExistFindAllHealthAlertByTenantAppDeviceGuid() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			"7d51c242-81db-11e6-a8c2-0746f010e949");

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json", "/fixtures/devices.json"})
    public void shouldFindAllHealthAlertByTenantAppDeviceGuid() throws Exception {
    	ServiceResponse<List<HealthAlert>> response = healthAlertService.findAllByTenantApplicationAndDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDeviceGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorGetHealthAlertByGuidTenantNull() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			null,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorGetHealthAlertByGuidAppNull() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			null,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidNull() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidTenantNotExists() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			otherTenant,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetHealthAlertByGuidAppNotExists() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			otherApplication,
    			healthAlert.getGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnErrorGetHealthAlertByGuidHealthAlertNotExists() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			newHealthAlert.getGuid());

    	assertThat(response, hasErrorMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldGetHealthAlertByGuid() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getByTenantApplicationAndHealthAlertGuid(
    			currentTenant,
    			application,
    			healthAlert.getGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(healthAlert));
    }

    ////////////////// removeAlertsFromTrigger //////////////////

    @Test
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidTenantIsNull() throws Exception {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                null,
                application,
                healthAlert.getGuid());

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidAppIsNull() throws Exception {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                null,
                healthAlert.getGuid());

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingHealthAlertByTriggerGuidGuidNull() throws Exception {
        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                null);

        assertThat(serviceResponse, hasErrorMessage(Validations.HEALTH_ALERT_GUID_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/devices.json"})
    public void shouldRemovingHealthAlertByTriggerGuidGuidNull() throws Exception {

        SilenceTrigger alertTrigger = new SilenceTrigger();
        alertTrigger.setTenant(currentTenant);
        alertTrigger.setApplication(application);
        alertTrigger.setGuid("37b8de6c-9f12-40f1-9689-4aded8867f82");
        alertTriggerRepository.save(alertTrigger);

        healthAlert.setTriggerGuid(alertTrigger.getGuid());
        tempHealthAlert.setTriggerGuid(alertTrigger.getGuid());

        healthAlertService.register(currentTenant, application, healthAlert);
        healthAlertService.register(currentTenant, application, tempHealthAlert);

        List<HealthAlert> healthAlerts = healthAlertService.findAllByTenantAndApplication(currentTenant, application).getResult();
        assertThat(healthAlerts.size(), equalTo(2));

        ServiceResponse<List<HealthAlert>> serviceResponse = healthAlertService.removeAlertsFromTrigger(
                currentTenant,
                application,
                alertTrigger.getGuid());

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.HEALTH_ALERT_REMOVED_SUCCESSFULLY.getCode(), null));

        healthAlerts = healthAlertService.findAllByTenantAndApplication(currentTenant, application).getResult();
        assertThat(healthAlerts.size(), equalTo(0));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnTenantNullGetLastHightSeverity() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHightServerityByDeviceGuid(
    			null,
    			application,
    			healthAlert.getDeviceGuid());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json"})
    public void shouldReturnAppNullGetLastHightSeverity() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHightServerityByDeviceGuid(
    			currentTenant,
    			null,
    			healthAlert.getDeviceGuid());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnDeviceNullGetLastHightSeverity() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHightServerityByDeviceGuid(
    			currentTenant,
    			application,
    			null);

    	assertThat(response, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/health-alert.json", "/fixtures/devices.json"})
    public void shouldGetLastHightSeverity() throws Exception {
    	ServiceResponse<HealthAlert> response = healthAlertService.getLastHightServerityByDeviceGuid(
    			currentTenant,
    			application,
    			healthAlert.getDeviceGuid());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(tempHealthAlert));
    }

}