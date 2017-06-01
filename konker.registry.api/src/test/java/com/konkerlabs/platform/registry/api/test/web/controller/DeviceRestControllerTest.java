package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.HealthAlert;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertSeverity;
import com.konkerlabs.platform.registry.business.model.HealthAlert.HealthAlertType;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.HealthAlertService.Validations;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private HealthAlertService healthAlertService;
    
    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private Device device1;

    private Device device2;
    
    private HealthAlert health1;
    
    private HealthAlert health2;
    
    private List<HealthAlert> healths;

    private String BASEPATH = "devices";

    @Before
    public void setUp() {
        final Location locationBR = Location.builder().name("br").build();

        device1 = Device.builder().deviceId("id1").name("name1").guid("guid1").location(locationBR).application(application).active(true).build();
        device2 = Device.builder().deviceId("id2").name("name2").guid("guid2").location(locationBR).application(application).active(false).build();

        Instant registrationDate = Instant.ofEpochMilli(1495716970000l).minusSeconds(3600l);
        
		health1 = HealthAlert.builder()
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
				.severity(HealthAlertSeverity.FAIL)
				.description("Device sem enviar mensagem por mais de 5 minutos")
				.registrationDate(registrationDate)
				.lastChange(Instant.ofEpochMilli(1495716970000l))
        		.type(HealthAlertType.SILENCE)
        		.deviceGuid(device1.getGuid())
        		.triggerGuid("7d51c242-81db-11e6-a8c2-0746f976f666")
        		.build();
		
		health2 = HealthAlert.builder()
				.guid("7d51c242-81db-11e6-a8c2-0746f976f223")
				.severity(HealthAlertSeverity.OK)
				.description("Device sem enviar mensagem por mais de 5 minutos")
				.registrationDate(registrationDate)
				.lastChange(Instant.ofEpochMilli(1495716970000l))
        		.type(HealthAlertType.SILENCE)
        		.deviceGuid(device1.getGuid())
        		.triggerGuid("7d51c242-81db-11e6-a8c2-0746f976f666")
        		.build();
		
		healths = Arrays.asList(health1, health2);
        
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.id", is("id1")))
                    .andExpect(jsonPath("$.result.name", is("name1")))
                    .andExpect(jsonPath("$.result.guid", is("guid1")))
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, device1.getGuid()))
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

        when(healthAlertService.getLastHightServerityByDeviceGuid(tenant, application, device1.getGuid()))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.severity", is("FAIL")))
                    .andExpect(jsonPath("$.result.lastUpdate", is("2017-05-25T12:56:10Z")));

    }
    
    @Test
    public void shouldShowDeviceHealthWithDeviceHealthEmpty() throws Exception {
    	
    	when(healthAlertService.getLastHightServerityByDeviceGuid(tenant, application, device1.getGuid()))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].guid", is(health1.getGuid())))
                    .andExpect(jsonPath("$.result[0].severity", is(health1.getSeverity().toString())))
                    .andExpect(jsonPath("$.result[0].description", is(health1.getDescription())))
                    .andExpect(jsonPath("$.result[0].occurenceDate", is(health1.getLastChange().toString())))
                    .andExpect(jsonPath("$.result[0].type", is(health1.getType().toString())))
                    .andExpect(jsonPath("$.result[0].triggerGuid", is(health1.getTriggerGuid())))
                    .andExpect(jsonPath("$.result[1].guid", is(health2.getGuid())))
                    .andExpect(jsonPath("$.result[1].severity", is(health2.getSeverity().toString())))
                    .andExpect(jsonPath("$.result[1].description", is(health2.getDescription())))
                    .andExpect(jsonPath("$.result[1].occurenceDate", is(health2.getLastChange().toString())))
                    .andExpect(jsonPath("$.result[1].type", is(health2.getType().toString())))
                    .andExpect(jsonPath("$.result[1].triggerGuid", is(health2.getTriggerGuid())));
    }
    
    @Test
    public void shouldShowDeviceHealthAlertsWithDeviceHealthEmpty() throws Exception {

    	when(healthAlertService.findAllByTenantApplicationAndDeviceGuid(tenant, application, device1.getGuid()))
				.thenReturn(ServiceResponseBuilder.<List<HealthAlert>> error().withMessage(Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build());

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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryDeleteWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, device1.getGuid()))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
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
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages").exists())
                    .andExpect(jsonPath("$.result").doesNotExist());

    }


}
