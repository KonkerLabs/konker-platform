package com.konkerlabs.platform.registry.test.services;

import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceSecurityCredentials;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.Messages;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
		PubServerConfig.class,
        EventStorageConfig.class,
        EmailConfig.class,
        DeviceRegisterServiceTest.DeviceRegisterServiceTestConfig.class})
@ActiveProfiles("ssl")
public class DeviceRegisterServiceTest extends BusinessLayerTestSupport {

    private static final String EMPTY_DEVICE_NAME = "";
    private static final String THE_TENANT_ID = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";
    private static final String THE_TENANT_DOMAIN_NAME = "konker";
    private static final String THE_DEVICE_INTERNAL_MONGO_ID = "67014de6-81db-11e6-a5bc-3f99b38315c6";
    private static final String THE_USER_DEFINED_DEVICE_ID = "SN1234567890";
    private static final String THE_DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String THE_OTHER_DEVICE_GUID = "8363c556-84ea-11e6-92a2-4b01fea7e259";
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
    private static final Set<String> DEVICE_TAGS =new HashSet<>(Arrays.asList("tag1", "tag2"));
    private static final Set<String> ANOTHER_DEVICE_TAGS =new HashSet<>(Arrays.asList("anotherTag1", "anotherTag2"));
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
    private LocationRepository locationRepository;
    @Autowired
    private DeviceModelRepository deviceModelRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private UserService userService;

    private Device device;
    private Tenant currentTenant;
    private Tenant emptyTenant;
    private Device rawDevice;
    private Application currentApplication;
    private Application otherApplication;
    private Application konkerApplication;
    private User userAdmin;
    private User userApplication;
    private User userLocation;

    @Before
    public void setUp() {
        currentTenant = tenantRepository.findByDomainName("konker");
        emptyTenant = tenantRepository.findByDomainName("empty");
        currentApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "smartffkonker");
        konkerApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        applicationRepository.findAllByTenant(currentTenant.getId());

        otherApplication = Application.builder()
				.name("smartffkonkerother")
				.friendlyName("Konker Smart Frig")
				.description("Konker Smart Frig - take pic, tells temperature")
				.tenant(currentTenant)
                .qualifier("konker")
                .registrationDate(Instant.ofEpochMilli(1453320973747L))
				.build();

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

        userAdmin = userService.findByEmail("admin@konkerlabs.com").getResult();
        userApplication = userService.findByEmail("user.application@konkerlabs.com").getResult();
        userLocation = userService.findByEmail("user.location@konkerlabs.com").getResult();
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfTenantIsNull() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(null, currentApplication, device);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfAppIsNull() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(currentTenant, null, device);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfAppNotExists() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(currentTenant, otherApplication, device);

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfTenantDoesNotExist() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .register(Tenant.builder().id("unknown_id").domainName("unknown_domain").build(), currentApplication, device);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfRecordIsNull() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.register(currentTenant, currentApplication, null);
//
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessagesIfRecordIsInvalid() {
        Map<String, Object[]> errorMessages = new HashMap<String, Object[]>() {
            private static final long serialVersionUID = 6888355713114248747L;
            {
                put("some.error", new Object[]{"some_value"});
            }
        };
        when(device.applyValidations()).thenReturn(Optional.of(errorMessages));

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, device);

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessageIfDeviceIdAlreadyInUse() {
        device.setDeviceId(DEVICE_ID_IN_USE);

        Map<String, Object[]> errorMessages = new HashMap<String, Object[]>() {
            private static final long serialVersionUID = -8293081311357160161L;
            {
                put(DeviceRegisterService.Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(), null);
            }
        };

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, device);

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessageIfDeviceLimitExceeded() {
    	currentTenant.setDevicesLimit(2l);

    	Map<String, Object[]> errorMessages = Collections.singletonMap(DeviceRegisterService.Validations.DEVICE_TENANT_LIMIT.getCode(), null);
    	ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, device);

    	assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldApplyOnRegistrationCallbackBeforeValidations() {
        deviceRegisterService.register(currentTenant, currentApplication, device);

        InOrder inOrder = Mockito.inOrder(device);

        inOrder.verify(device).onRegistration();
        inOrder.verify(device).applyValidations();
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldPersistIfDeviceIsValid() {
        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, rawDevice);

        assertThat(response, isResponseOk());

        Device saved = deviceRepository.findByTenantIdAndApplicationAndDeviceId(currentTenant.getId(), currentApplication.getName(), device.getDeviceId());

        assertThat(response.getResult(), equalTo(saved));
    }

