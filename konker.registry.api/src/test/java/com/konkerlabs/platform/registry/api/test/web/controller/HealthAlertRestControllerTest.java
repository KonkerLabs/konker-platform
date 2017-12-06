package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceHealthAlertInputVO;
import com.konkerlabs.platform.registry.api.model.UserVO;
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
import org.mockito.internal.matchers.Any;
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

    private AlertTrigger silenceAlertTrigger;

    private HealthAlert healthAlertA;

    private HealthAlert healthAlertB;

    private String BASEPATH = "triggers";

    @Before
    public void setUp() {

        silenceAlertTrigger = new AlertTrigger();
        silenceAlertTrigger.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        silenceAlertTrigger.setName("silence");
        silenceAlertTrigger.setTenant(tenant);
        silenceAlertTrigger.setApplication(application);
        silenceAlertTrigger.setType(AlertTrigger.AlertTriggerType.SILENCE);

        Instant lastChange = Instant.parse("2007-12-03T10:15:30.00Z");

        healthAlertA = HealthAlert.builder()
                .alertId("alert-a-id")
                .guid("71c9713e-1bfe-402b-9456-44464c864575")
                .type(AlertTrigger.AlertTriggerType.SILENCE)
                .lastChange(lastChange)
                .severity(HealthAlert.HealthAlertSeverity.FAIL)
                .build();

        healthAlertB = HealthAlert.builder()
                .alertId("alert-b-id")
                .guid("6d796020-892b-43f5-9694-57bc730bde2b")
                .type(AlertTrigger.AlertTriggerType.CUSTOM)
                .lastChange(lastChange)
                .severity(HealthAlert.HealthAlertSeverity.WARN)
                .build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(alertTriggerService.findByTenantAndApplicationAndName(tenant, application, silenceAlertTrigger.getName()))
            .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok().withResult(silenceAlertTrigger).build());

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
                        .findAllByTenantApplicationAndTriggerGuid(
                                tenant,
                                application,
                                silenceAlertTrigger.getGuid()
                        ))
            .thenReturn(ServiceResponseBuilder.<List<HealthAlert>> ok()
                    .withResult(alertTriggers).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}/alerts", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].guid", is(healthAlertA.getGuid())))
                .andExpect(jsonPath("$.result[0].type", is("SILENCE")))
                .andExpect(jsonPath("$.result[1].guid", is(healthAlertB.getGuid())))
                .andExpect(jsonPath("$.result[1].type", is("CUSTOM")))
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
                        .content(getJson(new DeviceHealthAlertInputVO().apply(healthAlertA)))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.guid", is(healthAlertA.getGuid())))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
        ;
    }

}
