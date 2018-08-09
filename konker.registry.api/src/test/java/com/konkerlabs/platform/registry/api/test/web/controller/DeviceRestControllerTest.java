package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceRestController;
import com.konkerlabs.platform.registry.api.web.controller.DeviceStatusRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DeviceRestController.class, DeviceStatusRestController.class})
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private Gateway gateway;

    @Autowired
    private GatewayService gatewayService;

    private Set<String> tags;

    private Device device1;

    private Device device2;

    private AlertTrigger alertTrigger;

    private HealthAlert health1;

    private HealthAlert health2;

    private List<HealthAlert> healths;

    private List<Event> events;

    private final String BASEPATH = "devices";

    private final Instant registrationDate = Instant.ofEpochMilli(1495716970000L).minusSeconds(3600L);

    @Before
    public void setUp() {
        final Location locationBR = Location.builder().name("br").build();

        tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
        device1 = Device.builder()
                .deviceId("id1")
                .name("name1")
                .guid("guid1")
                .location(locationBR)
                .application(application)
                .active(true)
                .registrationDate(registrationDate)
                .lastModificationDate(registrationDate)
                .tags(tags)
                .build();
        device2 = Device.builder().deviceId("id2").name("name2").guid("guid2").location(locationBR).application(application).active(false).build();

        alertTrigger = AlertTrigger.builder()
                .guid("7d51c242-81db-11e6-a8c2-0746f976f666")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .build();

        health1 = HealthAlert.builder()
                .guid("7d51c242-81db-11e6-a8c2-0746f976f223")
                .severity(HealthAlertSeverity.FAIL)
                .description("No message received from the device for a long time.")
                .registrationDate(registrationDate)
                .lastChange(Instant.ofEpochMilli(1495716970000L))
                .device(device1)
                .alertTrigger(alertTrigger)
                .build();

        health2 = HealthAlert.builder()
                .guid("7d51c242-81db-11e6-a8c2-0746f976f223")
                .severity(HealthAlertSeverity.OK)
                .description("No message received from the device for a long time.")
                .registrationDate(registrationDate)
                .lastChange(Instant.ofEpochMilli(1495716970000L))
                .device(device1)
                .alertTrigger(alertTrigger)
                .build();

        healths = Arrays.asList(health1, health2);

        Event event = Event.builder()
                .incoming(EventActor.builder().channel("out").deviceGuid(device1.getGuid()).build())
                .creationTimestamp(registrationDate)
                .build();
        events = Collections.singletonList(event);

        when(locationSearchService.findByName(tenant, application, "br", false))
                .thenReturn(ServiceResponseBuilder.<Location>ok().withResult(locationBR).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldListDevices() throws Exception {

        List<Device> devices = new ArrayList<>();
        devices.add(device1);
        devices.add(device2);

        when(deviceRegisterService.findAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<Device>>ok().withResult(devices).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].id", is("id1")))
                .andExpect(jsonPath("$.result[0].name", is("name1")))
                .andExpect(jsonPath("$.result[0].guid", is("guid1")))
                .andExpect(jsonPath("$.result[0].active", is(true)))
                .andExpect(jsonPath("$.result[1].id", is("id2")))
                .andExpect(jsonPath("$.result[1].name", is("name2")))
                .andExpect(jsonPath("$.result[1].guid", is("guid2")))
                .andExpect(jsonPath("$.result[1].active", is(false)));


    }

    @Test
    public void shouldTryListDevicesWithInternalError() throws Exception {

        when(deviceRegisterService.findAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<Device>>error().build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReadDevice() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.id", is("id1")))
                .andExpect(jsonPath("$.result.name", is("name1")))
                .andExpect(jsonPath("$.result.guid", is("guid1")))
                .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Device GUID does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldShowDeviceHealth() throws Exception {

        when(healthAlertService.getLastHighestSeverityByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<HealthAlert>ok().withResult(health1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/health", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.lastUpdate", is("2017-05-25T12:56:10Z")));

    }

    @Test
    public void shouldShowDeviceHealthWithDeviceHealthEmpty() throws Exception {

        when(healthAlertService.getLastHighestSeverityByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<HealthAlert>error().withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/health", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Health alert does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldShowDeviceHealthAlerts() throws Exception {

        when(healthAlertService.findAllByTenantApplicationAndDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<List<HealthAlert>>ok().withResult(healths).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/health/alerts", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].severity", is(health1.getSeverity().toString())))
                .andExpect(jsonPath("$.result[0].description", is("No message received from the device for a long time.")))
                .andExpect(jsonPath("$.result[0].occurrenceDate", is(health1.getRegistrationDate().toString())))
                .andExpect(jsonPath("$.result[0].type", is(health1.getAlertTrigger().getType().name())))
                .andExpect(jsonPath("$.result[0].triggerName", is(alertTrigger.getName())))
                .andExpect(jsonPath("$.result[1].severity", is(health2.getSeverity().toString())))
                .andExpect(jsonPath("$.result[1].description", is("No message received from the device for a long time.")))
                .andExpect(jsonPath("$.result[1].occurrenceDate", is(health2.getRegistrationDate().toString())))
                .andExpect(jsonPath("$.result[1].type", is(health2.getAlertTrigger().getType().name())))
                .andExpect(jsonPath("$.result[1].triggerName", is(alertTrigger.getName())));
    }

    @Test
    public void shouldShowDeviceHealthAlertsWithDeviceHealthEmpty() throws Exception {

        when(healthAlertService.findAllByTenantApplicationAndDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<List<HealthAlert>>error().withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/health/alerts", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Health alert does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldCreateDevice() throws Exception {

        when(deviceRegisterService.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .content(getJson(new DeviceVO().apply(device1)))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.id", is("id1")))
                .andExpect(jsonPath("$.result.name", is("name1")))
                .andExpect(jsonPath("$.result.guid", is("guid1")))
                .andExpect(jsonPath("$.result.locationName", is("br")))
                .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldTryCreateDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .content(getJson(new DeviceVO().apply(device1)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldUpdateDevice() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .content(getJson(new DeviceVO().apply(device1)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());


        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.id", is("id1")))
                .andExpect(jsonPath("$.result.name", is("name1")))
                .andExpect(jsonPath("$.result.guid", is("guid1")))
                .andExpect(jsonPath("$.result.tags", is(Arrays.asList("tag1", "tag2"))))
                .andExpect(jsonPath("$.result.active", is(true)));


    }

    @Test
    public void shouldTryUpdateDeviceWithInternalError() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .content(getJson(new DeviceVO().apply(device1)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldDeleteDevice() throws Exception {

        when(deviceRegisterService.remove(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NAME, BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteDeviceWithInternalError() throws Exception {

        when(deviceRegisterService.remove(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Messages.DEVICE_REMOVED_UNSUCCESSFULLY.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteNonexistentEventRoute() throws Exception {

        when(deviceRegisterService.remove(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldShowDeviceStats() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        when(deviceEventService.findIncomingBy(tenant, application, device1.getGuid(), null, null, null, false, 1))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(events).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/stats", application.getName(), BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.registrationDate", is(registrationDate.toString())))
                .andExpect(jsonPath("$.result.lastModificationDate", is(registrationDate.toString())))
                .andExpect(jsonPath("$.result.lastDataReceivedDate", is(registrationDate.toString())));

    }

    @Test
    public void shouldShowStatWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/stats", NONEXIST_APPLICATION_NAME, BASEPATH, device1.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryShowDeviceStatWithBadRequest() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        when(deviceEventService.findIncomingBy(tenant, application, device1.getGuid(), null, null, null, false, 1))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(events).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}/stats", application.getName(), BASEPATH, device1.getGuid()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Device GUID does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldCreateDeviceByGateway() throws Exception {
        Location br =
                Location.builder()
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acad")
                        .id("br")
                        .name("br")
                        .description("br")
                        .build();

        Location room1 =
                Location.builder()
                        .application(application)
                        .guid("f06d9d2d-f5ce-4cc6-8637-348743e8acae")
                        .id("sala-101")
                        .name("sala-101")
                        .description("sala-101")
                        .parent(br)
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
        br.setChildren(Arrays.asList(room1));

        when(deviceRegisterService.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
                .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        when(gatewayService.validateGatewayAuthorization(gateway, device1.getLocation()))
                .thenReturn(ServiceResponseBuilder.<Boolean>ok().withResult(Boolean.TRUE).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(
                MessageFormat.format("/{0}/{1}/",
                        application.getName(),
                        BASEPATH
                ))
                .content(getJson(new DeviceVO().apply(device1)))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.id", is("id1")))
                .andExpect(jsonPath("$.result.name", is("name1")))
                .andExpect(jsonPath("$.result.guid", is("guid1")))
                .andExpect(jsonPath("$.result.locationName", is("br")))
                .andExpect(jsonPath("$.result.active", is(true)));

    }


}
