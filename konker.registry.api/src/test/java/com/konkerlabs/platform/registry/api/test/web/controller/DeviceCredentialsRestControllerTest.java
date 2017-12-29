package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;

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
import com.konkerlabs.platform.registry.api.web.controller.DeviceCredentialRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceCredentialRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceCredentialsRestControllerTest extends WebLayerTestContext {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    private Device device1;

    private String BASEPATH = "deviceCredentials";

    @Before
    public void setUp() {
        device1 = Device.builder().deviceId("id1").name("name1").guid("guid1").apiKey("apiKey1").application(application).active(true).build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldReadDevice() throws Exception {

        DeviceRegisterService.DeviceDataURLs deviceDataURLs = new DeviceDataURLs(device1, Language.EN.getLocale());

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.getDeviceDataURLs(tenant, application, device1, Language.EN.getLocale()))
                .thenReturn(ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                    .contentType("application/json")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.username", is("apiKey1")))
                    .andExpect(jsonPath("$.result.password").doesNotExist())
                    .andExpect(jsonPath("$.result.httpsURLPub", is("https://dev-server:443/pub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpsURLSub", is("https://dev-server:443/sub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpURLPub", is("http://dev-server:8080/pub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpURLSub", is("http://dev-server:8080/sub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttPubTopic", is("data/apiKey1/pub/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttSubTopic", is("data/apiKey1/sub/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttURL", is("mqtt://dev-server:1883")))
                    .andExpect(jsonPath("$.result.mqttsURL", is("mqtts://dev-server:1883")));

    }

    @Test
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.password").doesNotExist());

    }

    @Test
    public void shouldCreateDevice() throws Exception {

        DeviceRegisterService.DeviceSecurityCredentials credentials = new DeviceRegisterService.DeviceSecurityCredentials(device1, "7I5ccJHCIE");
        
        DeviceRegisterService.DeviceDataURLs deviceDataURLs =  new DeviceDataURLs(device1, Language.EN.getLocale());

        when(deviceRegisterService.generateSecurityPassword(tenant, application, device1.getGuid()))
                 .thenReturn(ServiceResponseBuilder.<DeviceRegisterService.DeviceSecurityCredentials>ok().withResult(credentials).build());

        when(deviceRegisterService.getDeviceDataURLs(tenant, application, device1, Language.EN.getLocale()))
                .thenReturn(ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.username", is("apiKey1")))
                    .andExpect(jsonPath("$.result.password", is("7I5ccJHCIE")))
                    .andExpect(jsonPath("$.result.httpsURLPub", is("https://dev-server:443/pub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpsURLSub", is("https://dev-server:443/sub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpURLPub", is("http://dev-server:8080/pub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.httpURLSub", is("http://dev-server:8080/sub/apiKey1/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttPubTopic", is("data/apiKey1/pub/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttSubTopic", is("data/apiKey1/sub/<Channel>")))
                    .andExpect(jsonPath("$.result.mqttURL", is("mqtt://dev-server:1883")))
                    .andExpect(jsonPath("$.result.mqttsURL", is("mqtts://dev-server:1883")));

    }

    @Test
    public void shouldTryCreateDeviceWithBadRequest() throws Exception {

        DeviceRegisterService.DeviceSecurityCredentials credentials = new DeviceRegisterService.DeviceSecurityCredentials(device1, "7I5ccJHCIE");

        when(deviceRegisterService.generateSecurityPassword(tenant, application, device1.getGuid()))
                 .thenReturn(ServiceResponseBuilder.<DeviceRegisterService.DeviceSecurityCredentials>error().withResult(credentials).build());

        getMockMvc().perform(MockMvcRequestBuilders.post(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, device1.getGuid()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

}
