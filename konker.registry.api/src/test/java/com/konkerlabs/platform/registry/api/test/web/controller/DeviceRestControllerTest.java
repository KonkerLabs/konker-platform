package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;;

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

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private Tenant tenant;

    private Device device1;

    private Device device2;

    @Before
    public void setUp() {
        device1 = Device.builder().deviceId("id1").name("name1").guid("guid1").active(true).build();
        device2 = Device.builder().deviceId("id2").name("name2").guid("guid2").active(false).build();
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

        when(deviceRegisterService.findAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<Device>>ok().withResult(devices).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/devices/")
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

        when(deviceRegisterService.findAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<Device>>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/devices/")
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

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/devices/" + device1.getGuid())
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
    public void shouldTryReadDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/devices/" + device1.getGuid())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateDevice() throws Exception {

        when(deviceRegisterService.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/devices/")
                                                   .content(getJson(new DeviceVO(device1)))
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
                    .andExpect(jsonPath("$.result.active", is(true)));

    }

    @Test
    public void shouldTryCreateDeviceWithBadRequest() throws Exception {

        when(deviceRegisterService.register(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.post("/devices/")
                .content(getJson(new DeviceVO(device1)))
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

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/devices/" + device1.getGuid())
                .content(getJson(new DeviceVO(device1)))
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

        when(deviceRegisterService.getByDeviceGuid(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device1).build());

        when(deviceRegisterService.update(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.any(Device.class)))
                .thenReturn(ServiceResponseBuilder.<Device>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/devices/" + device1.getGuid())
                .content(getJson(new DeviceVO(device1)))
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

        when(deviceRegisterService.remove(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/devices/" + device1.getGuid())
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
    public void shouldTryDeleteDeviceWithInternalError() throws Exception {

        when(deviceRegisterService.remove(tenant, device1.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>error().withMessage(DeviceRegisterService.Messages.DEVICE_REMOVED_UNSUCCESSFULLY.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.delete("/devices/" + device1.getGuid())
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


}