	@Test
	@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json" })
	public void shouldPersistWithTenantLogLevel() {
		currentTenant.setLogLevel(LogLevel.ALL);

		ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, rawDevice);

		assertThat(response, isResponseOk());

		Device saved = deviceRepository.findByTenantIdAndApplicationAndDeviceId(currentTenant.getId(), currentApplication.getName(), device.getDeviceId());

		assertThat(saved.getLogLevel(), equalTo(LogLevel.ALL));

		deviceRegisterService.remove(currentTenant, currentApplication, rawDevice.getGuid());

		// back to default log server
		currentTenant.setLogLevel(LogLevel.WARNING);

		response = deviceRegisterService.register(currentTenant, currentApplication, rawDevice);

		assertThat(response, isResponseOk());
		assertThat(response.getResult().getLocation().getName(), is("default"));

		saved = deviceRepository.findByTenantIdAndApplicationAndDeviceId(currentTenant.getId(), currentApplication.getName(), device.getDeviceId());

		assertThat(saved.getLogLevel(), equalTo(LogLevel.WARNING));

	}

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnAValidResponseIfRegisterWasSuccessful() {
        Tenant tenant = tenantRepository.findOne("71fb0d48-674b-4f64-a3e5-0256ff3a63af");
        device.setTenant(tenant);

        ServiceResponse<Device> response = deviceRegisterService.register(currentTenant, currentApplication, device);

        assertThat(response, isResponseOk());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnAllRegisteredDevicesWithinATenant() {
        ServiceResponse<List<Device>> response = deviceRegisterService.findAll(emptyTenant, currentApplication);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult();

        assertThat(all, notNullValue());
        assertThat(all, empty());

        response = deviceRegisterService.findAll(currentTenant, currentApplication);
        assertThat(response, isResponseOk());
        all = response.getResult();
        assertThat(all, notNullValue());
        assertThat(all, hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnCountRegisteredDevicesWithinATenant() {
        ServiceResponse<Long> response = deviceRegisterService.countAll(emptyTenant, currentApplication);
        assertThat(response, isResponseOk());
        Long all = response.getResult();

        assertThat(all, notNullValue());
        assertThat(all, is(0L));

        response = deviceRegisterService.countAll(currentTenant, currentApplication);
        assertThat(response, isResponseOk());
        all = response.getResult();
        assertThat(all, notNullValue());
        assertThat(all, is(2L));
    }


    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldFindADeviceByItsTenantDomainNameAndDeviceGuid() {
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
    @UsingDataSet(locations = {"/fixtures/devices.json", "/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldFindADeviceByItsApiKey() {
        Device registeredDevice = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);
        Assert.assertThat(registeredDevice, notNullValue());

        Device found = deviceRegisterService.findByApiKey(THE_DEVICE_API_KEY);

        assertThat(found, notNullValue());
        assertThat(found, equalTo(registeredDevice));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldRaiseAnExceptionIfDeviceGuidIsNullWhenUpdating() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, currentApplication, null, device);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update device with null ID"));
        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldRaiseAnExceptionIfAppIsNullWhenUpdating() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, null, THE_USER_DEFINED_DEVICE_ID, device);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update device with null ID"));
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldRaiseAnExceptionIfDeviceIsNullWhenUpdating() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.update(currentTenant, currentApplication, THE_USER_DEFINED_DEVICE_ID, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Cannot update null device"));

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseErrorMessageIfDeviceNotExists() {
        device.setGuid(ANOTHER_DEVICE_GUID);

        Map<String, Object[]> errorMessages = new HashMap<String, Object[]>() {
            private static final long serialVersionUID = -4314418911800720742L;
            {
                put(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null);
            }
        };

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, currentApplication, ANOTHER_DEVICE_GUID, device);

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
//        assertThat(response.getResponseMessages(), equalTo(errorMessages));

        assertThat(response, hasAllErrors(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json", "/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldRetunResponseErrorMessageIfValidationFailsWhenUpdating() {
        device.setName(EMPTY_DEVICE_NAME);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, currentApplication, THE_USER_DEFINED_DEVICE_ID, device);
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages().isEmpty(), is(false));

        // ensure data was not changed
        Device foundDevice = deviceRegisterService.getByDeviceGuid(currentTenant, currentApplication, THE_DEVICE_GUID).getResult();
        assertThat(foundDevice.getName().length(), greaterThan(0));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldPersistIfDataIsValidWhenUpdating() {
        Device persisted = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setTags(ANOTHER_DEVICE_TAGS);
        persisted.setActive(false);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, currentApplication, THE_DEVICE_GUID, persisted);

        Device updated = deviceRepository.findByTenantIdAndApplicationAndDeviceId(currentTenant.getId(), currentApplication.getName(), persisted.getDeviceId());

//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
//        assertThat(response.getResponseMessages(), empty());
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), equalTo(updated));

        // ensure that relevant data was changed
        Device foundDevice = deviceRegisterService.getByDeviceGuid(currentTenant, currentApplication, THE_DEVICE_GUID).getResult();
        assertThat(foundDevice.getName(), equalTo(ANOTHER_DEVICE_NAME));
        assertThat(foundDevice.getDescription(), equalTo(ANOTHER_DEVICE_DESCRIPTION));
        assertThat(foundDevice.getTags(), equalTo(ANOTHER_DEVICE_TAGS));
        assertThat(foundDevice.isActive(), equalTo(false));

        // ensure that data should not be changed didn't change
        assertThat(foundDevice.getRegistrationDate(), not(equalTo(THE_REGISTRATION_TIME)));
//        assertThat(foundDevice.getEvents(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldNotSetOrChangeApiKeyWhenUpdating() {
        Device persisted = deviceRepository.findOne(THE_DEVICE_INTERNAL_MONGO_ID);

        persisted.setName(ANOTHER_DEVICE_NAME);
        persisted.setDescription(ANOTHER_DEVICE_DESCRIPTION);
        persisted.setRegistrationDate(THE_REGISTRATION_TIME);
        persisted.setTags(ANOTHER_DEVICE_TAGS);
        persisted.setApiKey("changed_api_key");
        persisted.setActive(false);

        persisted = spy(persisted);

        ServiceResponse<Device> response = deviceRegisterService.update(currentTenant, currentApplication, THE_DEVICE_GUID, persisted);

        InOrder inOrder = Mockito.inOrder(persisted);

        inOrder.verify(persisted, never()).onRegistration();
        inOrder.verify(persisted, never()).setApiKey(anyString());
        inOrder.verify(persisted, never()).getApiKey();

        assertThat(response.getResult().getApiKey(), equalTo(THE_DEVICE_API_KEY));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessageIfDeviceGuidIsNullWhenChangingActivation() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, currentApplication, null);

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID cannot be null"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnResponseMessageIfDeviceDoesNotExist() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, currentApplication, "unknown_guid");

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.ERROR));
//        assertThat(serviceResponse.getResponseMessages(),hasItem("Device ID does not exist"));

        assertThat(serviceResponse, hasErrorMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldSwitchDeviceActivation() {
        Device device = deviceRegisterService.getByDeviceGuid(currentTenant, currentApplication, THE_DEVICE_GUID).getResult();
        boolean expected = !device.isActive();

        ServiceResponse<Device> serviceResponse = deviceRegisterService.switchEnabledDisabled(currentTenant, currentApplication, THE_DEVICE_GUID);

        Device updated = deviceRegisterService.getByDeviceGuid(currentTenant, currentApplication, THE_DEVICE_GUID).getResult();

//        assertThat(serviceResponse,notNullValue());
//        assertThat(serviceResponse.getStatus(),equalTo(ServiceResponse.Status.OK));
//        assertThat(serviceResponse.getResponseMessages(),empty());
        assertThat(serviceResponse, isResponseOk());
        assertThat(updated, notNullValue());
        assertThat(serviceResponse.getResult(), equalTo(updated));
        assertThat(updated.isActive(), equalTo(expected));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMessageIfIdDoesNotBelongToTenantWhenGet() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService.getByDeviceGuid(emptyTenant, currentApplication, THE_DEVICE_GUID);

        assertThat(serviceResponse, notNullValue());
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMessageIfDeviceBelongsToOtherTenantOnDeletion() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(ANOTHER_TENANT_ID).build(), currentApplication, THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMessageIfDeviceDoesNotExistsOnDeletion() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).build(), currentApplication, ANOTHER_USER_DEFINED_DEVICE_ID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    }

    @Test
    @UsingDataSet(locations = {
            "/fixtures/tenants.json",
            "/fixtures/devices.json",
            "/fixtures/event-routes.json",
            "/fixtures/applications.json"})
    public void shouldReturnErrorMessageIfDeviceHaveEventRoutesOnDeletion() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).build(), currentApplication, THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_HAVE_EVENTROUTES.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/events-incoming.json", "/fixtures/events-outgoing.json", "/fixtures/applications.json"})
    public void shouldReturnSuccessMessageIfDeviceDeletionSucceed() {
        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(Tenant.builder().id(THE_TENANT_ID).domainName(THE_TENANT_DOMAIN_NAME).build(), currentApplication, THE_DEVICE_GUID);
        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/events-incoming.json", "/fixtures/events-outgoing.json", "/fixtures/applications.json"})
    public void shouldDeleteInLogicalWayEachDataIngestedOnDeviceForSucceedDeletion() {
        Device device = deviceRegisterService
                .findByTenantDomainNameAndDeviceGuid(currentTenant.getDomainName(), THE_OTHER_DEVICE_GUID);


        ServiceResponse<List<Event>> incomingEvents = deviceEventService.findIncomingBy(currentTenant, konkerApplication, THE_OTHER_DEVICE_GUID, null,
                INCOMING_CHANNEL, null, null, false, 100);
        ServiceResponse<List<Event>> outgoingEvents = deviceEventService.findOutgoingBy(currentTenant, konkerApplication, THE_OTHER_DEVICE_GUID, null,
                OUTGOING_CHANNEL, null, null, false, 100);

        assertThat(incomingEvents.getResult().size(), equalTo(2));
        assertThat(outgoingEvents.getResult().size(), equalTo(2));

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .remove(currentTenant, konkerApplication, device.getGuid());

        assertThat(serviceResponse.getResponseMessages(), equalTo(Collections.singletonMap(Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null)));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldGetAValidQrCodeForCredentials() {
        DeviceSecurityCredentials credentials
                = new DeviceSecurityCredentials(device, THE_DEVICE_PASSWORD);
        credentials.getDevice().setApplication(currentApplication);
        ServiceResponse<String> qrCode =
                deviceRegisterService.generateQrCodeAccess(credentials, 200, 200, Language.EN.getLocale());
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

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldGenerateSecurityCredentials() {
    	Device device = deviceRegisterService
                .findByTenantDomainNameAndDeviceGuid(currentTenant.getDomainName(), THE_DEVICE_GUID);

    	ServiceResponse<DeviceSecurityCredentials> credentials = deviceRegisterService.generateSecurityPassword(device.getTenant(), currentApplication, device.getGuid());

    	assertThat(credentials.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(credentials.getResult().getDevice().getApiKey(), is(not(device.getApiKey())));
    	assertThat(credentials.getResult().getDevice().getSecurityHash(), is(not(device.getSecurityHash())));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorWhenGenerateSecurityCredentials() {
    	ServiceResponse<DeviceSecurityCredentials> credentials = deviceRegisterService
    			.generateSecurityPassword(currentTenant, currentApplication, ANOTHER_DEVICE_GUID);

    	assertThat(credentials.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    }

    // ************************* getDeviceDataURLs *************************

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldGetDeviceDataURLs() {
        Device device = deviceRegisterService
                .findByTenantDomainNameAndDeviceGuid(currentTenant.getDomainName(), THE_DEVICE_GUID);

        ServiceResponse<DeviceRegisterService.DeviceDataURLs> response = deviceRegisterService
                .getDeviceDataURLs(currentTenant, currentApplication, device, Locale.ENGLISH);

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getHttpURLPub(), is("http://dev-server:8080/pub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpURLSub(), is("http://dev-server:8080/sub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpsURLPub(), is("https://dev-server:443/pub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpsURLSub(), is("https://dev-server:443/sub/e4399b2ed998/<Channel>"));

        assertThat(response.getResult().getMqttPubTopic(), is("data/e4399b2ed998/pub/<Channel>"));
        assertThat(response.getResult().getMqttSubTopic(), is("data/e4399b2ed998/sub/<Channel>"));
        assertThat(response.getResult().getMqttURL(), is("mqtt://dev-server:1883"));
        assertThat(response.getResult().getMqttsURL(), is("mqtts://dev-server:1883"));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldGetDeviceDataURLsWithDataDomain() {
        Device device = deviceRegisterService
                .findByTenantDomainNameAndDeviceGuid(currentTenant.getDomainName(), THE_DEVICE_GUID);

        currentApplication.setDataApiDomain("domain.io");
        currentApplication.setDataMqttDomain("domain.io.mqtt");
        device.getApplication().setDataApiDomain("domain.io");
        device.getApplication().setDataMqttDomain("domain.io.mqtt");

        ServiceResponse<DeviceRegisterService.DeviceDataURLs> response = deviceRegisterService
                .getDeviceDataURLs(currentTenant, currentApplication, device, Locale.ENGLISH);

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getHttpURLPub(), is("http://domain.io:8080/pub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpURLSub(), is("http://domain.io:8080/sub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpsURLPub(), is("https://domain.io:443/pub/e4399b2ed998/<Channel>"));
        assertThat(response.getResult().getHttpsURLSub(), is("https://domain.io:443/sub/e4399b2ed998/<Channel>"));

        assertThat(response.getResult().getMqttPubTopic(), is("data/e4399b2ed998/pub/<Channel>"));
        assertThat(response.getResult().getMqttSubTopic(), is("data/e4399b2ed998/sub/<Channel>"));
        assertThat(response.getResult().getMqttURL(), is("mqtt://domain.io.mqtt:1883"));
        assertThat(response.getResult().getMqttsURL(), is("mqtts://domain.io.mqtt:1883"));

    }

    // ************************* move *************************

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldMove() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        ServiceResponse<Device> response = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, otherApplication);

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldMoveNonExistingLocationAndDeviceModel() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        Location location = Location
                .builder()
                .tenant(currentTenant)
                .application(currentApplication)
                .name("dxpobdi1yx")
                .defaultLocation(false)
                .build();
        location = locationRepository.save(location);

        Device device = deviceRepository.findByTenantAndApplicationAndGuid(
                currentTenant.getId(),
                currentApplication.getName(),
                THE_DEVICE_GUID);
        device.setLocation(location);
        deviceRepository.save(device);

        ServiceResponse<Device> response = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, otherApplication);

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getLocation().getName(), equalTo("default"));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldMoveExistingLocationAndDeviceModel() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        Location location = Location
                .builder()
                .tenant(currentTenant)
                .application(currentApplication)
                .name("dxpobdi1yx")
                .defaultLocation(false)
                .build();
        location = locationRepository.save(location);

        Location locationOther = Location
                .builder()
                .tenant(currentTenant)
                .application(otherApplication)
                .name("dxpobdi1yx")
                .defaultLocation(false)
                .build();
        locationRepository.save(locationOther);

        Device device = deviceRepository.findByTenantAndApplicationAndGuid(
                currentTenant.getId(),
                currentApplication.getName(),
                THE_DEVICE_GUID);
        device.setLocation(location);
        deviceRepository.save(device);

        ServiceResponse<Device> response = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, otherApplication);

        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResult().getLocation().getName(), equalTo("dxpobdi1yx"));

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldReturnErrorMoveDeviceWithDependencies() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, otherApplication);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_HAVE_EVENTROUTES.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldReturnErrorMoveDeviceWithExistingId() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        Device sameNameDevice = Device.builder()
                                      .tenant(currentTenant)
                                      .application(otherApplication)
                                      .deviceId("SN1234567890")
                                      .build();
        deviceRepository.save(sameNameDevice);

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, otherApplication);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_ID_ALREADY_REGISTERED.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMoveSameDestination() {

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, currentApplication);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.EQUALS_ORIGIN_DESTINATION_APPLICATIONS.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMoveWithGuidNull() {

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, null, currentApplication);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_GUID_NULL.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMoveWithDestinationNull() {

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, null);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(ApplicationService.Validations.APPLICATION_NULL.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMoveWithInvalidGuid() {

        Application otherApplication = applicationRepository.findByTenantAndName(currentTenant.getId(), "konker");

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, "0000-aaaa", otherApplication);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorMoveWithInvalidDestination() {

        ServiceResponse<Device> serviceResponse = deviceRegisterService
                .move(currentTenant, currentApplication, THE_DEVICE_GUID, Application.builder().name("NOT").build());

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(),
                hasEntry(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());

    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorTenantNullFindByDeviceId() {
    	ServiceResponse<Device> serviceResponse = deviceRegisterService.findByDeviceId(null, currentApplication, device.getDeviceId());
    	
    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(CommonValidations.TENANT_NULL.getCode(), null));
    	assertThat(serviceResponse.getResult(), nullValue());
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnErrorApplicationNullFindByDeviceId() {
    	ServiceResponse<Device> serviceResponse = deviceRegisterService.findByDeviceId(currentTenant, null, device.getDeviceId());
    	
    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(ApplicationService.Validations.APPLICATION_NULL.getCode(), null));
    	assertThat(serviceResponse.getResult(), nullValue());
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json"})
    public void shouldReturnDeviceFindByDeviceId() {
    	ServiceResponse<Device> serviceResponse = deviceRegisterService.findByDeviceId(currentTenant, currentApplication, THE_USER_DEFINED_DEVICE_ID);
    	
    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResult(), notNullValue());
    	assertThat(serviceResponse.getResult().getApiKey(), equalTo(THE_DEVICE_API_KEY));
    }
    
    @Configuration
    static class DeviceRegisterServiceTestConfig {
    	
    	@Bean
    	public TenantDailyUsageRepository tenantDailyUsageRepository() {
    		return Mockito.mock(TenantDailyUsageRepository.class);
    	}
    	
    	@Bean
    	public JavaMailSender javaMailSender() {
    		return Mockito.mock(JavaMailSender.class);
    	}
    	
    	@Bean
    	public SpringTemplateEngine springTemplateEngine() {
    		return Mockito.mock(SpringTemplateEngine.class);
    	}
    	
    	@Bean
        public ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }

        @Bean
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
            return Mockito.mock(RabbitTemplate.class);
        }
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/location.json", "/fixtures/users.json"})
    public void shouldSearchWithoutFilter() {
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userAdmin, null, null, 1, 10);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult().getContent();

        all = response.getResult().getContent();
        assertThat(all, notNullValue());
        assertThat(all, hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/location.json", "/fixtures/users.json"})
    public void shouldSearchFilterByTag() {
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userAdmin, null, "anotherTag1", 1, 10);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult().getContent();

        all = response.getResult().getContent();
        assertThat(all, notNullValue());
        assertThat(all, hasSize(1));
        assertTrue(all.get(0).getTags().contains("anotherTag1"));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByUserAdmin() {
        List<Device> all = new ArrayList<>();
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userAdmin, null, null, 1, 10);
        assertThat(response, isResponseOk());
        all.addAll(response.getResult().getContent());

        currentApplication.setName("konker");
        response = deviceRegisterService.search(currentTenant, currentApplication, userAdmin, null, null, 1, 10);
        assertThat(response, isResponseOk());
        all.addAll(response.getResult().getContent());

        assertThat(all, notNullValue());
        assertThat(all, hasSize(4));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByUserApplication() {
        List<Device> all = new ArrayList<>();
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userApplication, null, null, 1, 10);
        assertThat(response, isResponseOk());
        all.addAll(response.getResult().getContent());

        assertThat(all, notNullValue());
        assertThat(all, hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByUserLocation() {
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userLocation, null, null, 1, 10);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult().getContent();

        assertThat(all, notNullValue());
        assertThat(all, hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByLocation() {
        ServiceResponse<Page<Device>> response = deviceRegisterService.search(currentTenant, currentApplication, userApplication, "br", null, 1, 10);
        assertThat(response, isResponseOk());
        List<Device> all = response.getResult().getContent();

        assertThat(all, notNullValue());
        assertThat(all, hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByUserNoPermission() {
        ServiceResponse<Page<Device>> serviceResponse = deviceRegisterService.search(currentTenant, otherApplication, userLocation, "sp", null, 1, 10);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByInexistLocation() {
        ServiceResponse<Page<Device>> serviceResponse = deviceRegisterService.search(currentTenant, currentApplication, userLocation, "noLoc", null, 1, 10);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(LocationService.Validations.LOCATION_GUID_DOES_NOT_EXIST.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/devices.json", "/fixtures/applications.json",
            "/fixtures/locations.json", "/fixtures/users.json"})
    public void shouldSearchFilterByNoPermissionLocation() {
        ServiceResponse<Page<Device>> serviceResponse = deviceRegisterService.search(currentTenant, currentApplication, userLocation, "rj", null, 1, 10);

        assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.ERROR));
        assertThat(serviceResponse.getResponseMessages(), hasEntry(DeviceRegisterService.Validations.DEVICE_LOCATION_IS_NOT_CHILD.getCode(), null));
        assertThat(serviceResponse.getResult(), nullValue());
    }

}