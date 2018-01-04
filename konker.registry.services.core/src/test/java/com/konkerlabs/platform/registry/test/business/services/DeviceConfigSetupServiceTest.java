package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.rules.ExpectedException.none;

import java.time.Instant;
import java.util.ArrayList;
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
import com.konkerlabs.platform.registry.business.model.DeviceConfig;
import com.konkerlabs.platform.registry.business.model.DeviceConfigSetup;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceConfigSetupRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService.Messages;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class})
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json" })
public class DeviceConfigSetupServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private DeviceConfigSetupService subject;

    @Autowired
    private DeviceConfigSetupRepository deviceConfigSetupRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Tenant tenant;

    private Application application;
    private Application otherApplication;

    private DeviceModel deviceModel1;
    private Location locationParent;
    private Location location1;
    private Location location11;
    private Location location2;
    private Location location3;

    private DeviceConfig deviceConfig1;
    private DeviceConfig deviceConfig2;

    private String json1 = "{ 'temp' : 34 }";
    private String json2 = "{ 'code' : 'tschuss' }";

    @Before
    public void setUp() {
        tenant = tenantRepository.findByDomainName("konker");

        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        otherApplication = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        deviceModel1 = DeviceModel.builder()
                                 .tenant(tenant)
                                 .application(application)
                                 .name("air conditioner")
                                 .guid("be68c474-b961-4974-829d-daeed9e4142b")
                                 .build();
        deviceModelRepository.save(deviceModel1);

        locationParent = Location.builder()
                .tenant(tenant)
                .application(application)
                .name("house")
                .guid("26c4f3a1-9b86-447b-b5ec-711739de639b")
                .build();
        locationRepository.save(locationParent);

        location1 = Location.builder()
                            .tenant(tenant)
                            .application(application)
                            .name("room")
                            .guid("a82851c2-2319-4076-9046-c5882ce59e9d")
                            .parent(locationParent)
                            .build();
        locationRepository.save(location1);

        location11 = Location.builder()
                             .tenant(tenant)
                             .application(application)
                             .name("room-table")
                             .guid("0fc5760b-0618-4b55-a326-320309548316")
                             .parent(location1)
                             .build();
        locationRepository.save(location11);

        location2 = Location.builder()
                            .tenant(tenant)
                            .application(application)
                            .name("kitchen")
                            .guid("8ca90ff1-5fe0-459f-aa9f-338581642584")
                            .parent(locationParent)
                            .build();
        locationRepository.save(location2);

        location3 = Location.builder()
                            .tenant(tenant)
                            .application(application)
                            .name("garage")
                            .guid("e179cc28-8241-48e8-8487-2234d2b5fc9a")
                            .parent(locationParent)
                            .build();
        locationRepository.save(location3);

        deviceConfig1 = DeviceConfig.builder()
                                    .deviceModel(deviceModel1.getName())
                                    .deviceModelGuid(deviceModel1.getGuid())
                                    .locationName(location1.getName())
                                    .locationGuid(location1.getGuid())
                                    .json(json1)
                                    .build();

        deviceConfig2 = DeviceConfig.builder()
                                    .deviceModel(deviceModel1.getName())
                                    .deviceModelGuid(deviceModel1.getGuid())
                                    .locationName(location2.getName())
                                    .locationGuid(location2.getGuid())
                                    .json(json2)
                                    .build();

        List<DeviceConfig> configs = new ArrayList<>();
        configs.add(deviceConfig1);
        configs.add(deviceConfig2);

        DeviceConfigSetup configSetup0 = DeviceConfigSetup.builder()
                                                .tenant(tenant)
                                                .application(application)
                                                .date(Instant.now())
                                                .version(0)
                                                .build();
        deviceConfigSetupRepository.save(configSetup0);

        DeviceConfigSetup configSetup1 = DeviceConfigSetup.builder()
                .tenant(tenant)
                .application(application)
                .date(Instant.now())
                .version(1)
                .configs(configs)
                .build();
        deviceConfigSetupRepository.save(configSetup1);

    }

    // ============================== findAll ==============================//

    @Test
    public void shouldListFindAll() {
        ServiceResponse<List<DeviceConfig>> response = subject.findAll(tenant, application);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(2));
        assertThat(response.getResult().get(0).getJson(), is(json1));
        assertThat(response.getResult().get(1).getJson(), is(json2));
    }

    @Test
    public void shouldListFindAllWithEmptyConfig() {
        ServiceResponse<List<DeviceConfig>> response = subject.findAll(tenant, otherApplication);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(0));
    }

    @Test
    public void shouldListFindAllWithoutTenant() {
        ServiceResponse<List<DeviceConfig>> response = subject.findAll(null, application);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldListFindAllWithoutApplication() {
      ServiceResponse<List<DeviceConfig>> response = subject.findAll(tenant, null);
      assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    // ============================== findAllByModel ==============================//

    @Test
    public void shouldListFindAllByModel() {
        ServiceResponse<List<DeviceConfig>> response = subject.findAllByDeviceModel(tenant, application, deviceModel1);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(2));
        assertThat(response.getResult().get(0).getJson(), is(json1));
        assertThat(response.getResult().get(1).getJson(), is(json2));
    }

    // ============================== findAllByLocation ==============================//

    @Test
    public void shouldListFindAllByLocation() {
        ServiceResponse<List<DeviceConfig>> response = subject.findAllByLocation(tenant, application, location1);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(1));
        assertThat(response.getResult().get(0).getJson(), is(json1));
    }

    // ============================== save ==============================//

    @Test
    public void shouldSave() {
        String newJson = "{ 'valid' : true }";

        ServiceResponse<DeviceConfig> response = subject.save(tenant, application, deviceModel1, location3, newJson);
        assertThat(response, isResponseOk());

        ServiceResponse<String> json = subject.findByModelAndLocation(tenant, application, deviceModel1, location3);
        assertThat(json, isResponseOk());
        assertThat(json.getResult(), is(newJson));

        ServiceResponse<List<DeviceConfig>> responseConfigs = subject.findAll(tenant, application);
        assertThat(responseConfigs, isResponseOk());
        assertThat(responseConfigs.getResult().size(), is(3));
    }

    @Test
    public void shouldSaveExistingConfig() {
        String newJson = "{ 'valid' : true }";

        ServiceResponse<DeviceConfig> response = subject.save(tenant, application, deviceModel1, location2, newJson);
        assertThat(response, isResponseOk());

        ServiceResponse<String> json = subject.findByModelAndLocation(tenant, application, deviceModel1, location2);
        assertThat(json, isResponseOk());
        assertThat(json.getResult(), is(newJson));

        ServiceResponse<List<DeviceConfig>> responseConfigs = subject.findAll(tenant, application);
        assertThat(responseConfigs, isResponseOk());
        assertThat(responseConfigs.getResult().size(), is(2));
    }

    @Test
    public void shouldSaveWithNullJson() {
        String newJson = null;

        ServiceResponse<DeviceConfig> response = subject.save(tenant, application, deviceModel1, location3, newJson);
        assertThat(response, hasErrorMessage(DeviceConfigSetupService.Validations.DEVICE_INVALID_JSON.getCode()));
    }

    @Test
    public void shouldSaveWithInvalidJson() {
        String newJson = "}}";

        ServiceResponse<DeviceConfig> response = subject.save(tenant, application, deviceModel1, location3, newJson);
        assertThat(response, hasErrorMessage(DeviceConfigSetupService.Validations.DEVICE_INVALID_JSON.getCode()));
    }

    @Test
    public void shouldTrySaveWithoutDeviceModel() {
        String newJson = "{}";

        ServiceResponse<DeviceConfig> response = subject.save(tenant, application, null, location3, newJson);
        assertThat(response, hasErrorMessage(DeviceModelService.Validations.DEVICE_MODEL_NULL.getCode()));
    }


    // ============================== update ==============================//

    @Test
    public void shouldUpdate() {
        String newJson = "{ 'valid' : true }";

        ServiceResponse<DeviceConfig> response = subject.update(tenant, application, deviceModel1, location1, newJson);
        assertThat(response, isResponseOk());

        ServiceResponse<String> json = subject.findByModelAndLocation(tenant, application, deviceModel1, location1);
        assertThat(json, isResponseOk());
        assertThat(json.getResult(), is(newJson));

        ServiceResponse<List<DeviceConfig>> responseConfigs = subject.findAll(tenant, application);
        assertThat(responseConfigs, isResponseOk());
        assertThat(responseConfigs.getResult().size(), is(2));
    }

    @Test
    public void shouldTryUpdateNonExistingConfig() {
        String newJson = "{ 'valid' : true }";

        ServiceResponse<DeviceConfig> response = subject.update(tenant, application, deviceModel1, location3, newJson);
        assertThat(response, hasErrorMessage(DeviceConfigSetupService.Validations.DEVICE_CONFIG_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldTryUpdateWithoutTenant() {
        String newJson = "{ 'valid' : true }";

        ServiceResponse<DeviceConfig> response = subject.update(null, application, deviceModel1, location1, newJson);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithInvalidJson() {
        String newJson = "{ --- }";

        ServiceResponse<DeviceConfig> response = subject.update(tenant, application, deviceModel1, location1, newJson);
        assertThat(response, hasErrorMessage(DeviceConfigSetupService.Validations.DEVICE_INVALID_JSON.getCode()));
    }

    // ============================== remove ==============================//

    @Test
    public void shouldRemove() {
        ServiceResponse<DeviceConfigSetup> response = subject.remove(tenant, application, deviceModel1, location1);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(Messages.DEVICE_CONFIG_REMOVED_SUCCESSFULLY.getCode(), null));

        ServiceResponse<List<DeviceConfig>> responseConfigs = subject.findAll(tenant, application);
        assertThat(responseConfigs, isResponseOk());
        assertThat(responseConfigs.getResult().size(), is(1));
        assertThat(responseConfigs.getResult().get(0).getLocationName(), is(location2.getName()));
    }

    @Test
    public void shouldTryUpdateWithoutApplication() {
        ServiceResponse<DeviceConfigSetup> response = subject.remove(tenant, null, deviceModel1, location1);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    // ============================== findByModelAndLocation ==============================//

    @Test
    public void shouldTryFindByModelAndLocationNonExistingConfig() {
        ServiceResponse<String> response = subject.findByModelAndLocation(tenant, application, deviceModel1, location3);
        assertThat(response, hasErrorMessage(DeviceConfigSetupService.Validations.DEVICE_CONFIG_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldTryFindByModelAndLocationOnlyParentConfig() {
        ServiceResponse<String> response = subject.findByModelAndLocation(tenant, application, deviceModel1, location11);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), is(json1));
    }

    @Test
    public void shouldTryFindByModelAndLocationWithoutLocation() {
        ServiceResponse<String> response = subject.findByModelAndLocation(tenant, application, deviceModel1, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));
    }

}
