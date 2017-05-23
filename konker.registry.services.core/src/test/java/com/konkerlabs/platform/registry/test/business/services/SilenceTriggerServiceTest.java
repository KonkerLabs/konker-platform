package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
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
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService.Messages;
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService.Validations;
import com.konkerlabs.platform.registry.business.services.api.SilenceTriggerService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
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

    private SilenceTrigger trigger;

    private DeviceModel deviceModel;
    private Location locationA;
    private Location locationB;
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

    	trigger = new SilenceTrigger();
    	trigger.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
    	trigger.setTenant(currentTenant);
    	trigger.setApplication(application);
    	trigger.setDeviceModel(deviceModel);
    	trigger.setLocation(locationA);
        trigger.setMinutes(100);
        trigger = silenceTriggerRepository.save(trigger);

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {
        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(null, application, trigger);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {
        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, null, trigger);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }


    @Test
    public void shouldReturnErrorIfSavingWithInvalidMinutes() throws Exception {
        trigger.setMinutes(1);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, trigger);

        assertThat(serviceResponse, hasErrorMessage(SilenceTrigger.Validations.INVALID_MINUTES_VALUE.getCode()));
    }

    @Test
    public void shouldSaveSilenceTrigger() throws Exception {

        trigger = new SilenceTrigger();
        trigger.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        trigger.setTenant(currentTenant);
        trigger.setApplication(application);
        trigger.setDeviceModel(deviceModel);
        trigger.setLocation(locationB);
        trigger.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, trigger);

        assertThat(serviceResponse.isOk(), is(true));
    }

    @Test
    public void shouldTryToSaveExistingSilenceTrigger() throws Exception {

        trigger = new SilenceTrigger();
        trigger.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        trigger.setTenant(currentTenant);
        trigger.setApplication(application);
        trigger.setDeviceModel(deviceModel);
        trigger.setLocation(locationB);
        trigger.setMinutes(100);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.save(currentTenant, application, trigger);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = silenceTriggerService.save(currentTenant, application, trigger);
        assertThat(serviceResponse, hasErrorMessage(Validations.SILENCE_TRIGGER_ALREADY_EXISTS.getCode()));
    }

    @Test
    public void shouldUpdateSilenceTrigger() throws Exception {

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), trigger.getGuid()).getMinutes(), is(100));

        SilenceTrigger updating = new SilenceTrigger();
        updating.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        updating.setTenant(currentTenant);
        updating.setApplication(application);
        updating.setDeviceModel(deviceModel);
        updating.setLocation(locationB);
        updating.setMinutes(500);

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.update(currentTenant, application, trigger.getGuid(), updating);
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), trigger.getGuid()).getMinutes(), is(500));

    }

    @Test
    public void shouldRemoveSilenceTrigger() throws Exception {

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), trigger.getGuid()), notNullValue());

        ServiceResponse<SilenceTrigger> serviceResponse = silenceTriggerService.remove(currentTenant, application, trigger.getGuid());
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(silenceTriggerRepository.findByTenantIdApplicationIdAndGuid(currentTenant.getId(), application.getName(), trigger.getGuid()), nullValue());

    }

}