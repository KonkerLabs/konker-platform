package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.HealthAlertInputVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.HealthAlertRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HealthAlertRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class HealthAlertRestControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private AlertTriggerService alertTriggerService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    private AlertTrigger silenceAlertTrigger;

    private HealthAlert healthAlertA;

    private HealthAlert healthAlertB;

    private Device device;

    private final String BASEPATH = "triggers";

    @Before
    public void setUp() {

        silenceAlertTrigger = new AlertTrigger();
        silenceAlertTrigger.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        silenceAlertTrigger.setName("silence");
        silenceAlertTrigger.setTenant(tenant);
        silenceAlertTrigger.setApplication(application);
        silenceAlertTrigger.setType(AlertTrigger.AlertTriggerType.SILENCE);

        Instant creationDateTime = Instant.parse("2007-12-03T10:15:30.00Z");

        device = Device.builder()
                .deviceId("iu9ocqczgw")
                .build();

        healthAlertA = HealthAlert.builder()
                .device(device)
                .alertTrigger(silenceAlertTrigger)
                .alertId("alert-a-id")
                .description("alert-a-desc")
                .guid("71c9713e-1bfe-402b-9456-44464c864575")
                .lastChange(creationDateTime)
                .registrationDate(creationDateTime)
                .severity(HealthAlert.HealthAlertSeverity.FAIL)
                .build();

        healthAlertB = HealthAlert.builder()
                .device(device)
                .alertTrigger(silenceAlertTrigger)
                .alertId("alert-b-id")
                .description("alert-b-desc")
                .guid("6d796020-892b-43f5-9694-57bc730bde2b")
                .lastChange(creationDateTime)
                .registrationDate(creationDateTime)
                .severity(HealthAlert.HealthAlertSeverity.WARN)
                .build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(alertTriggerService.findByTenantAndApplicationAndName(tenant, application, silenceAlertTrigger.getName()))
            .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok().withResult(silenceAlertTrigger).build());

        when(deviceRegisterService.findByDeviceId(tenant, application, device.getDeviceId()))
            .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(device).build());

        when(healthAlertService.findByTenantApplicationTriggerAndAlertId(
                tenant,
                application,
                silenceAlertTrigger,
                healthAlertA.getAlertId())
        ).thenReturn(ServiceResponseBuilder.<HealthAlert> ok().withResult(healthAlertA).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldListHealthAlerts() throws Exception {

        List<HealthAlert> alertTriggers = new ArrayList<>();
        alertTriggers.add(healthAlertA);
        alertTriggers.add(healthAlertB);

        when(healthAlertService
                        .findAllByTenantApplicationAndTrigger(
                                tenant,
                                application,
                                silenceAlertTrigger
                        ))
            .thenReturn(ServiceResponseBuilder.<List<HealthAlert>> ok()
                    .withResult(alertTriggers).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}/alerts/", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result[0].description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result[1].alertId", is(healthAlertB.getAlertId())))
                .andExpect(jsonPath("$.result[1].description", is(healthAlertB.getDescription())))
                ;

    }

    @Test
    public void shouldUpdateHealthAlert() throws Exception {

        when(healthAlertService
                .update(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.anyString(), Matchers.any(HealthAlert.class)))
                .thenReturn(ServiceResponseBuilder.<HealthAlert> ok()
                        .withResult(healthAlertA).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .put(MessageFormat.format("/{0}/{1}/{2}/alerts/{3}", application.getName(), BASEPATH, silenceAlertTrigger.getName(), healthAlertA.getAlertId()))
                        .content(getJson(new HealthAlertInputVO().apply(healthAlertA)))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result.deviceId", is(device.getDeviceId())))
                .andExpect(jsonPath("$.result.description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result.occurrenceDate", is("2007-12-03T10:15:30Z")))
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.triggerName", is(silenceAlertTrigger.getName())))
        ;

    }

    @Test
    public void shouldEditHealthAlert() throws Exception {

        when(healthAlertService.update(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.anyString(), Matchers.any(HealthAlert.class)))
                .thenReturn(ServiceResponseBuilder.<HealthAlert> ok()
                        .withResult(healthAlertA).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .put(MessageFormat.format("/{0}/{1}/{2}/alerts/{3}", application.getName(), BASEPATH, silenceAlertTrigger.getName(), healthAlertA.getAlertId()))
                        .contentType("application/json")
                        .content(getJson(new HealthAlertInputVO().apply(healthAlertA)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result.deviceId", is(device.getDeviceId())))
                .andExpect(jsonPath("$.result.description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result.occurrenceDate", is("2007-12-03T10:15:30Z")))
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.triggerName", is(silenceAlertTrigger.getName())))

                ;
    }


    @Test
    public void shouldEditNonExistingHealthAlert() throws Exception {

        when(healthAlertService.findByTenantApplicationTriggerAndAlertId(
                tenant,
                application,
                silenceAlertTrigger,
                healthAlertA.getAlertId())
        ).thenReturn(ServiceResponseBuilder.<HealthAlert> error()
                .withMessage(HealthAlertService.Validations.HEALTH_ALERT_DOES_NOT_EXIST.getCode()).build());

        when(healthAlertService.register(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.any(HealthAlert.class)))
                .thenReturn(ServiceResponseBuilder.<HealthAlert> ok()
                        .withResult(healthAlertA).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .put(MessageFormat.format("/{0}/{1}/{2}/alerts/{3}", application.getName(), BASEPATH, silenceAlertTrigger.getName(), healthAlertA.getAlertId()))
                        .contentType("application/json")
                        .content(getJson(new HealthAlertInputVO().apply(healthAlertA)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result.deviceId", is(device.getDeviceId())))
                .andExpect(jsonPath("$.result.description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result.occurrenceDate", is("2007-12-03T10:15:30Z")))
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.triggerName", is(silenceAlertTrigger.getName())))

        ;
    }


    @Test
    public void shouldCreateHealthAlert() throws Exception {

        when(healthAlertService.register(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.any(HealthAlert.class)))
                .thenReturn(ServiceResponseBuilder.<HealthAlert> ok()
                        .withResult(healthAlertA).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .post(MessageFormat.format("/{0}/{1}/{2}/alerts", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
                        .contentType("application/json")
                        .content(getJson(new HealthAlertInputVO().apply(healthAlertA)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result.deviceId", is(device.getDeviceId())))
                .andExpect(jsonPath("$.result.description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result.occurrenceDate", is("2007-12-03T10:15:30Z")))
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.triggerName", is(silenceAlertTrigger.getName())))
        ;

    }

    @Test
    public void shouldRemoveHealthAlert() throws Exception {

        when(healthAlertService.remove(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.anyString(), Matchers.any(HealthAlert.Solution.class)))
                .thenReturn(ServiceResponseBuilder.<HealthAlert> ok()
                        .withResult(healthAlertA).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .delete(MessageFormat.format("/{0}/{1}/{2}/alerts/{3}", application.getName(), BASEPATH, silenceAlertTrigger.getName(), healthAlertA.getAlertId()))
                        .contentType("application/json")
                        .content(getJson(new HealthAlertInputVO().apply(healthAlertA)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldReadHealthAlert() throws Exception {

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .get(MessageFormat.format("/{0}/{1}/{2}/alerts/{3}", application.getName(), BASEPATH, silenceAlertTrigger.getName(), healthAlertA.getAlertId()))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.alertId", is(healthAlertA.getAlertId())))
                .andExpect(jsonPath("$.result.deviceId", is(device.getDeviceId())))
                .andExpect(jsonPath("$.result.description", is(healthAlertA.getDescription())))
                .andExpect(jsonPath("$.result.occurrenceDate", is("2007-12-03T10:15:30Z")))
                .andExpect(jsonPath("$.result.severity", is("FAIL")))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.triggerName", is(silenceAlertTrigger.getName())))
        ;

    }

}
