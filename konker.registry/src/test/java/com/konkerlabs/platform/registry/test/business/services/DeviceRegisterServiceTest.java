package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.DeviceController;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoTestConfiguration.class, BusinessTestConfiguration.class, RedisTestConfiguration.class})
public class DeviceRegisterServiceTest extends BusinessLayerTestSupport {

    private static final String EMPTY_DEVICE_NAME = "";
    private static final String THE_TENANT_ID = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";
    private static final String THE_TENANT_DOMAIN_NAME = "konker";
    private static final String THE_DEVICE_INTERNAL_MONGO_ID = "67014de6-81db-11e6-a5bc-3f99b38315c6";
    private static final String THE_USER_DEFINED_DEVICE_ID = "SN1234567890";
    private static final String THE_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String THE_DEVICE_API_KEY = "e4399b2ed998";
    private static final String DEVICE_ID_IN_USE = "SN1234567890";
    private static final String INCOMING_CHANNEL = "e4399b2ed998.testchannel";
    private static final String OUTGOING_CHANNEL = "e4399b2ed998.testchannel";
    private static final String ANOTHER_TENANT_ID = "0b0fd1a4-81e2-11e6-ae1a-8b71ef1bc5b7";
    private static final String ANOTHER_USER_DEFINED_DEVICE_ID = "eorgh9rgjiod";
    private static final String ANOTHER_DEVICE_GUID = "eaf8213c-81e1-11e6-9254-3314e9f85368";
    private static final String ANOTHER_DEVICE_NAME = "Another Device Name";
    private static final String ANOTHER_DEVICE_DESCRIPTION = "Another Device Description";
    private static final Instant THE_REGISTRATION_TIME = Instant.now().minus(Duration.ofDays(2));
    private static final String THE_DEVICE_PASSWORD = "vKyCY2VXjHWC";


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceEventService deviceEventService;

    private Device device;
    private Tenant currentTenant;
    private Tenant emptyTenant;
    private Device rawDevice;
//    private Event event;

    @Before
    public void setUp() {
        currentTenant = tenantRepository.findByName("Konker");
        emptyTenant = tenantRepository.findByName("EmptyTenant");

        rawDevice = Device.builder().deviceId("94c32b36cd2b43f1").name("Device name")
                .description("Description").active(true)
//                .events(Arrays.asList(new Event[]{Event.builder()
//                        .payload("Payload one").timestamp(Instant.ofEpochMilli(1453320973747L)).build()}))
                .build();
        device = spy(rawDevice);
//        event = Event.builder()
//                .timestamp(Instant.now())
//                .channel("konker")
//                .payload("konker")
//                .deleted(false)
//                .build();


    }

    @Test
    public void shouldReturnResponseMessagesIfTenantIsNull() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(null, device);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnResponseMessagesIfTenantDoesNotExist() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .register(Tenant.builder().id("unknown_id").build(), device);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsNull() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(currentTenant, null);
//
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        Map<String, Object[]> errorMessages = new HashMap() {{
            put("some.error", new Object[]{"some_value"});
        }};
        when(device.applyValidations()).thenReturn(Optional.of(errorMessages));

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnResponseMessageIfDeviceIdAlreadyInUse() throws Exception {
        device.setDeviceId(DEVICE_ID_IN_USE);

        Map<String, Object[]> errorMessages = new HashMap() {{
            put(DeviceRegisterService.Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(), null);
        }};

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldApplyOnRegistrationCallbackBeforeValidations() throws Exception {
        deviceRegisterService.register(currentTenant, device);

        InOrder inOrder = Mockito.inOrder(device);

        inOrder.verify(device).onRegistration();
        inOrder.verify(device).applyValidations();
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldPersistIfDeviceIsValid() throws Exception {
        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, rawDevice);

        assertThat(response, isResponseOk());

        Device saved = deviceRepository.findByTenantIdAndDeviceId(currentTenant.getId(), device.getDeviceId());

        assertThat(response.getResult(), equalTo(saved));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidResponseIfRegisterWasSuccessful() throws Exception {
        Tenant tenant = tenantRepository.findOne("71fb0d48-674b-4f64-a3e5-0256ff3a63af");
        device.setTenant(tenant);

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, device);

        assertThat(response, isResponseOk());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnAllRegisteredDevicesWithinATenant() throws Exception {
        ServiceResponse<List<Device>> response = deviceRegisterService.findAll(emptyTenant);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult();

        assertThat(all, notNullValue());
        assertThat(all, empty());

        response = deviceRegisterService.findAll(currentTenant);
        assertThat(response, isResponseOk());
        all = response.getResult();
        assertThat(all, notNullValue());
        assertThat(all, hasSize(2));
    }


    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldFindADeviceByItsTenantDomainNameAndDeviceGuid() throws Exception {
        Device registeredDevice = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);
        Assert.assertThat(registeredDevice, notNullValue());

        Device found = deviceRegisterService.findByTenantDomainNameAndDeviceGuid(
                registeredDevice.getTenant().getDomainName(),
                registeredDevice.getGuid()
        );

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json"})
    public void shouldFindADeviceByItsApiKey() throws Exception {
        Device registeredDevice = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);
        Assert.assertThat(registeredDevice, notNullValue());

        Device found = deviceRegisterService.findByApiKey(THE_DEVICE_API_KEY);

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldRaiseAnExceptionIfDeviceGuidIsNullWhenUpdating() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, null, device);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update device with null ID"));
        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldRaiseAnExceptionIfDeviceIsNullWhenUpdating() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, THE_USER_DEFINED_DEVICE_ID, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update null device"));

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnResponseErrorMessageIfDeviceNotExists() throws Exception {
        device.setGuid(ANOTHER_DEVICE_GUID);

        Map<String, Object[]> errorMessages = new HashMap() {{
            put(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null);
        }};

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, ANOTHER_DEVICE_GUID, device);

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
//        assertThat(response.getResponseMessages(), equalTo(errorMessages));

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json", "/fixtures/tenants.json"})
    public void shouldRetunResponseErrorMessageIfValidationFailsWhenUpdating() throws Exception {
        device.setName(EMPTY_DEVICE_NAME);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_USER_DEFINED_DEVICE_ID, device);
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages().isEmpty(), is(false));

