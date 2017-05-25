package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.SilenceTrigger;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.SilenceTriggerRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService;
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService.Validations;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.HealthAlertsConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
		PubServerConfig.class,
		HealthAlertsConfig.class,
        EventStorageConfig.class})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json", "/fixtures/locations.json"})
public class SilenceTriggerServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private SilenceTriggerService silenceTriggerService;

    @Autowired
    private SilenceTriggerRepository silenceTriggerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private LocationRepository locationRepository;

    private SilenceTrigger triggerA;
    private SilenceTrigger triggerC;

    private DeviceModel deviceModel;
    private Location locationA;
    private Location locationB;
    private Location locationC;
    private Application application;
    private Tenant currentTenant;

    @Before
    public void setUp() {
    	currentTenant = tenantRepository.findByName("Konker");

    	application = Application.builder()
    					.name("smartffkonker")
    					.friendlyName("Konker Smart Frig")
    					.description("Konker Smart Frig - take pic, tells temperatue")
    					.tenant(currentTenant)
                        .qualifier("konker")
                        .registrationDate(Instant.ofEpochMilli(1453320973747L))
                        .build();

    	deviceModel = DeviceModel.builder()
    					.guid("7d51c242-81db-11e6-a8c2-0746f908e887")
		    			.name("SmartFF")
		    			.description("Smart ff model")
    					.application(application)
    					.defaultModel(true)
    					.tenant(currentTenant)
    					.build();
    	deviceModel = deviceModelRepository.save(deviceModel);

    	locationA = Location.builder()
                        .guid("3bc07c9e-eb48-4c92-97a8-d9c662d1bfcd")
                        .name("BR")
                        .description("Brazil")
                        .application(application)
                        .defaultLocation(true)
                        .tenant(currentTenant)
                        .build();
    	locationA = locationRepository.save(locationA);

        locationB = Location.builder()
                        .guid("13efd64e-13dc-4401-99bf-8ae98629aeca")
                        .name("AR")
                        .description("Argentina")
                        .application(application)
                        .defaultLocation(false)
                        .tenant(currentTenant)
                        .build();
        locationB = locationRepository.save(locationB);

        locationC = Location.builder()
                        .guid("b9cc9543-9230-4c63-a3bf-aaa1e47ffcf4")
                        .name("CL")
                        .description("Chile")
                        .application(application)
                        .defaultLocation(false)
                        .tenant(currentTenant)
                        .build();
        locationC = locationRepository.save(locationC);

    	triggerA = new SilenceTrigger();
    	triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
    	triggerA.setTenant(currentTenant);
    	triggerA.setApplication(application);
    	triggerA.setDeviceModel(deviceModel);
    	triggerA.setLocation(locationA);
        triggerA.setMinutes(100);
        triggerA = silenceTriggerRepository.save(triggerA);

        triggerC = new SilenceTrigger();
        triggerC.setGuid("a702273d-dfca-4ca7-b61b-ed4f7b4cfb8e");
        triggerC.setTenant(currentTenant);
        triggerC.setApplication(application);
        triggerC.setDeviceModel(deviceModel);
        triggerC.setLocation(locationC);
        triggerC.setMinutes(200);
        triggerC = silenceTriggerRepository.save(triggerC);

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {
        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(null, application, triggerA);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {
        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, null, triggerA);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }


    @Test
    public void shouldReturnErrorIfSavingWithInvalidMinutes() throws Exception {
        triggerA.setMinutes(1);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);

        assertThat(serviceResponse, hasErrorMessage(SilenceTrigger.Validations.INVALID_MINUTES_VALUE.getCode()));
    }

    @Test
    public void shouldSaveSilenceTrigger() throws Exception {

        triggerA = new SilenceTrigger();
        triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        triggerA.setTenant(currentTenant);
        triggerA.setApplication(application);
        triggerA.setDeviceModel(deviceModel);
        triggerA.setLocation(locationB);
        triggerA.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);

        assertThat(serviceResponse.isOk(), is(true));
    }

    @Test
    public void shouldTrySaveSilenceTriggerWithoutDeviceModel() throws Exception {

        triggerA = new SilenceTrigger();
        triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        triggerA.setTenant(currentTenant);
        triggerA.setApplication(application);
        triggerA.setDeviceModel(null);
        triggerA.setLocation(locationB);
        triggerA.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);
        assertThat(serviceResponse, hasErrorMessage(DeviceModelService.Validations.DEVICE_MODEL_NULL.getCode()));

    }

    @Test
    public void shouldTrySaveSilenceTriggerWithoutLocation() throws Exception {

        triggerA = new SilenceTrigger();
        triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        triggerA.setTenant(currentTenant);
        triggerA.setApplication(application);
        triggerA.setDeviceModel(deviceModel);
        triggerA.setLocation(null);
        triggerA.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);
        assertThat(serviceResponse, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryToSaveExistingSilenceTrigger() throws Exception {

        triggerA = new SilenceTrigger();
        triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        triggerA.setTenant(currentTenant);
        triggerA.setApplication(application);
        triggerA.setDeviceModel(deviceModel);
        triggerA.setLocation(locationB);
        triggerA.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = silenceTriggerService.save(currentTenant, application, triggerA);
        assertThat(serviceResponse, hasErrorMessage(Validations.SILENCE_TRIGGER_ALREADY_EXISTS.getCode()));
    }

    @Test
    public void shouldUpdateSilenceTrigger() throws Exception {

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), triggerA.getGuid()).getMinutes(), is(100));

        SilenceTrigger updating = new SilenceTrigger();
        updating.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        updating.setTenant(currentTenant);
        updating.setApplication(application);
        updating.setDeviceModel(deviceModel);
        updating.setLocation(locationB);
        updating.setMinutes(500);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.update(currentTenant, application, triggerA.getGuid(), updating);
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), triggerA.getGuid()).getMinutes(), is(500));

    }

    @Test
    public void shouldTryUpdateInvalidMinutesSilenceTrigger() throws Exception {

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), triggerA.getGuid()).getMinutes(), is(100));

        SilenceTrigger updating = new SilenceTrigger();
        updating.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        updating.setTenant(currentTenant);
        updating.setApplication(application);
        updating.setDeviceModel(deviceModel);
        updating.setLocation(locationB);
        updating.setMinutes(5);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.update(currentTenant, application, triggerA.getGuid(), updating);
        assertThat(serviceResponse, hasErrorMessage(SilenceTrigger.Validations.INVALID_MINUTES_VALUE.getCode()));

    }


    @Test
    public void shouldTryUpdateNonExistingSilenceTrigger() throws Exception {

        SilenceTrigger updating = new SilenceTrigger();
        updating.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        updating.setTenant(currentTenant);
        updating.setApplication(application);
        updating.setDeviceModel(deviceModel);
        updating.setLocation(locationB);
        updating.setMinutes(500);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.update(currentTenant, application, "4f2d1a19-dedc-4e96-b252-fa5f5682914c", updating);
        assertThat(serviceResponse, hasErrorMessage(Validations.SILENCE_TRIGGER_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldRemoveSilenceTrigger() throws Exception {

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), triggerA.getGuid()), notNullValue());

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.remove(currentTenant, application, triggerA.getGuid());
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), triggerA.getGuid()), nullValue());

    }

    @Test
    public void shouldTryToRemoveNonExistingSilenceTrigger() throws Exception {

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.remove(currentTenant, application, "4f2d1a19-dedc-4e96-b252-fa5f5682914c");
        assertThat(serviceResponse, hasErrorMessage(Validations.SILENCE_TRIGGER_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldFindByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(currentTenant, application, deviceModel, locationA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getMinutes(), is(100));

    }

    @Test
    public void shouldFindNonExistingByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.findByTenantAndApplicationAndModelAndLocation(currentTenant, application, deviceModel, locationB);
        assertThat(serviceResponse, hasErrorMessage(Validations.SILENCE_TRIGGER_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldListByTenantAndApplication() throws Exception {

        ServiceResponse<List<SilenceTrigger>> serviceResponse = silenceTriggerService.listByTenantAndApplication(currentTenant, application);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().size(), is(2));
        assertThat(serviceResponse.getResult().get(0).getLocation().getName(), is("BR"));
        assertThat(serviceResponse.getResult().get(1).getLocation().getName(), is("CL"));

    }

}