package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.security.managers.PasswordManager;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.konkerlabs.platform.registry.test.base.matchers.NewServiceResponseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
public class DeviceRegisterServiceTest extends BusinessLayerTestSupport {

    private static final String EMPTY_DEVICE_NAME = "";
    private static final String THE_DEVICE_ID = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private static final String THE_DEVICE_API_KEY = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private static final String DEVICE_ID_IN_USE = "95c14b36ba2b43f1";
    private static final String ANOTHER_DEVICE_ID = "eorgh9rgjiod";
    private static final String ANOTHER_DEVICE_NAME = "Another Device Name";
    private static final String ANOTHER_DEVICE_DESCRIPTION = "Another Device Description";
    private static final Instant THE_REGISTRATION_TIME = Instant.now().minus(Duration.ofDays(2));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    private Device device;
    private Tenant currentTenant;
    private Tenant emptyTenant;
    private Device rawDevice;

    @Before
    public void setUp() {
        currentTenant = tenantRepository.findByName("Konker");
        emptyTenant = tenantRepository.findByName("EmptyTenant");

        rawDevice = Device.builder().deviceId("94c32b36cd2b43f1").name("Device name")
                .description("Description").active(true)
                .events(Arrays.asList(new Event[] { Event.builder()
                        .payload("Payload one").timestamp(Instant.ofEpochMilli(1453320973747L)).build() }))
                .build();
        device = spy(rawDevice);
    }
    @Test
    public void shouldReturnResponseMessagesIfTenantIsNull() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.register(null, device);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
    @Test
    public void shouldReturnResponseMessagesIfTenantDoesNotExist() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService
                .register(Tenant.builder().id("unknown_id").build(), device);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsNull() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.register(currentTenant, null);
//
        assertThat(serviceResponse,hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        Map<String, Object[]> errorMessages = new HashMap() {{
           put("some.error", new Object[] {"some_value"});
        }};
        when(device.applyValidations()).thenReturn(Optional.of(errorMessages));

        NewServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response,hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/devices.json" })
    public void shouldReturnResponseMessageIfDeviceIdAlreadyInUse() throws Exception {
        device.setDeviceId(DEVICE_ID_IN_USE);

        Map<String, Object[]> errorMessages = new HashMap() {{
            put(DeviceRegisterService.Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(), null);
        }};

        NewServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json" })
    public void shouldApplyOnRegistrationCallbackBeforeValidations() throws Exception {
        deviceRegisterService.register(currentTenant, device);

        InOrder inOrder = Mockito.inOrder(device);

        inOrder.verify(device).onRegistration();
        inOrder.verify(device).applyValidations();
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json" })
    public void shouldPersistIfDeviceIsValid() throws Exception {
        NewServiceResponse<Device> response = deviceRegisterService.register(currentTenant, rawDevice);

        assertThat(response,isResponseOk());

        Device saved = deviceRepository.findByTenantIdAndDeviceId(currentTenant.getId(), device.getDeviceId());

        assertThat(response.getResult(),equalTo(saved));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json" })
    public void shouldReturnAValidResponseIfRegisterWasSuccessful() throws Exception {
        Tenant tenant = tenantRepository.findOne("71fb0d48-674b-4f64-a3e5-0256ff3a63af");
        device.setTenant(tenant);

        NewServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response, isResponseOk());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json" })
    public void shouldReturnAllRegisteredDevicesWithinATenant() throws Exception {
        NewServiceResponse<List<Device>> response = deviceRegisterService.findAll(emptyTenant);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult();

        assertThat(all, notNullValue());
        assertThat(all, empty());

        response = deviceRegisterService.findAll(currentTenant);
        assertThat(response, isResponseOk());
        all = response.getResult();
        assertThat(all, notNullValue());
        assertThat(all, hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json",  "/fixtures/tenants.json"})
    public void shouldFindADeviceByItsInternalId() throws Exception {
        Device registeredDevice = deviceRepository.findOne(THE_DEVICE_ID);
        Assert.assertThat(registeredDevice,notNullValue());

        Device found = deviceRegisterService.getByDeviceId(currentTenant, THE_DEVICE_ID).getResult();

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json" })
    public void shouldFindADeviceByItsTenantDomainNameAndDeviceId() throws Exception {
        Device registeredDevice = deviceRepository.findByDeviceId(DEVICE_ID_IN_USE);
        Assert.assertThat(registeredDevice,notNullValue());

        Device found = deviceRegisterService.findByTenantDomainNameAndDeviceId(
            registeredDevice.getTenant().getDomainName(),
            registeredDevice.getDeviceId()
        );

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json" })
    public void shouldFindADeviceByItsApiKey() throws Exception {
        Device registeredDevice = deviceRepository.findOne(THE_DEVICE_ID);
        Assert.assertThat(registeredDevice,notNullValue());

        Device found = deviceRegisterService.findByApiKey(THE_DEVICE_API_KEY);

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json" })
    public void shouldRaiseAnExceptionIfDeviceIdIsNullWhenUpdating() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, null, device);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update device with null ID"));
        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_ID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json" })
    public void shouldRaiseAnExceptionIfDeviceIsNullWhenUpdating() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, THE_DEVICE_ID, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update null device"));

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/devices.json" })
    public void shouldReturnResponseErrorMessageIfDeviceNotExists() throws Exception {
        device.setDeviceId(ANOTHER_DEVICE_ID);

        Map<String, Object[]> errorMessages = new HashMap() {{
            put(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(), null);
        }};

        NewServiceResponse<Device> response = deviceRegisterService.update(currentTenant, ANOTHER_DEVICE_ID, device);

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
//        assertThat(response.getResponseMessages(), equalTo(errorMessages));

        assertThat(response,hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/devices.json", "/fixtures/tenants.json" })
    public void shouldRetunResponseErrorMessageIfValidationFailsWhenUpdating() throws Exception {
        device.setName(EMPTY_DEVICE_NAME);

        NewServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_DEVICE_ID, device);
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages().isEmpty(), is(false));

        // ensure data was not changed
        Device foundDevice = deviceRegisterService.getByDeviceId(currentTenant, THE_DEVICE_ID).getResult();
        assertThat(foundDevice.getName().length(), greaterThan(0));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json" })
    public void shouldPersistIfDataIsValidWhenUpdating() throws Exception {
        Device persisted = deviceRepository.findOne(THE_DEVICE_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setActive(false);

        NewServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_DEVICE_ID, persisted);

        Device updated = deviceRepository.findByTenantIdAndDeviceId(currentTenant.getId(), persisted.getDeviceId());

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
//        assertThat(response.getResponseMessages(), empty());
        assertThat(response, isResponseOk());
        assertThat(response.getResult(),notNullValue());
        assertThat(response.getResult(),equalTo(updated));

        // ensure that relevant data was changed
        Device foundDevice = deviceRegisterService.getByDeviceId(currentTenant, THE_DEVICE_ID).getResult();
        assertThat(foundDevice.getName(), equalTo(ANOTHER_DEVICE_NAME));
        assertThat(foundDevice.getDescription(), equalTo(ANOTHER_DEVICE_DESCRIPTION));
        assertThat(foundDevice.isActive(), equalTo(false));

        // ensure that data should not be changed didn't change
        assertThat(foundDevice.getRegistrationDate(), not(equalTo(THE_REGISTRATION_TIME)));
        assertThat(foundDevice.getEvents(), nullValue());
    }

    @Test
    @UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/devices.json" })
    public void shouldNotSetOrChangeApiKeyWhenUpdating() throws Exception {
        Device persisted = deviceRepository.findOne(THE_DEVICE_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setApiKey("changed_api_key");
        persisted.setActive(false);

        persisted = spy(persisted);

        NewServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_DEVICE_ID, persisted);

        InOrder inOrder = Mockito.inOrder(persisted);

        inOrder.verify(persisted, never()).onRegistration();
        inOrder.verify(persisted, never()).setApiKey(anyString());
        inOrder.verify(persisted, never()).getApiKey();

        assertThat(response.getResult().getApiKey(),equalTo(THE_DEVICE_API_KEY));
    }

    @Test
    public void shouldReturnResponseMessageIfDeviceIdIsNullWhenChangingActivation() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID cannot be null"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_ID_NULL.getCode()));
    }

    @Test
    public void shouldReturnResponseMessageIfDeviceDoesNotExist() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, "unknown_id");

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID does not exist"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
    public void shouldSwitchDeviceActivation() throws Exception {
        Device device = deviceRegisterService.getByDeviceId(currentTenant, THE_DEVICE_ID).getResult();
        boolean expected = !device.isActive();

        NewServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, THE_DEVICE_ID);

        Device updated = deviceRegisterService.getByDeviceId(currentTenant, THE_DEVICE_ID).getResult();

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.OK));
//        assertThat(serviceResponse.getResponseMessages(),empty());
        assertThat(serviceResponse, isResponseOk());
        assertThat(updated,notNullValue());
        assertThat(serviceResponse.getResult(),equalTo(updated));
        assertThat(updated.isActive(),equalTo(expected));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
    public void shouldReturnErrorMessageIfIdDoesNotBelongToTenantWhenGet() throws Exception {
        NewServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceId(emptyTenant, THE_DEVICE_ID);

        assertThat(serviceResponse,notNullValue());
        assertThat(serviceResponse.getStatus(),equalTo(NewServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(),null));
        assertThat(serviceResponse.getResult(),nullValue());
    }


}