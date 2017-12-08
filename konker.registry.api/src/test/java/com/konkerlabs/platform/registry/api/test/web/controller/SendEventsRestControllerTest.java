package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
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
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.IncomingEventsRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = IncomingEventsRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class SendEventsRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;
    private Application application;
    
    private String jsonPayload = "{\"tem\":27}";

    @Before
    public void setUp() {
        application = Application.builder()
        		.name("YOicfcBMre")
        		.friendlyName("Smart Frig")
        		.description("Description of smartff")
        		.qualifier(tenant.getName())
        		.registrationDate(Instant.now())
        		.build();


    }

    @After
    public void tearDown() {
        Mockito.reset(deviceEventService);
    }

    @Test
    public void shouldTryToSendWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
            .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/" + NONEXIST_APPLICATION_NANE + "/sendEvents")
        		.content(jsonPayload)
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
        
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/text;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("Application does not exist")))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldListEventsNoParam() throws Exception {

        List<Event> incomingEvents = new ArrayList<>();
//        incomingEvents.add(event1);
//        incomingEvents.add(event2);

        when(deviceEventService.findIncomingBy(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.isNull(String.class), org.mockito.Matchers.isNull(String.class), org.mockito.Matchers.isNull(Instant.class), org.mockito.Matchers.isNull(Instant.class), org.mockito.Matchers.eq(false), org.mockito.Matchers.eq(100)))
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
//                    .andExpect(jsonPath("$.result[0].timestamp", is(dateIso)))
//                    .andExpect(jsonPath("$.result[0].payload", is(JSON.parse(PAYLOAD1))))
//                    .andExpect(jsonPath("$.result[1].timestamp", is(dateIso)))
//                    .andExpect(jsonPath("$.result[1].payload", is(JSON.parse(PAYLOAD2))))
                    ;

    }

}
