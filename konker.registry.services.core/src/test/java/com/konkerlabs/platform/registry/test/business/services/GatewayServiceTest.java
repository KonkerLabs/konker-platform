package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.GatewayRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.GatewayService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        MongoBillingTestConfiguration.class,
        BusinessTestConfiguration.class,
        EmailTestConfig.class})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/users.json", "/fixtures/locations.json"})
public class GatewayServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private GatewayService subject;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserService userService;

    private Tenant tenant;

    private Application application;
    private Application otherApplication;

    private Location location;
    private Gateway gateway;
    private User userAdmin;
    private User userApplication;
    private User userLocation;

    private String guid;

    @Before
    public void setUp() {
        tenant = tenantRepository.findByDomainName("konker");

        guid = "baadad6b-31c7-4827-9d17-61633c0f2efd";

        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        otherApplication = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        location = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "br");

        gateway = Gateway.builder()
                .tenant(tenant)
                .application(application)
                .location(location)
                .name("air conditioner")
                .guid(guid)
                .build();
        gatewayRepository.save(gateway);

        location = locationRepository.findByTenantAndApplicationAndName(tenant.getId(), application.getName(), "sp");
        Gateway deviceFirmware = Gateway.builder()
                .tenant(tenant)
                .application(application)
                .location(location)
                .build();
        gatewayRepository.save(deviceFirmware);

        userAdmin = userService.findByEmail("admin@konkerlabs.com").getResult();
        userApplication = userService.findByEmail("user.application@konkerlabs.com").getResult();
        userLocation = userService.findByEmail("user.location@konkerlabs.com").getResult();

    }

    // ============================== save ==============================//

    @Test
    public void shouldSave() {

        String newName = "ntdxsmztwi";

        Gateway newFirmware = Gateway.builder()
                .name(newName)
                .tenant(tenant)
                .application(application)
                .location(location)
                .build();

        ServiceResponse<Gateway> response = subject.save(tenant, application, newFirmware);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getName(), is(newName));

        Gateway firmwareFromDB = gatewayRepository.findByGuid(tenant.getId(), application.getName(), response.getResult().getGuid());
        assertThat(firmwareFromDB, notNullValue());
        assertThat(firmwareFromDB.getName(), is(newName));

    }

    @Test
    public void shouldTrySaveWithExistingName() {

        Gateway newFirmware = Gateway.builder()
                .name(gateway.getName())
                .tenant(tenant)
                .application(application)
                .location(location)
                .build();

        ServiceResponse<Gateway> response = subject.save(tenant, application, newFirmware);
        assertThat(response, hasErrorMessage(GatewayService.Validations.NAME_IN_USE.getCode()));

    }

    @Test
    public void shouldTrySaveWithNullLocation() {

        Gateway newFirmware = Gateway.builder()
                .name("gwsemlocation")
                .tenant(tenant)
                .application(application)
                .location(null)
                .build();

        ServiceResponse<Gateway> response = subject.save(tenant, application, newFirmware);
        assertThat(response, isResponseOk());

    }

    @Test
    public void shouldTrySaveWithNullTenant() {

        Gateway newFirmware = Gateway.builder()
                .tenant(tenant)
                .application(application)
                .location(location)
                .build();

        ServiceResponse<Gateway> response = subject.save(null, application, newFirmware);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTrySaveWithNullApplication() {

        Gateway newFirmware = Gateway.builder()
                .tenant(tenant)
                .application(application)
                .location(location)
                .build();

        ServiceResponse<Gateway> response = subject.save(tenant, null, newFirmware);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    // ============================== getByGUID ==============================//

    @Test
    public void shouldGetByGuid() {

        ServiceResponse<Gateway> response = subject.getByGUID(tenant, application, gateway.getGuid());
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());

    }

    @Test
    public void shouldTryGetByGuidNonExistingGateway() {

        ServiceResponse<Gateway> response = subject.getByGUID(tenant, application, "wrong_guid");
        assertThat(response, hasErrorMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldTryGetByGuidNonNullGuid() {

        ServiceResponse<Gateway> response = subject.getByGUID(tenant, application, null);
        assertThat(response, hasErrorMessage(GatewayService.Validations.GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryGetByGuidWithNullTenant() {

        ServiceResponse<Gateway> response = subject.getByGUID(null, application, gateway.getGuid());
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }

    // ============================== getAll ==============================//

    @Test
    public void shouldGetAll() {

        ServiceResponse<List<Gateway>> response = subject.getAll(tenant, application, userAdmin);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().isEmpty(), is(false));

    }

    @Test
    public void shouldGetAllByApplication() {
        ServiceResponse<List<Gateway>> response = subject.getAll(tenant, application, userApplication);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().isEmpty(), is(false));
    }

    @Test
    public void shouldGetAllByLocation() {
        ServiceResponse<List<Gateway>> response = subject.getAll(tenant, application, userLocation);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().isEmpty(), is(false));
    }

    @Test
    public void shouldTryGetAllWithNullTenant() {

        ServiceResponse<List<Gateway>> response = subject.getAll(null, application, null);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }


    // ============================== update ==============================//

    @Test
    public void shouldUpdate() {

        gateway.setName("new name");

        ServiceResponse<Gateway> response = subject.update(tenant, application, gateway.getGuid(), gateway);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getName(), is("new name"));

        Gateway firmwareFromDB = gatewayRepository.findByGuid(tenant.getId(), application.getName(), response.getResult().getGuid());
        assertThat(firmwareFromDB, notNullValue());
        assertThat(firmwareFromDB.getName(), is("new name"));

    }

    @Test
    public void shouldTryUpdateWithNullTenant() {

        ServiceResponse<Gateway> response = subject.update(null, application, gateway.getGuid(), gateway);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullGuid() {

        ServiceResponse<Gateway> response = subject.update(tenant, application, null, gateway);
        assertThat(response, hasErrorMessage(GatewayService.Validations.GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryUpdateWithInvalidGuid() {

        ServiceResponse<Gateway> response = subject.update(tenant, application, "invalid_guid", gateway);
        assertThat(response, hasErrorMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullName() {

        gateway.setName(null);

        ServiceResponse<Gateway> response = subject.update(tenant, application, gateway.getGuid(), gateway);
        assertThat(response, hasErrorMessage(Gateway.Validations.NAME_NULL_EMPTY.getCode()));

    }

    @Test
    public void shouldTryUpdateWithNullGateway() {

        ServiceResponse<Gateway> response = subject.update(tenant, application, gateway.getGuid(), null);
        assertThat(response, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));

    }


    // ============================== remove ==============================//

    @Test
    public void shouldRemove() {

        ServiceResponse<Gateway> response = subject.remove(tenant, application, gateway.getGuid());
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());

        Gateway firmwareFromDB = gatewayRepository.findByGuid(tenant.getId(), application.getName(), response.getResult().getGuid());
        assertThat(firmwareFromDB, nullValue());

    }

    @Test
    public void shouldTryRemoveWithNullTenant() {

        ServiceResponse<Gateway> response = subject.remove(null, application, gateway.getGuid());
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }


    @Test
    public void shouldTryRemoveWithNullGuid() {

        ServiceResponse<Gateway> response = subject.remove(tenant, application, null);
        assertThat(response, hasErrorMessage(GatewayService.Validations.GUID_NULL.getCode()));

    }

    @Test
    public void shouldTryRemoveWithInvalidGuid() {

        ServiceResponse<Gateway> response = subject.remove(tenant, application, "invalid_guid");
        assertThat(response, hasErrorMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()));

    }

    @Test
    public void shouldReturnValidAuthorizationToManageDevice() {
        Location rj =
                Location.builder()
                        .tenant(tenant)
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                        .id("rj")
                        .name("rj")
                        .description("rj")
                        .build();

        Location room1 =
                Location.builder()
                        .tenant(tenant)
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acae")
                        .id("sala-101")
                        .name("sala-101")
                        .description("sala-101")
                        .parent(rj)
                        .build();

        Location room101Roof = Location.builder()
                .tenant(tenant)
                .application(application)
                .parent(room1)
                .name("sala-101-teto")
                .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acaf")
                .parent(room1)
                .build();

        room1.setChildren(Arrays.asList(room101Roof));
        rj.setChildren(Arrays.asList(room1));

        ServiceResponse<Boolean> response =
                subject.validateGatewayAuthorization(
                        Gateway
                                .builder()
                                .tenant(tenant)
                                .application(application)
                                .location(rj)
                                .build(),
                        room1
                );

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult(), equalTo(Boolean.TRUE));
    }


    @Test
    public void shoulntdReturnValidAuthorizationToManageDevice() {
        Location rj =
                Location.builder()
                        .tenant(tenant)
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                        .id("rj")
                        .name("rj")
                        .description("rj")
                        .build();

        Location room1 =
                Location.builder()
                        .tenant(tenant)
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acae")
                        .id("sala-101")
                        .name("sala-101")
                        .description("sala-101")
                        .parent(rj)
                        .build();

        Location room101Roof = Location.builder()
                .tenant(tenant)
                .application(application)
                .parent(room1)
                .name("sala-101-teto")
                .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acaf")
                .parent(room1)
                .build();

        rj.setChildren(Arrays.asList(room1));

        ServiceResponse<Boolean> response =
                subject.validateGatewayAuthorization(
                        Gateway
                                .builder()
                                .location(rj)
                                .build(),
                        room101Roof
                );

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResult(), equalTo(Boolean.FALSE));
    }

}
