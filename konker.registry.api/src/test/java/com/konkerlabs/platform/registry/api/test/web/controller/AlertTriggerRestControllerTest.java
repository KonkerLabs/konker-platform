package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.konkerlabs.platform.registry.api.model.AlertTriggerInputVO;
import com.konkerlabs.platform.registry.api.web.controller.AlertTriggerRestController;
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

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AlertTriggerRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class AlertTriggerRestControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private AlertTriggerService alertTriggerService;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private LocationSearchService locationSearchService;

    private DeviceModel deviceModel;

    private Location location;

    private AlertTrigger silenceAlertTrigger;

    private final String BASEPATH = "triggers";

    @Before
    public void setUp() {

        deviceModel = DeviceModel.builder()
                .tenant(tenant)
                .application(application)
        		.guid(UUID.randomUUID().toString())
        		.name("air conditioner")
        		.build();

        location = Location.builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .name("13th floor")
                .build();

        silenceAlertTrigger = new AlertTrigger();
        silenceAlertTrigger.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        silenceAlertTrigger.setName("silence");
        silenceAlertTrigger.setDescription("2cdc391d description");
        silenceAlertTrigger.setTenant(tenant);
        silenceAlertTrigger.setApplication(application);
        silenceAlertTrigger.setDeviceModel(deviceModel);
        silenceAlertTrigger.setLocation(location);
        silenceAlertTrigger.setType(AlertTrigger.AlertTriggerType.SILENCE);
        silenceAlertTrigger.setMinutes(200);

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> ok().withResult(deviceModel).build());

        when(locationSearchService.findByName(tenant, application, location.getName(), true))
            .thenReturn(ServiceResponseBuilder.<Location> ok().withResult(location).build());

        when(alertTriggerService.findByTenantAndApplicationAndName(tenant, application, silenceAlertTrigger.getName()))
            .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok().withResult(silenceAlertTrigger).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldListAlertTrigger() throws Exception {

        List<AlertTrigger> alertTriggers = new ArrayList<>();
        alertTriggers.add(silenceAlertTrigger);
        alertTriggers.add(silenceAlertTrigger);

        when(alertTriggerService.listByTenantAndApplication(tenant, application))
            .thenReturn(ServiceResponseBuilder.<List<AlertTrigger>> ok()
                    .withResult(alertTriggers).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result[0].locationName", is(location.getName())))
                .andExpect(jsonPath("$.result[0].type", is("SILENCE")))
                .andExpect(jsonPath("$.result[0].minutes", is(200)))
                .andExpect(jsonPath("$.result[1].deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result[1].locationName", is(location.getName())))
                .andExpect(jsonPath("$.result[1].type", is("SILENCE")))
                .andExpect(jsonPath("$.result[1].minutes", is(200)))
                ;
    }

    @Test
    public void shouldReturnInternalErrorWhenListAlertTrigger() throws Exception {

        when(alertTriggerService.listByTenantAndApplication(tenant, application))
            .thenReturn(ServiceResponseBuilder.<List<AlertTrigger>>error()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
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
    public void shouldEditAlertTrigger() throws Exception {

        when(alertTriggerService.update(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.anyString(), Matchers.any(AlertTrigger.class)))
                .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok()
                        .withResult(silenceAlertTrigger).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
                        .contentType("application/json")
                        .content(getJson(new AlertTriggerInputVO().apply(silenceAlertTrigger)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.name", is(silenceAlertTrigger.getName())))
                .andExpect(jsonPath("$.result.description", is(silenceAlertTrigger.getDescription())))
                .andExpect(jsonPath("$.result.deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.minutes", is(200)))
                ;

    }

    @Test
    public void shouldCreateAlertTrigger() throws Exception {

        when(alertTriggerService.save(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.any(AlertTrigger.class)))
            .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok()
                    .withResult(silenceAlertTrigger).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .post(MessageFormat.format("/{0}/{1}", application.getName(), BASEPATH))
                        .contentType("application/json")
                        .content(getJson(new AlertTriggerInputVO().apply(silenceAlertTrigger)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.name", is(silenceAlertTrigger.getName())))
                .andExpect(jsonPath("$.result.description", is(silenceAlertTrigger.getDescription())))
                .andExpect(jsonPath("$.result.deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.minutes", is(200)))
        ;
    }

    @Test
    public void shouldRemoveAlertTrigger() throws Exception {

        when(alertTriggerService.remove(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.anyString()))
                .thenReturn(ServiceResponseBuilder.<AlertTrigger> ok()
                        .withResult(silenceAlertTrigger).build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
                        .contentType("application/json")
                        .content(getJson(new AlertTriggerInputVO().apply(silenceAlertTrigger)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }


    @Test
    public void shouldReadAlertTrigger() throws Exception {

        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, silenceAlertTrigger.getName()))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.name", is(silenceAlertTrigger.getName())))
                .andExpect(jsonPath("$.result.description", is(silenceAlertTrigger.getDescription())))
                .andExpect(jsonPath("$.result.deviceModelName", is(deviceModel.getName())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                .andExpect(jsonPath("$.result.type", is("SILENCE")))
                .andExpect(jsonPath("$.result.minutes", is(200)))
        ;

    }

}
