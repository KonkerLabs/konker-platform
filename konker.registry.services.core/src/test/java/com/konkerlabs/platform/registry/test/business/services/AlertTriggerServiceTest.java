package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;

import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.test.base.*;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.AlertTriggerRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.business.services.api.AlertTriggerService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        MongoBillingTestConfiguration.class,
        EmailConfig.class,
        SpringMailTestConfiguration.class,
        BusinessTestConfiguration.class,
		PubServerConfig.class,
        EventStorageConfig.class})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json", "/fixtures/locations.json"})
public class AlertTriggerServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private AlertTriggerService alertTriggerService;

    @Autowired
    private AlertTriggerRepository alertTriggerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private LocationRepository locationRepository;

    private AlertTrigger triggerA;
    private AlertTrigger triggerB;

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
                        .guid("b9cc9543-9230-4c63-a3bf-aaa1e47ffcf4")
                        .name("CL")
                        .description("Chile")
                        .application(application)
                        .defaultLocation(false)
                        .tenant(currentTenant)
                        .build();
        locationB = locationRepository.save(locationB);

    	triggerA = AlertTrigger.builder().build();
    	triggerA.setGuid("95a79b96-6193-4d13-a85e-8bafc3a44837");
        triggerA.setName("silence a");
    	triggerA.setTenant(currentTenant);
    	triggerA.setApplication(application);
    	triggerA.setDeviceModel(deviceModel);
    	triggerA.setLocation(locationA);
    	triggerA.setType(AlertTrigger.AlertTriggerType.SILENCE);
        triggerA.setMinutes(100);
        triggerA = alertTriggerRepository.save(triggerA);

        triggerB = AlertTrigger.builder().build();
        triggerB.setGuid("a702273d-dfca-4ca7-b61b-ed4f7b4cfb8e");
        triggerB.setName("silence b");
        triggerB.setTenant(currentTenant);
        triggerB.setApplication(application);
        triggerB.setDeviceModel(deviceModel);
        triggerB.setLocation(locationB);
        triggerB.setType(AlertTrigger.AlertTriggerType.SILENCE);
        triggerB.setMinutes(200);
        triggerB = alertTriggerRepository.save(triggerB);

    }

    @Test
    public void shouldListByTenantAndApplication() throws Exception {

        ServiceResponse<List<AlertTrigger>> serviceResponse = alertTriggerService.listByTenantAndApplication(currentTenant, application);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().size(), is(2));
        assertThat(serviceResponse.getResult().get(0).getLocation().getName(), is("BR"));
        assertThat(serviceResponse.getResult().get(0).getType(), is(AlertTrigger.AlertTriggerType.SILENCE));

        assertThat(serviceResponse.getResult().get(1).getLocation().getName(), is("CL"));
        assertThat(serviceResponse.getResult().get(1).getType(), is(AlertTrigger.AlertTriggerType.SILENCE));

    }

    @Test
    public void shouldTryListByTenantAndApplicationWithNullTenant() throws Exception {

        ServiceResponse<List<AlertTrigger>> serviceResponse = alertTriggerService.listByTenantAndApplication(null, application);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryListByTenantAndApplicationWithNullApplication() throws Exception {

        ServiceResponse<List<AlertTrigger>> serviceResponse = alertTriggerService.listByTenantAndApplication(currentTenant, null);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldFindByTenantAndApplicationAndGuid() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.findByTenantAndApplicationAndGuid(currentTenant, application, triggerA.getGuid());
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getGuid(), is(triggerA.getGuid()));

    }

    @Test
    public void shouldTryFindByTenantAndApplicationAndGuidNonExistingTrigger() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.findByTenantAndApplicationAndGuid(currentTenant, application, "000-000");
        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()));

    }

    /***************************** findByTenantAndApplicationAndName *****************************/

    @Test
    public void shouldTryFindByTenantAndApplicationAndNameNullApplication() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                null,
                triggerA.getGuid(),
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldFindByTenantAndApplicationAndName() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.findByTenantAndApplicationAndName(
                currentTenant,
                application,
                triggerA.getName()
        );

        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getGuid(), is(triggerA.getGuid()));

    }

    @Test
    public void shouldTryFindNonExistingByTenantAndApplicationAndName() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.findByTenantAndApplicationAndName(
                currentTenant,
                application,
                "invalid name"
        );

        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()));

    }

    /***************************** remove *****************************/

    @Test
    public void shouldTryRemoveWithNullGuid() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.remove(
                currentTenant,
                application,
                null
        );

        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryRemoveWithInvalidGuid() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.remove(
                currentTenant,
                application,
                "invalid-guid"
        );

        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()));

    }


    @Test
    public void shouldRemove() throws Exception {

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.remove(
                currentTenant,
                application,
                triggerA.getGuid()
        );

        assertThat(serviceResponse.isOk(), is(true));

    }

    /***************************** update *****************************/

    @Test
    public void shouldTryUpdateWithoutTenant() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                null,
                application,
                triggerA.getGuid(),
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullGuid() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger.
                builder().
                build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                application,
                null,
                alertTrigger
        );
        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidGuid() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                application,
                "invalid-guid",
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidName() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger.
                builder().
                build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                application,
                "invalid-guid",
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(AlertTrigger.Validations.NAME_INVALID.getCode()));

    }

    @Test
    public void shouldUpdateWithInvalidMinutes() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence 42")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                application,
                triggerA.getGuid(),
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(AlertTrigger.Validations.INVALID_MINUTES_VALUE.getCode()));

    }

    @Test
    public void shouldUpdate() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence 42")
                .description("forty two")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .minutes(42)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.update(
                currentTenant,
                application,
                triggerA.getGuid(),
                alertTrigger
        );

        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getDescription(), is("forty two"));
        assertThat(serviceResponse.getResult().getMinutes(), is(42));

    }

    /***************************** save *****************************/

    @Test
    public void shouldTrySaveWithoutTenant() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.save(
                null,
                application,
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTrySaveSilenceWithInvalidMinutes() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.save(
                currentTenant,
                application,
                alertTrigger
                );

        assertThat(serviceResponse, hasErrorMessage(AlertTrigger.Validations.INVALID_MINUTES_VALUE.getCode()));

    }

    @Test
    public void shouldTrySaveWithExistingName() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name(triggerA.getName())
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .minutes(200)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.save(
                currentTenant,
                application,
                alertTrigger
        );

        assertThat(serviceResponse, hasErrorMessage(Validations.ALERT_TRIGGER_ALREADY_EXISTS.getCode()));

    }

    @Test
    public void shouldSave() throws Exception {

        AlertTrigger alertTrigger = AlertTrigger
                .builder()
                .name("silence")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .minutes(200)
                .build();

        ServiceResponse<AlertTrigger> serviceResponse = alertTriggerService.save(
                currentTenant,
                application,
                alertTrigger
        );

        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getGuid(), Matchers.notNullValue());

    }

}