        // ensure data was not changed
        Device foundDevice = deviceRegisterService.getByDeviceGuid(currentTenant, THE_DEVICE_GUID).getResult();
        assertThat(foundDevice.getName().length(), greaterThan(0));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldPersistIfDataIsValidWhenUpdating() throws Exception {
        Device persisted = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setActive(false);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_DEVICE_GUID, persisted);

        Device updated = deviceRepository.findByTenantIdAndDeviceId(currentTenant.getId(), persisted.getDeviceId());

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
//        assertThat(response.getResponseMessages(), empty());
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), equalTo(updated));

        // ensure that relevant data was changed
        Device foundDevice = deviceRegisterService.getByDeviceGuid(currentTenant, THE_DEVICE_GUID).getResult();
        assertThat(foundDevice.getName(), equalTo(ANOTHER_DEVICE_NAME));
        assertThat(foundDevice.getDescription(), equalTo(ANOTHER_DEVICE_DESCRIPTION));
        assertThat(foundDevice.isActive(), equalTo(false));

        // ensure that data should not be changed didn't change
        assertThat(foundDevice.getRegistrationDate(), not(equalTo(THE_REGISTRATION_TIME)));
//        assertThat(foundDevice.getEvents(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldNotSetOrChangeApiKeyWhenUpdating() throws Exception {
        Device persisted = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setApiKey("changed_api_key");
        persisted.setActive(false);

        persisted = spy(persisted);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, THE_DEVICE_GUID, persisted);

        InOrder inOrder = Mockito.inOrder(persisted);

        inOrder.verify(persisted, never()).onRegistration();
        inOrder.verify(persisted, never()).setApiKey(anyString());
        inOrder.verify(persisted, never()).getApiKey();

        assertThat(response.getResult().getApiKey(), equalTo(THE_DEVICE_API_KEY));
    }

    @Test
    public void shouldReturnResponseMessageIfDeviceGuidIsNullWhenChangingActivation() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID cannot be null"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    public void shouldReturnResponseMessageIfDeviceDoesNotExist() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, "unknown_guid");

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID does not exist"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldSwitchDeviceActivation() throws Exception {
        Device device = deviceRegisterService.getByDeviceGuid(currentTenant, THE_DEVICE_GUID).getResult();
        boolean expected = !device.isActive();

        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, THE_DEVICE_GUID);

        Device updated = deviceRegisterService.getByDeviceGuid(currentTenant, THE_DEVICE_GUID).getResult();

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.OK));
//        assertThat(serviceResponse.getResponseMessages(),empty());
        assertThat(serviceResponse, isResponseOk());
        assertThat(updated, notNullValue());
        assertThat(serviceResponse.getResult(), equalTo(updated));
        assertThat(updated.isActive(), equalTo(expected));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnErrorMessageIfIdDoesNotBelongToTenantWhenGet() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceGuid(emptyTenant, THE_DEVICE_GUID);

        assertThat(serviceResponse, notNullValue());
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnErrorMessageIfDeviceBelongsToOtherTenantOnDeletion() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(ANOTHER_TENANT_ID).build(), THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json"})
    public void shouldReturnErrorMessageIfDeviceDoesNotExistsOnDeletion() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).build(), ANOTHER_USER_DEFINED_DEVICE_ID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/devices.json",
            "/fixtures/event-routes.json"})
    public void shouldReturnErrorMessageIfDeviceHaveEventRoutesOnDeletion() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).build(), THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_HAVE_EVENTROUTES.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/enrichment-rest.json"})
    public void shouldReturnErrorMessageIfDeviceHaveEnrichmentsOnDeletion() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).build(), THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_HAVE_ENRICHMENTS.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/events-incoming.json", "/fixtures/events-outgoing.json"})
    public void shouldReturnSuccessMessageIfDeviceDeletionSucceed() throws Exception {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).domainName(THE_TENANT_DOMAIN_NAME).build(), THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceController.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/events-incoming.json", "/fixtures/events-outgoing.json"})
    public void shouldDeleteInLogicalWayEachDataIngestedOnDeviceForSucceedDeletion() throws Exception {
        Device device = deviceRegisterService
                .findByTenantDomainNameAndDeviceGuid(currentTenant.getDomainName(), THE_DEVICE_GUID);


        ServiceResponse<List<Event>> incomingEvents = deviceEventService.findIncomingBy(currentTenant, THE_DEVICE_GUID,
                INCOMING_CHANNEL, null, null, false, 100);
        ServiceResponse<List<Event>> outgoingEvents = deviceEventService.findOutgoingBy(currentTenant, THE_DEVICE_GUID,
                OUTGOING_CHANNEL, null, null, false, 100);

        assertThat(incomingEvents.getResult().size(), equalTo(2));
        assertThat(outgoingEvents.getResult().size(), equalTo(2));

        deviceRegisterService
                .remove(currentTenant, device.getGuid());

        incomingEvents = deviceEventService.findIncomingBy(currentTenant, THE_DEVICE_GUID,
                INCOMING_CHANNEL, null, null, false, 100);
        outgoingEvents = deviceEventService.findOutgoingBy(currentTenant, THE_DEVICE_GUID,
                OUTGOING_CHANNEL, null, null, false, 100);

        assertThat(incomingEvents.getResult().size(), equalTo(0));
        assertThat(outgoingEvents.getResult().size(), equalTo(0));
    }

    @Test
    public void shouldGetAValidQrCodeForCredentials() {
        DeviceRegisterService.DeviceSecurityCredentials credentials
                = new DeviceRegisterService.DeviceSecurityCredentials(device, THE_DEVICE_PASSWORD);
        ServiceResponse<String> qrCode =
                deviceRegisterService.generateQrCodeAccess(credentials, 200, 200);
        assertThat(qrCode.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(qrCode.getResult().trim().replaceAll("\n","").replaceAll("\\r",""),
                equalTo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzA" +
                        "AADLElEQVR42u2YvY3kMAyFaThQZjcgQG0oU0t2A/5pYNySMrUhwA3ImQLDvMfZnb29" +
                        "4A4YOrwdOJjxR0Ay+fRID/HfPvRD/i9SiCa/b9GVwGvlhzmJnJocfA6GL+PWaDtE0Tn" +
                        "zDRLtnHJf7WDsFLh4BN4iQ3APrJN4MbZP9iaZiBfaCyGEj3TeIsiBz2O0I7uL8AR/ZO" +
                        "ddgvqMyX6/vlXubSKfeHaUG7aTd0f6Q1XvEjw6EfVRJPPArrl9eD05koNwitlXtg0WS" +
                        "VJzNSmUJ+MWkpCt2rm2i9ETTigRDeHsKw3ebZIDpybFY5tnkxzHnSNC8vDagYIcdV88" +
                        "b7w/vB0IIaiSnpSAA+cW30LaUCIKXkhPrrBzbTf4SrAN4/Dtx2cONATaGaNwCYmWAnV" +
                        "flXufHBFHrYXzrRXmlzuDZd0NcpJ3nNo1MjjuNZHVpEgiswgHJhpEO0d1aoJj0VdeI0" +
                        "7JDueb69lHVhMcjof0igzHGjx+7pe/QXDPZFhUk3IHP6gfBqMkz1yKV3UGVgqb35fg1" +
                        "KQYFES8aiDYjMV3ermLgiAH6LEfXoV1oKCXrjXk8qJr1AchDGMOOMd6UoJDdx0ZF82S" +
                        "EvcMUZIjUVN58VAimi2NlZpX5RSkGDG/PuUZTTJICPkbhNBpxatWhsG0Wzp/O8X7BCr" +
                        "enl61yVL7g1AopyZos2j7MKqV2zWdIpyXrhXk8ji44lWM2cSfOMEj6wn00gXxqitIy8" +
                        "XFUU9K2EuAwchB6dltkGTVk4tgTri9F4PbSGrLn0rUkOKfxakEaTdQN6JeO1AQJLILM" +
                        "o5h9tykzuhCTk0OCBCzMMwv5hHGjKGs6skV8uQlnTMePdk5fgwpSgKjuuhjFsM6eY7n" +
                        "WG8Qj/eSTJINDGXS0B5GTy6DpgEt515GbORjL96pCeP2U4OFMCHC/OiVURV52jxLp4X" +
                        "Zw67ccYPI7M/YaSst18jLxECsJngvmULLGAQYIZjv3PGlRAVBIlOeAjSIRTCIfZuEVG" +
                        "Qg20OAVVrHszHeIsiBvLmmHbAz51cONARtBwOFp85bdJ7LiAWqybM+eJ/GkIjxhLqAi" +
                        "ZjV5Oefkh/yD/ILpTl1Kuf8VpkAAAAASUVORK5C"));

    }


}