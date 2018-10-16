package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceInputVO;
import com.konkerlabs.platform.registry.api.model.GatewayVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.GatewayRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.idm.services.OAuth2AccessTokenService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceSecurityCredentials;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService.DeviceDataURLs;
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
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GatewayRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class GatewayRestControllerTest extends WebLayerTestContext {

    private static final String NONEXIST_APPLICATION_NANE = "AppLost";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private OAuth2AccessTokenService oAuth2AccessTokenService;

    private Location location;

    private Gateway gateway;

    private Device device;

    private DeviceInputVO deviceHumidity;

    private DeviceInputVO deviceTemp;

    private DeviceSecurityCredentials deviceSecurityCredentials;

    private DeviceDataURLs deviceDataURLs;

    private List<DeviceInputVO> devices = new ArrayList<>();

    private final String BASEPATH = "gateways";

    private final String INVALID_GUID = "000000-aaa";

    @Before
    public void setUp() {

        location = Location.builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .name("13th floor")
                .build();

        gateway = new Gateway();
        gateway.setName("hdxzbgh2ti");
        gateway.setDescription("w2f4ep5ksu");
        gateway.setGuid("2cdc391d-6a31-4103-9679-52cb6f2e5df5");
        gateway.setTenant(tenant);
        gateway.setApplication(application);
        gateway.setLocation(location);


        device = Device.builder()
                .deviceId("sendorHumidity")
                .name("Humidity")
                .apiKey("apikey")
                .application(application)
                .location(location)
                .build();
        deviceSecurityCredentials = new DeviceSecurityCredentials(device, "xpto123");
        deviceDataURLs = new DeviceDataURLs(device, Locale.ENGLISH);

        deviceHumidity = new DeviceInputVO();
        deviceHumidity.setId("sendorHumidity");
        deviceHumidity.setName("Humidity");
        devices.add(deviceHumidity);

        deviceTemp = new DeviceInputVO();
        deviceTemp.setId("sendorTemp");
        deviceTemp.setName("Temperature");
        devices.add(deviceTemp);

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(locationSearchService.findByName(tenant, application, location.getName(), false))
            .thenReturn(ServiceResponseBuilder.<Location> ok().withResult(location).build());

        when(gatewayService.getByGUID(tenant, application, gateway.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Gateway> ok().withResult(gateway).build());

        when(gatewayService.getByGUID(tenant, application, INVALID_GUID))
                .thenReturn(ServiceResponseBuilder.<Gateway> error().withMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
        Mockito.reset(oAuth2AccessTokenService);
    }

    @Test
    public void shouldListGateways() throws Exception {

        List<Gateway> gateways = new ArrayList<>();
        gateways.add(gateway);

        when(gatewayService.getAll(tenant, application))
                .thenReturn(ServiceResponseBuilder.<List<Gateway>>ok().withResult(gateways).build());

        getMockMvc().perform(MockMvcRequestBuilders.get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].name", is(gateway.getName())))
                .andExpect(jsonPath("$.result[0].description", is(gateway.getDescription())))
                .andExpect(jsonPath("$.result[0].locationName", is(gateway.getLocation().getName())))
                .andExpect(jsonPath("$.result[0].guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result[0].active", is(false)))
                ;

    }

    @Test
    public void shouldReadGateway() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.name", is(gateway.getName())))
                .andExpect(jsonPath("$.result.guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                ;

    }

    @Test
    public void shouldReadWithWrongApplication() throws Exception {

        when(applicationService.getByApplicationName(tenant, NONEXIST_APPLICATION_NANE))
                .thenReturn(ServiceResponseBuilder.<Application>error().withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
                    .get(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, location.getName()))
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
    public void shouldReturnNotFoundWhenReadByGuid() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldCreateDevicesByGateway() throws Exception {
        when(gatewayService.getByGUID(any(Tenant.class), any(Application.class), anyString()))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .withResult(gateway)
                    .build());

        when(deviceRegisterService.register(any(Tenant.class), any(Application.class), any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok()
                        .withResult(device)
                        .build());

        when(deviceRegisterService.generateSecurityPassword(any(Tenant.class), any(Application.class), anyString()))
                .thenReturn(ServiceResponseBuilder.<DeviceSecurityCredentials>ok()
                        .withResult(deviceSecurityCredentials)
                        .build());

        when(deviceRegisterService.getDeviceDataURLs(any(Tenant.class), any(Application.class), any(Device.class), any(Locale.class)))
                .thenReturn(ServiceResponseBuilder.<DeviceDataURLs>ok()
                        .withResult(deviceDataURLs)
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/devices", application.getName(), BASEPATH, "abdc-guid-gateway"))
                .content(getJson(devices))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].id", is("sendorHumidity")))
                .andExpect(jsonPath("$.result[0].name", is("Humidity")))
                .andExpect(jsonPath("$.result[0].username", is("apikey")))
                .andExpect(jsonPath("$.result[0].password", is("xpto123")))
                .andExpect(jsonPath("$.result[0].httpURLPub", is("http://dev-server:8080/pub/apikey/<Channel>")))
                .andExpect(jsonPath("$.result[0].httpURLSub", is("http://dev-server:8080/sub/apikey/<Channel>")))
                .andExpect(jsonPath("$.result[0].httpsURLPub", is("https://dev-server:443/pub/apikey/<Channel>")))
                .andExpect(jsonPath("$.result[0].httpsURLSub", is("https://dev-server:443/sub/apikey/<Channel>")))
                .andExpect(jsonPath("$.result[0].mqttURL", is("mqtt://dev-server:1883")))
                .andExpect(jsonPath("$.result[0].mqttsURL", is("mqtts://dev-server:1883")))
                .andExpect(jsonPath("$.result[0].mqttPubTopic", is("data/apikey/pub/<Channel>")))
                .andExpect(jsonPath("$.result[0].mqttSubTopic", is("data/apikey/sub/<Channel>")));

    }

    @Test
    public void shouldTryCreateDevicesByGatewayWithBadRequest() throws Exception {
        when(gatewayService.getByGUID(any(Tenant.class), any(Application.class), anyString()))
                .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                        .withResult(gateway)
                        .build());

        when(deviceRegisterService.register(any(Tenant.class), any(Application.class), any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error()
                        .withMessage(DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode())
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/{2}/devices", application.getName(), BASEPATH, "abdc-guid-gateway"))
                .content(getJson(devices))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Device ID does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateGateway() throws Exception {

        when(gatewayService.save(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.any(Gateway.class)))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .withResult(gateway)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}", application.getName(), BASEPATH))
        		.content(getJson(new GatewayVO().apply(gateway)))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.guid", is(gateway.getGuid())))
                .andExpect(jsonPath("$.result.locationName", is(location.getName())))
                ;

    }

    @Test
    public void shouldTryCreateGatewayWithBadRequest() throws Exception {

        when(gatewayService.save(Mockito.any(Tenant.class), Mockito.any(Application.class), Mockito.any(Gateway.class)))
                .thenReturn(ServiceResponseBuilder.<Gateway>error()
                        .withMessage(GatewayService.Validations.NAME_IN_USE.getCode())
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}", application.getName(), BASEPATH))
                .content(getJson(new GatewayVO().apply(gateway)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Gateway name is already in use")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldUpdateGateway() throws Exception {

        when(gatewayService.update(tenant, application, gateway.getGuid(), gateway))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .withResult(gateway)
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
                .content(getJson(new GatewayVO().apply(gateway)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateInvalidGuid() throws Exception {

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
                .content(getJson(new GatewayVO().apply(gateway)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Gateway not found")))
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReturnInternalErrorWhenUpdateGateway() throws Exception {

        when(gatewayService.update(tenant, application, gateway.getGuid(), gateway))
            .thenReturn(ServiceResponseBuilder.<Gateway>error().build());

    	getMockMvc().perform(MockMvcRequestBuilders
    	        .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
    	        .content(getJson(new GatewayVO().apply(gateway)))
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
    public void shouldDeleteGateway() throws Exception {

        when(gatewayService.remove(tenant, application, gateway.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Gateway>ok()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
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

        getMockMvc().perform(MockMvcRequestBuilders
                    .delete(MessageFormat.format("/{0}/{1}/{2}", NONEXIST_APPLICATION_NANE, BASEPATH, location.getName()))
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
    public void shouldReturnInternalErrorWhenDeleteGateway() throws Exception {

        when(gatewayService.remove(tenant, application, gateway.getGuid()))
            .thenReturn(ServiceResponseBuilder.<Gateway> error().build());

        getMockMvc().perform(MockMvcRequestBuilders
                .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, gateway.getGuid()))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    public void shouldTryDeleteNonExistentGateway() throws Exception {

        when(gatewayService.remove(tenant, application, INVALID_GUID))
                .thenReturn(ServiceResponseBuilder.<Gateway> error().withMessage(GatewayService.Validations.GATEWAY_NOT_FOUND.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders
    	        .delete(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, INVALID_GUID))
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

    @Test
    public void shouldCreateToken() throws Exception {

        OAuth2AccessToken oAuth2AccessToken = new DefaultOAuth2AccessToken("ab66tfz3mw");

        when(oAuth2AccessTokenService.getGatewayAccessToken(tenant, application, gateway))
                .thenReturn(ServiceResponseBuilder.<OAuth2AccessToken> ok().withResult(oAuth2AccessToken).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/{2}/token", application.getName(), BASEPATH, gateway.getGuid()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.access_token", is("ab66tfz3mw")))
        ;

    }

}
