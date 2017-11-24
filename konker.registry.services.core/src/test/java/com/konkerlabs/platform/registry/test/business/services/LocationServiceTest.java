package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.LocationService.Validations;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class})
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/locations.json" })
public class LocationServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceConfigSetupService deviceConfigSetupService;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Tenant tenant;

    private Application application;
    private Application otherApplication;

    @Before
    public void setUp() {
        tenant = tenantRepository.findByName("Konker");

        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        otherApplication = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");
    }

    // ============================== findAll ==============================//

    @Test
    public void shouldListFindAll() {
        ServiceResponse<List<Location>> response = locationSearchService.findAll(tenant, application);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(4));
        assertThat(response.getResult().get(0).getName(), is("br"));
        assertThat(response.getResult().get(1).getName(), is("sp"));
        assertThat(response.getResult().get(2).getName(), is("rj"));
        assertThat(response.getResult().get(3).getName(), is("sala-101"));
    }

    @Test
    public void shouldListFindAllWithNullTenant() {
        ServiceResponse<List<Location>> response = locationSearchService.findAll(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldListFindAllWithNullApplication() {
        ServiceResponse<List<Location>> response = locationSearchService.findAll(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    // ============================== update ==============================//

    @Test
    public void shouldUpdateWithNullTenant() {
        ServiceResponse<Location> response = locationService.update(null, null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullApplication() {
        ServiceResponse<Location> response = locationService.update(tenant, null, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullGuid() {
        ServiceResponse<Location> response = locationService.update(tenant, application, null, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullObject() {
        ServiceResponse<Location> response = locationService.update(tenant, application, "71fb0d48-674b-4f64-a3e5-0256ff3a63af", null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNonExistingGuid() {
        Location newLocation = Location.builder()
                                       .name("br2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "71fb0d48-674b-4f64-a3e5-0256ff3a63af", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldUpdateWithNullName() {
        Location newLocation = Location.builder()
                                       .name(null)
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_NULL_EMPTY.getCode()));
    }

    @Test
    public void shouldTryToUpdateWithExistingName() {
        Location newLocation = Location.builder()
                                       .name("sp")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "8f07f5e4-b411-45d4-90b5-a5228f7e0361", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));
    }

    @Test
    public void shouldTryToUpdateWithoutParent() {
        Location newLocation = Location.builder()
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "8f07f5e4-b411-45d4-90b5-a5228f7e0361", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_PARENT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNewDefault() {
        Location parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();
        assertThat(parent.isDefaultLocation(), is(true));

        Location newLocation = Location.builder()
                .parent(parent)
                .name("BR2")
                .description("BBRR")
                .defaultLocation(true)
                .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(true));

        parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();
        assertThat(parent.isDefaultLocation(), is(false));
    }

    @Test
    public void shouldUpdate() {
        Location parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
    }

    // ============================== save ==============================//

    @Test
    public void shouldSaveWithNullTenant() {
        ServiceResponse<Location> response = locationService.save(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullApplication() {
        ServiceResponse<Location> response = locationService.save(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullObject() {
        ServiceResponse<Location> response = locationService.save(tenant, application, null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullName() {
        Location newLocation = Location.builder()
                                       .name(null)
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_NULL_EMPTY.getCode()));
    }

    @Test
    public void shouldSaveWithInvalidName() {
        Location newLocation = Location.builder()
                                       .name("*oi")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_INVALID.getCode()));

        newLocation = Location.builder()
                .name("5")
                .description("BBRR")
                .defaultLocation(false)
                .build();

        response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_INVALID.getCode()));

        newLocation = Location.builder()
                .name("Sssss5\\111")
                .description("BBRR")
                .defaultLocation(false)
                .build();

        response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_INVALID.getCode()));

        newLocation = Location.builder()
                .name("São Paulo")
                .description("BBRR")
                .defaultLocation(false)
                .build();

        response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_PARENT_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithExistingName() {
        Location newLocation = Location.builder()
                                       .name("sp")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));
    }

    @Test
    public void shouldTryToSaveWithoutParent() {
        Location newLocation = Location.builder()
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_PARENT_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNewDefault() {
        Location parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();
        assertThat(parent.isDefaultLocation(), is(true));

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(true)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getGuid(), notNullValue());
        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(true));

        parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();
        assertThat(parent.isDefaultLocation(), is(false));
    }

    @Test
    public void shouldSave() {
        Location parent = locationSearchService.findByName(tenant, application, "sp", false).getResult();

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = locationService.save(tenant, application, newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getGuid(), notNullValue());
        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
    }

    // ============================== remove ==============================//

    @Test
    public void shouldRemoveWithNullTenant() {
        ServiceResponse<Location> response = locationService.remove(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNullApplication() {
        ServiceResponse<Location> response = locationService.remove(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNullGuid() {
        ServiceResponse<Location> response = locationService.remove(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNonExistingGuid() {
        ServiceResponse<Location> response = locationService.remove(tenant, application, "591200ea9061e67cb2228f85");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldTryToRemoveWithDevices() {
        Location location = locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), "d75758a6-235b-413b-85b3-d218404f8c11");

        Device device = Device.builder()
                              .name("8r4ictigl5")
                              .tenant(tenant)
                              .application(application)
                              .location(location)
                              .build();

        deviceRepository.save(device);

        ServiceResponse<Location> response = locationService.remove(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_HAVE_DEVICES.getCode()));
    }

    @Test
    public void shouldRemove() {
        Location locationRJ = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "rj");
        Location locationSala = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sala-101");

        assertThat(locationRJ, notNullValue());
        assertThat(locationSala, notNullValue());

        ServiceResponse<Location> response = locationService.remove(tenant, application, locationRJ.getGuid());
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REMOVED_SUCCESSFULLY.getCode(), null));

        locationRJ = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "rj");
        locationSala = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sala-101");

        assertThat(locationRJ, nullValue());
        assertThat(locationSala, nullValue());

    }

    // ============================== findRoot ==============================//

    @Test
    public void shouldFindRootWithNullTenant() {
        ServiceResponse<Location> response = locationSearchService.findRoot(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindRootWithNullApplication() {
        ServiceResponse<Location> response = locationSearchService.findRoot(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindRoot() {
        ServiceResponse<Location> response = locationSearchService.findRoot(tenant, application);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("br"));
        assertThat(response.getResult().getParent(), nullValue());

        Location br = response.getResult();
        assertThat(br.getName(), is("br"));
        assertThat(br.getChildren().size(), is(2));
        assertThat(br.getChildren().get(0).getName(), is("sp"));
        assertThat(br.getChildren().get(1).getName(), is("rj"));

        Location sp = br.getChildren().get(0);
        Location rj = br.getChildren().get(1);

        assertThat(sp.getChildren().size(), is(0));
        assertThat(rj.getChildren().size(), is(1));
        assertThat(rj.getChildren().get(0).getName(), is("sala-101"));
    }

    @Test
    public void shouldFindRootWithoutRoot() {
        ServiceResponse<Location> response = locationSearchService.findRoot(tenant, otherApplication);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("default"));
        assertThat(response.getResult().getParent(), nullValue());
    }


    // ============================== findDefault ==============================//

    @Test
    public void shouldFindDefaultWithNullTenant() {
        ServiceResponse<Location> response = locationSearchService.findDefault(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindDefaultWithNullApplication() {
        ServiceResponse<Location> response = locationSearchService.findDefault(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindDefaultWithoutRoot() {
        ServiceResponse<Location> response = locationSearchService.findDefault(tenant, otherApplication);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("default"));
        assertThat(response.getResult().getParent(), nullValue());
    }

    @Test
    public void shouldFindDefault() {
        ServiceResponse<Location> response = locationSearchService.findDefault(tenant, application);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("sp"));
        assertThat(response.getResult().getParent().getName(), is("br"));
    }

    // ============================== findByName ==============================//

    @Test
    public void shouldFindByNameWithNullTenant() {
        ServiceResponse<Location> response = locationSearchService.findByName(null, null, null, false);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindByNameWithNullApplication() {
        ServiceResponse<Location> response = locationSearchService.findByName(tenant, null, null, false);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindByNameWithNullName() {
        ServiceResponse<Location> response = locationSearchService.findByName(tenant, application, null, false);
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldTryToFindByNameNonExistingName() {
        ServiceResponse<Location> response = locationSearchService.findByName(tenant, application, "br2", false);
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFindByNameWithTreeStructure() {
        ServiceResponse<Location> response = locationSearchService.findByName(tenant, application, "rj", true);
        assertThat(response, isResponseOk());

        assertThat(response.getResult().getName(), is("rj"));
        assertThat(response.getResult().getDescription(), is("Rio Janeiro"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
        assertThat(response.getResult().getChildren().size(), is(1));
    }

    @Test
    public void shouldFindByName() {
        ServiceResponse<Location> response = locationSearchService.findByName(tenant, application, "rj", false);
        assertThat(response, isResponseOk());

        assertThat(response.getResult().getName(), is("rj"));
        assertThat(response.getResult().getDescription(), is("Rio Janeiro"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
        assertThat(response.getResult().getChildren(), nullValue());
    }

    // ============================== findByGuid ==============================//

    @Test
    public void shouldFindByGuidWithNullTenant() {
        ServiceResponse<Location> response = locationSearchService.findByGuid(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindByGuidWithNullApplication() {
        ServiceResponse<Location> response = locationSearchService.findByGuid(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindByGuidWithNullName() {
        ServiceResponse<Location> response = locationSearchService.findByGuid(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldTryToFindByGuidNonExistingName() {
        ServiceResponse<Location> response = locationSearchService.findByGuid(tenant, application, "br2");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldFindByGuid() {
        ServiceResponse<Location> response = locationSearchService.findByGuid(tenant, application, "a14e671f-32d7-4ec0-8006-8d93eeed401c");
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), is("sala-101"));
        assertThat(response.getResult().getDescription(), is("Sala 101"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
    }

    // ============================== listDevicesByLocationName ==============================//

    @Test
    public void shouldListDevicesByLocationNameWithNullTenant() {
        ServiceResponse<List<Device>> response = locationSearchService.listDevicesByLocationName(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldListDevicesByLocationNameWithNullApplication() {
        ServiceResponse<List<Device>> response = locationSearchService.listDevicesByLocationName(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldListDevicesByLocationNameWithNullLocationName() {
        ServiceResponse<List<Device>> response = locationSearchService.listDevicesByLocationName(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldListDevices() {
        Location sp = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sp");
        Location rj = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "rj");
        Location sala = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sala-101");

        deviceRepository.save(Device.builder().name("sp-device").tenant(tenant).application(application).location(sp).build());
        deviceRepository.save(Device.builder().name("rj-device").tenant(tenant).application(application).location(rj).build());
        deviceRepository.save(Device.builder().name("sala-device").tenant(tenant).application(application).location(sala).build());

        ServiceResponse<List<Device>> response = null;

        response = locationSearchService.listDevicesByLocationName(tenant, application, "sp");
        assertThat(response, isResponseOk());
        assertThat(response.getResult().size(), is(1));
        assertThat(response.getResult().get(0).getName(), is("sp-device"));

        response = locationSearchService.listDevicesByLocationName(tenant, application, "rj");
        assertThat(response, isResponseOk());
        assertThat(response.getResult().size(), is(2));
        assertThat(response.getResult().get(0).getName(), is("rj-device"));
        assertThat(response.getResult().get(1).getName(), is("sala-device"));
    }

    // ============================== updateSubtree ==============================//

    @Test
    public void shouldUpdateSubtreeWithNullTenant() {
        List<Location> sublocations = new ArrayList<>();

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(null, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateSubtreeWithNullApplication() {
        List<Location> sublocations = new ArrayList<>();

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, null, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldTryUpdateSubtreeWithNonExistingGuid() {

        List<Location> sublocations = new ArrayList<>();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, "invalid-guid", sublocations);
        assertThat(response, hasErrorMessage(Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldTryUpdateSubtreeWithNodeWithoutName() {

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(Location.builder().tenant(tenant).build());

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_NULL_EMPTY.getCode()));

    }

    @Test
    public void shouldTryUpdateSubtreeWithMultiplesDefault() {

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(Location.builder().name("rj-01").tenant(tenant).defaultLocation(true).build());
        sublocations.add(Location.builder().name("rj-02").tenant(tenant).defaultLocation(true).build());

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(Validations.LOCATION_MULTIPLE_DEFAULTS.getCode()));

    }

    @Test
    public void shouldTryUpdateSubtreeWithNodesWithSameName() {

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(Location.builder().name("rj-01").tenant(tenant).build());
        sublocations.add(Location.builder().name("rj-01").tenant(tenant).build());

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));

    }

    @Test
    public void shouldTryUpdateSubtreeWithNodesWithNameInUse() {

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(Location.builder().name("sp").tenant(tenant).build());

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));

    }

    @Test
    public void shouldTryUpdateSubtreeWithNodesSubtreeWithConfigs() {

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();
        Location locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();

        List<Location> sublocations = new ArrayList<>();
        // remove sala-101

        Location locationSala101Teto = Location.builder()
                                               .tenant(tenant)
                                               .application(application)
                                               .parent(locationSala101)
                                               .name("sala-101-teto")
                                               .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                                               .build();

        locationRepository.save(locationSala101Teto);

        DeviceModel deviceModel = DeviceModel.builder()
                                             .guid("5fddb765-fef6-4e2a-b8bc-770a46197f1a")
                                             .tenant(tenant)
                                             .application(application)
                                             .name("sensor")
                                             .build();

        deviceModelRepository.save(deviceModel);

        ServiceResponse<DeviceConfig> deviceConfigResponse = deviceConfigSetupService.save(tenant, application, deviceModel, locationSala101Teto, "{}");
        assertThat(deviceConfigResponse, isResponseOk());

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, hasErrorMessage(Validations.LOCATION_HAVE_DEVICE_CONFIGS.getCode()));

    }


    @Test
    public void shouldUpdateSubtreeWithNewNodes() {

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();
        Location locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();

        Location rj01 = Location.builder().name("rj-01").tenant(tenant).build();
        Location rj02 = Location.builder().name("rj-02").tenant(tenant).build();

        locationSala101.setChildren(new ArrayList<>());
        locationSala101.getChildren().add(rj02);

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(rj01);
        sublocations.add(locationSala101);

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, isResponseOk());

        rj01 = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "rj-01");
        assertThat(rj01, notNullValue());
        assertThat(rj01.getParent().getName(), is("rj"));

        rj02 = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "rj-02");
        assertThat(rj02, notNullValue());
        assertThat(rj02.getParent().getName(), is("sala-101"));

    }

    @Test
    public void shouldUpdateSubtreeChangingDescription() {

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();
        Location locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();

        List<Location> sublocations = new ArrayList<>();
        sublocations.add(locationSala101);

        locationSala101.setDescription("test change description");

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, isResponseOk());

        locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();
        assertThat(locationSala101.getDescription(), is("test change description"));

    }

    @Test
    public void shouldUpdateSubtreeRemovingNode() {

        List<Location> sublocations = new ArrayList<>();

        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();

        Location locationSala101 = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sala-101");
        assertThat(locationSala101, notNullValue());

        ServiceResponse<Location> response = locationService.updateSubtree(tenant, application, locationRJ.getGuid(), sublocations);
        assertThat(response, isResponseOk());

        locationSala101 = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sala-101");
        assertThat(locationSala101, nullValue());

    }

    @Test
    public void shouldReturnTrueForSubLocation() {
        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();
        Location locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();


        Location locationSala101Teto = Location.builder()
                .tenant(tenant)
                .application(application)
                .parent(locationSala101)
                .name("sala-101-teto")
                .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                .build();

        locationRJ.setChildren(new ArrayList<>());
        locationSala101.setChildren(new ArrayList<>());
        locationSala101.getChildren().add(locationSala101Teto);
        locationRJ.getChildren().add(locationSala101);

        Assert.assertTrue(LocationTreeUtils.isSublocationOf(locationRJ, locationSala101));
        Assert.assertTrue(LocationTreeUtils.isSublocationOf(locationRJ, locationSala101Teto));
    }

    @Test
    public void shouldReturnFalseForSubLocation() {
        Location locationRJ = locationSearchService.findByName(tenant, application, "rj", false).getResult();
        Location locationSala101 = locationSearchService.findByName(tenant, application, "sala-101", false).getResult();


        Location locationSala101Teto = Location.builder()
                .tenant(tenant)
                .application(application)
                .parent(locationSala101)
                .name("sala-101-teto")
                .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                .build();

        locationRJ.setChildren(new ArrayList<>());
        locationSala101.setChildren(new ArrayList<>());

        Assert.assertFalse(LocationTreeUtils.isSublocationOf(locationRJ, locationSala101));
        Assert.assertFalse(LocationTreeUtils.isSublocationOf(locationRJ, locationSala101Teto));
    }

}
