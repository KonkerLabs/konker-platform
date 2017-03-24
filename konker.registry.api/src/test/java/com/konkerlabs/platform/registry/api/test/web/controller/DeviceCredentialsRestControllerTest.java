package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

/*@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceCredentialRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})*/
public class DeviceCredentialsRestControllerTest extends WebLayerTestContext {

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private User user;

    private Device device1;

    @Before
    public void setUp() {
        device1 = Device.builder().deviceId("id1").name("name1").guid("guid1").apiKey("apiKey1").active(true).build();
    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldReadDevice() throws Exception {

        DeviceRegisterService.DeviceDataURLs deviceDataURLs = DeviceDataURLs
                .builder()
                .httpsURLPub("httpsURLPub")
                .httpsURLSub("httpsURLSub")
                .httpURLPub("httpURLPub")
                .httpURLSub("httpURLSub")
                .mqttPubTopic("mqttPubTopic")
                .mqttSubTopic("mqttSubTopic")
                .mqttURL("mqttURL")
                .mqttsURL("mqttsURL")
                .build();

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.getDeviceDataURLs(tenant, device1, user.getLanguage().getLocale()))
                .thenReturn(ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/deviceCredentials/" + device1.getGuid())
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
                    .andExpect(jsonPath("$.result.httpsURLPub", is("httpsURLPub")))
                    .andExpect(jsonPath("$.result.httpsURLSub", is("httpsURLSub")))
                    .andExpect(jsonPath("$.result.httpURLPub", is("httpURLPub")))
                    .andExpect(jsonPath("$.result.httpURLSub", is("httpURLSub")))
                    .andExpect(jsonPath("$.result.mqttPubTopic", is("mqttPubTopic")))
                    .andExpect(jsonPath("$.result.mqttSubTopic", is("mqttSubTopic")))
                    .andExpect(jsonPath("$.result.mqttURL", is("mqttURL")))
                    .andExpect(jsonPath("$.result.mqttsURL", is("mqttsURL")));

    }

    @Test
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/deviceCredentials/" + device1.getGuid())
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

        DeviceRegisterService.DeviceDataURLs deviceDataURLs = DeviceDataURLs
                .builder()
                .httpsURLPub("httpsURLPub")
                .httpsURLSub("httpsURLSub")
                .httpURLPub("httpURLPub")
                .httpURLSub("httpURLSub")
                .mqttPubTopic("mqttPubTopic")
                .mqttSubTopic("mqttSubTopic")
                .mqttURL("mqttURL")
                .mqttsURL("mqttsURL")
                .build();

        when(deviceRegisterService.generateSecurityPassword(tenant, device1.getGuid()))
                 .thenReturn(ServiceResponseBuilder.<DeviceRegisterService.DeviceSecurityCredentials>ok().withResult(credentials).build());

        when(deviceRegisterService.getDeviceDataURLs(tenant, device1, user.getLanguage().getLocale()))
                .thenReturn(ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/deviceCredentials/" + device1.getGuid())
                                                   .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                    .andExpect(jsonPath("$.result").isMap())
                    .andExpect(jsonPath("$.result.username", is("apiKey1")))
                    .andExpect(jsonPath("$.result.password", is("7I5ccJHCIE")))
                    .andExpect(jsonPath("$.result.httpsURLPub", is("httpsURLPub")))
                    .andExpect(jsonPath("$.result.httpsURLSub", is("httpsURLSub")))
                    .andExpect(jsonPath("$.result.httpURLPub", is("httpURLPub")))
                    .andExpect(jsonPath("$.result.httpURLSub", is("httpURLSub")))
                    .andExpect(jsonPath("$.result.mqttPubTopic", is("mqttPubTopic")))
                    .andExpect(jsonPath("$.result.mqttSubTopic", is("mqttSubTopic")))
                    .andExpect(jsonPath("$.result.mqttURL", is("mqttURL")))
                    .andExpect(jsonPath("$.result.mqttsURL", is("mqttsURL")));

    }

    @Test
    public void shouldTryCreateDeviceWithBadRequest() throws Exception {

        DeviceRegisterService.DeviceSecurityCredentials credentials = new DeviceRegisterService.DeviceSecurityCredentials(device1, "7I5ccJHCIE");

        when(deviceRegisterService.generateSecurityPassword(tenant, device1.getGuid()))
                 .thenReturn(ServiceResponseBuilder.<DeviceRegisterService.DeviceSecurityCredentials>error().withResult(credentials).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/deviceCredentials/" + device1.getGuid())
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
