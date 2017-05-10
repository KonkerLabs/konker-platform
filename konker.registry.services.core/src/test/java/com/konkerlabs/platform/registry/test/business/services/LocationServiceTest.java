package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;

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
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class})
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/locations.json" })
public class LocationServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private LocationService subject;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

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
        ServiceResponse<List<Location>> response = subject.findAll(tenant, application);
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
        ServiceResponse<List<Location>> response = subject.findAll(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldListFindAllWithNullApplication() {
        ServiceResponse<List<Location>> response = subject.findAll(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    // ============================== update ==============================//

    @Test
    public void shouldUpdateWithNullTenant() {
        ServiceResponse<Location> response = subject.update(null, null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullApplication() {
        ServiceResponse<Location> response = subject.update(tenant, null, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullGuid() {
        ServiceResponse<Location> response = subject.update(tenant, application, null, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNullObject() {
        ServiceResponse<Location> response = subject.update(tenant, application, "71fb0d48-674b-4f64-a3e5-0256ff3a63af", null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNonExistingGuid() {
        Location newLocation = Location.builder()
                                       .name("br2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "71fb0d48-674b-4f64-a3e5-0256ff3a63af", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldUpdateWithNullName() {
        Location newLocation = Location.builder()
                                       .name(null)
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_NULL_EMPTY.getCode()));
    }

    @Test
    public void shouldTryToUpdateWithExistingName() {
        Location newLocation = Location.builder()
                                       .name("sp")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "8f07f5e4-b411-45d4-90b5-a5228f7e0361", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));
    }

    @Test
    public void shouldTryToUpdateWithoutParent() {
        Location newLocation = Location.builder()
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "8f07f5e4-b411-45d4-90b5-a5228f7e0361", newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_PARENT_NULL.getCode()));
    }

    @Test
    public void shouldUpdateWithNewDefault() {
        Location parent = subject.findByName(tenant, application, "sp").getResult();
        assertThat(parent.isDefaultLocation(), is(true));

        Location newLocation = Location.builder()
                .parent(parent)
                .name("BR2")
                .description("BBRR")
                .defaultLocation(true)
                .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(true));

        parent = subject.findByName(tenant, application, "sp").getResult();
        assertThat(parent.isDefaultLocation(), is(false));
    }

    @Test
    public void shouldUpdate() {
        Location parent = subject.findByName(tenant, application, "sp").getResult();

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.update(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11", newLocation);
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
        ServiceResponse<Location> response = subject.save(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullApplication() {
        ServiceResponse<Location> response = subject.save(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullObject() {
        ServiceResponse<Location> response = subject.save(tenant, application, null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNullName() {
        Location newLocation = Location.builder()
                                       .name(null)
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(Location.Validations.NAME_NULL_EMPTY.getCode()));
    }

    @Test
    public void shouldSaveWithExistingName() {
        Location newLocation = Location.builder()
                                       .name("sp")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_NAME_ALREADY_REGISTERED.getCode()));
    }

    @Test
    public void shouldTryToSaveWithoutParent() {
        Location newLocation = Location.builder()
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.save(tenant, application, newLocation);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_PARENT_NULL.getCode()));
    }

    @Test
    public void shouldSaveWithNewDefault() {
        Location parent = subject.findByName(tenant, application, "sp").getResult();
        assertThat(parent.isDefaultLocation(), is(true));

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(true)
                                       .build();

        ServiceResponse<Location> response = subject.save(tenant, application, newLocation);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REGISTERED_SUCCESSFULLY.getCode(), null));

        assertThat(response.getResult().getGuid(), notNullValue());
        assertThat(response.getResult().getName(), is("BR2"));
        assertThat(response.getResult().getDescription(), is("BBRR"));
        assertThat(response.getResult().getParent().getName(), is("sp"));
        assertThat(response.getResult().isDefaultLocation(), is(true));

        parent = subject.findByName(tenant, application, "sp").getResult();
        assertThat(parent.isDefaultLocation(), is(false));
    }

    @Test
    public void shouldSave() {
        Location parent = subject.findByName(tenant, application, "sp").getResult();

        Location newLocation = Location.builder()
                                       .parent(parent)
                                       .name("BR2")
                                       .description("BBRR")
                                       .defaultLocation(false)
                                       .build();

        ServiceResponse<Location> response = subject.save(tenant, application, newLocation);
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
        ServiceResponse<Location> response = subject.remove(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNullApplication() {
        ServiceResponse<Location> response = subject.remove(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNullGuid() {
        ServiceResponse<Location> response = subject.remove(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_NULL.getCode()));
    }

    @Test
    public void shouldRemoveWithNonExistingGuid() {
        ServiceResponse<Location> response = subject.remove(tenant, application, "591200ea9061e67cb2228f85");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldTryToRemoveWithSubLocations() {
        ServiceResponse<Location> response = subject.remove(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_HAVE_CHILDRENS.getCode()));
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

        ServiceResponse<Location> response = subject.remove(tenant, application, "d75758a6-235b-413b-85b3-d218404f8c11");
        assertThat(response, hasErrorMessage(LocationService.Validations.LOCATION_HAVE_DEVICES.getCode()));
    }

    @Test
    public void shouldRemove() {
        ServiceResponse<Location> response = subject.remove(tenant, application, "a14e671f-32d7-4ec0-8006-8d93eeed401c");
        assertThat(response.isOk(), is(true));
        assertThat(response.getResponseMessages(), hasEntry(LocationService.Messages.LOCATION_REMOVED_SUCCESSFULLY.getCode(), null));

        assertThat(locationRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), "a14e671f-32d7-4ec0-8006-8d93eeed401c"), nullValue());
    }

    // ============================== findRoot ==============================//

    @Test
    public void shouldFindRootWithNullTenant() {
        ServiceResponse<Location> response = subject.findRoot(null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindRootWithNullApplication() {
        ServiceResponse<Location> response = subject.findRoot(tenant, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindRoot() {
        ServiceResponse<Location> response = subject.findRoot(tenant, application);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("br"));
        assertThat(response.getResult().getParent(), nullValue());

        Location br = response.getResult();
        assertThat(br.getName(), is("br"));
        assertThat(br.getChildrens().size(), is(2));
        assertThat(br.getChildrens().get(0).getName(), is("sp"));
        assertThat(br.getChildrens().get(1).getName(), is("rj"));

        Location sp = br.getChildrens().get(0);
        Location rj = br.getChildrens().get(1);

        assertThat(sp.getChildrens().size(), is(0));
        assertThat(rj.getChildrens().size(), is(1));
        assertThat(rj.getChildrens().get(0).getName(), is("sala-101"));
    }

    @Test
    public void shouldFindRootWithoutRoot() {
        ServiceResponse<Location> response = subject.findRoot(tenant, otherApplication);
        assertThat(response.isOk(), is(true));
        assertThat(response.getResult().getName(), is("root"));
        assertThat(response.getResult().getParent(), nullValue());
    }

    // ============================== findByName ==============================//

    @Test
    public void shouldFindByNameWithNullTenant() {
        ServiceResponse<Location> response = subject.findByName(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindByNameWithNullApplication() {
        ServiceResponse<Location> response = subject.findByName(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindByNameWithNullName() {
        ServiceResponse<Location> response = subject.findByName(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldTryToFindByNameNonExistingName() {
        ServiceResponse<Location> response = subject.findByName(tenant, application, "br2");
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFindByName() {
        ServiceResponse<Location> response = subject.findByName(tenant, application, "rj");
        assertThat(response, isResponseOk());

        assertThat(response.getResult().getName(), is("rj"));
        assertThat(response.getResult().getDescription(), is("Rio Janeiro"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
    }

    // ============================== findByGuid ==============================//

    @Test
    public void shouldFindByGuidWithNullTenant() {
        ServiceResponse<Location> response = subject.findByGuid(null, null, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));
    }

    @Test
    public void shouldFindByGuidWithNullApplication() {
        ServiceResponse<Location> response = subject.findByGuid(tenant, null, null);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    public void shouldFindByGuidWithNullName() {
        ServiceResponse<Location> response = subject.findByGuid(tenant, application, null);
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldTryToFindByGuidNonExistingName() {
        ServiceResponse<Location> response = subject.findByGuid(tenant, application, "br2");
        assertThat(response, hasErrorMessage(LocationService.Messages.LOCATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFindByGuid() {
        ServiceResponse<Location> response = subject.findByGuid(tenant, application, "a14e671f-32d7-4ec0-8006-8d93eeed401c");
        assertThat(response, isResponseOk());
        assertThat(response.getResult().getName(), is("sala-101"));
        assertThat(response.getResult().getDescription(), is("Sala 101"));
        assertThat(response.getResult().isDefaultLocation(), is(false));
    }

}
