package com.konkerlabs.platform.registry.test.controllers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.WebIntegrationTestContext;
import com.konkerlabs.platform.registry.web.controllers.DeviceController;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfig.class,DeviceControllerTest.DeviceTestContextConfig.class})
public class DeviceControllerTest extends WebIntegrationTestContext {

    @Autowired
    DeviceRegisterService deviceRegisterService;

    private List<Device> registeredDevices;
    private ServiceResponse response;
    MultiValueMap<String,String> deviceData;
    private Device device;
    private DeviceController.DeviceRegistrationForm deviceForm;

    @Before
    public void setUp() {
        registeredDevices = new ArrayList<>();
        registeredDevices.add(Device.builder().build());

        deviceData = new LinkedMultiValueMap<>();
        deviceData.add("name","Device name");
        deviceData.add("deviceId","95c14b36ba2b43f1");

        device = Device.builder()
            .deviceId(deviceData.getFirst("deviceId"))
            .name(deviceData.getFirst("name"))
            .build();

        deviceForm = new DeviceController.DeviceRegistrationForm();
        deviceForm.setDeviceId(device.getDeviceId());
        deviceForm.setName(device.getName());
        deviceForm.setDescription(device.getDescription());
    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldListAllRegisteredDevices() throws Exception {
        when(deviceRegisterService.getAll()).thenReturn(registeredDevices);

        getMockMvc().perform(get("/devices/"))
            .andExpect(model().attribute("devices",equalTo(registeredDevices)))
            .andExpect(view().name("layout:devices/index"));
    }

    @Test
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/devices/new"))
            .andExpect(model().attribute("device", Matchers.isA(Device.class)))
            .andExpect(view().name("layout:devices/form"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        response = ServiceResponse.builder()
            .responseMessages(Arrays.asList(new String[]{"Some error"}))
            .status(ServiceResponse.Status.ERROR).build();

        when(deviceRegisterService.register(eq(device))).thenReturn(response);

        getMockMvc().perform(
            post("/devices/save").params(deviceData))
            .andExpect(model().attribute("errors",equalTo(response.getResponseMessages())))
            .andExpect(model().attribute("device",equalTo(deviceForm)))
            .andExpect(view().name("layout:devices/form"));

        verify(deviceRegisterService).register(eq(device));
    }

    @Test
    public void shouldRedirectToListAfterRegistrationSucceed() throws Exception {
        response = ServiceResponse.builder()
                .status(ServiceResponse.Status.OK).build();

        when(deviceRegisterService.register(eq(device))).thenReturn(response);

        getMockMvc().perform(
                post("/devices/save").params(deviceData))
                .andExpect(model().attribute("message","Device registered successfully"))
                .andExpect(view().name("layout:devices/index"));

        verify(deviceRegisterService).register(eq(device));
    }

    @Configuration
    static class DeviceTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }
    }
}