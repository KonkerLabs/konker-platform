package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
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

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfig.class,DeviceControllerTest.DeviceTestContextConfig.class})
public class DeviceControllerTest extends WebLayerTestContext {

    @Autowired
    DeviceRegisterService deviceRegisterService;

    private List<Device> registeredDevices;
    private ServiceResponse response;
    MultiValueMap<String,String> deviceData;
    private Device device;
    private DeviceRegistrationForm deviceForm;

    @Before
    public void setUp() {
        registeredDevices = new ArrayList<>();
        registeredDevices.add(Device.builder().build());

        deviceData = new LinkedMultiValueMap<>();
        deviceData.add("name","Device name");
        deviceData.add("deviceId","95c14b36ba2b43f1");
        deviceData.add("description","Some description");

        device = Device.builder()
            .deviceId(deviceData.getFirst("deviceId"))
            .name(deviceData.getFirst("name"))
            .description(deviceData.getFirst("description"))
            .build();

        deviceForm = new DeviceRegistrationForm();
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

        getMockMvc().perform(get("/devices"))
            .andExpect(model().attribute("devices",equalTo(registeredDevices)))
            .andExpect(view().name("layout:devices/index"));
    }

    @Test
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/devices/new"))
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
    public void shouldBindExceptionMessageWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        String exceptionMessage = "Some business exception message";

        when(deviceRegisterService.register(eq(device))).thenThrow(new BusinessException(exceptionMessage));

        getMockMvc().perform(
                post("/devices/save").params(deviceData))
                .andExpect(model().attribute("errors",equalTo(Arrays.asList(new String[] {exceptionMessage}))))
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
                .andExpect(flash().attribute("message","Device registered successfully"))
                .andExpect(redirectedUrl("/registry/devices"));

        verify(deviceRegisterService).register(eq(device));
    }

    @Test
    public void shouldShowDeviceDetails() throws Exception {
        device.setRegistrationDate(Instant.now());
        when(deviceRegisterService.findById(device.getDeviceId())).thenReturn(device);

        getMockMvc().perform(
            get("/devices/show").param("deviceId",device.getDeviceId())
        ).andExpect(model().attribute("device",device)
        ).andExpect(view().name("layout:devices/show"));

        verify(deviceRegisterService).findById(device.getDeviceId());
    }

    @Test
    public void shouldShowDeviceEventList() throws Exception {
        device.setRegistrationDate(Instant.now());
        when(deviceRegisterService.findById(device.getDeviceId())).thenReturn(device);

        getMockMvc().perform(
                get(MessageFormat.format("/devices/{0}/events",device.getDeviceId()))
        ).andExpect(model().attribute("device",device)
        ).andExpect(view().name("layout:devices/events"));

        verify(deviceRegisterService).findById(device.getDeviceId());
    }

    @Configuration
    static class DeviceTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }
    }
}