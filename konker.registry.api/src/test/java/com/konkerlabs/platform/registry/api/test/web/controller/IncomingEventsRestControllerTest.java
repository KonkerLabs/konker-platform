package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.IncomingEventsRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.mongodb.util.JSON;
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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = IncomingEventsRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class IncomingEventsRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NAME = "AppLost";

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;
    private Application application;
    private final String dateIso = "2017-04-07T15:10:02.827Z";
    private Event event1;
    private Event event2;
    private final String PAYLOAD1 = "{\"id\":\"payload1\"}";
    private final String PAYLOAD2 = "{\"id\":\"payload2\"}";

    @Before
    public void setUp() {

        Instant instant = OffsetDateTime.parse(dateIso).toInstant();

        application = Application.builder()
        		.name("YOicfcBMre")
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();

        Device device = Device.builder().deviceId("id1").name("name1").guid("guid1").active(true).build();
        EventActor eventActor = EventActor.builder().deviceGuid(device.getGuid()).deviceId(device.getId()).channel("temp").build();;

        event1 = Event.builder().creationTimestamp(instant).ingestedTimestamp(instant).incoming(eventActor).payload(PAYLOAD1).build();
        event2 = Event.builder().creationTimestamp(instant).ingestedTimestamp(instant).incoming(eventActor).payload(PAYLOAD2).build();

    }

    @After
    public void tearDown() {
        Mockito.reset(deviceEventService);
    }

    @Test
    public void shouldTryToListWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NAME))
            .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + NONEXIST_APPLICATION_NAME + "/incomingEvents")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
                    .andExpect(jsonPath("$.result").doesNotExist())
                    ;

    }

    @Test
    public void shouldListEventsNoParam() throws Exception {

        List<Event> incomingEvents = new ArrayList<>();
        incomingEvents.add(event1);
        incomingEvents.add(event2);

        when(deviceEventService.findIncomingBy(org.mockito.Matchers.any(Tenant.class),
                org.mockito.Matchers.any(Application.class),
                org.mockito.Matchers.any(User.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.eq(false),
                org.mockito.Matchers.eq(100)))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(incomingEvents).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
        		.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[0].payload", is(JSON.parse(PAYLOAD1))))
                    .andExpect(jsonPath("$.result[1].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[1].payload", is(JSON.parse(PAYLOAD2))))
                    ;

    }

    @Test
    public void shouldListEventsWithQuery() throws Exception {

        List<Event> incomingEvents = new ArrayList<>();
        incomingEvents.add(event1);
        incomingEvents.add(event2);

        when(deviceEventService.findIncomingBy(org.mockito.Matchers.any(Tenant.class),
                org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(User.class),
                org.mockito.Matchers.eq("0000"),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.eq("temp"),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.eq(false),
                org.mockito.Matchers.eq(100)))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(incomingEvents).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .contentType("application/json")
                                                   .param("q", "channel:temp device:0000")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[0].payload", is(JSON.parse(PAYLOAD1))))
                    .andExpect(jsonPath("$.result[1].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[1].payload", is(JSON.parse(PAYLOAD2))))
                    ;

    }

    @Test
    public void shouldListEventsSortOldest() throws Exception {

        List<Event> incomingEvents = new ArrayList<>();
        incomingEvents.add(event1);
        incomingEvents.add(event2);

        when(deviceEventService.findIncomingBy(org.mockito.Matchers.any(Tenant.class),
                org.mockito.Matchers.any(Application.class),
                org.mockito.Matchers.any(User.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.eq(true),
                org.mockito.Matchers.eq(100)))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(incomingEvents).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .param("sort", "oldest")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[0].payload", is(JSON.parse(PAYLOAD1))))
                    .andExpect(jsonPath("$.result[1].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[1].payload", is(JSON.parse(PAYLOAD2))))
                    ;

    }

    @Test
    public void shouldListEventsWithLimit() throws Exception {

        List<Event> incomingEvents = new ArrayList<>();
        incomingEvents.add(event1);
        incomingEvents.add(event2);

        when(deviceEventService.findIncomingBy(org.mockito.Matchers.any(Tenant.class),
                org.mockito.Matchers.any(Application.class),
                org.mockito.Matchers.any(User.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(String.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.isNull(Instant.class),
                org.mockito.Matchers.eq(false),
                org.mockito.Matchers.eq(500)))
                .thenReturn(ServiceResponseBuilder.<List<Event>>ok().withResult(incomingEvents).build());

        when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .param("limit", "500")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[0].payload", is(JSON.parse(PAYLOAD1))))
                    .andExpect(jsonPath("$.result[1].timestamp", is(dateIso)))
                    .andExpect(jsonPath("$.result[1].payload", is(JSON.parse(PAYLOAD2))))
                    ;

    }

    @Test
    public void shouldListEventsWithInvalidLimit() throws Exception {
    	when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .param("limit", "90000")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Invalid limit. Max: 50000")))
                    .andExpect(jsonPath("$.result").doesNotExist())
                    ;

    }

    @Test
    public void shouldListEventsInvalidQuery() throws Exception {
    	when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/" + application.getName() + "/incomingEvents")
                                                   .param("q", "qqq=qqq")
                                                   .contentType("application/json")
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                    .andExpect(jsonPath("$.messages[0]", is("Invalid filter: qqq=qqq")))
                    .andExpect(jsonPath("$.result").doesNotExist())
                    ;

    }

}
