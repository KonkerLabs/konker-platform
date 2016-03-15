package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceControllerTest.DeviceTestContextConfig.class
})
public class DeviceControllerTest extends WebLayerTestContext {

    private static final String DEVICE_ID_95C14B36BA2B43F1 = "95c14b36ba2b43f1";

    @Autowired
    DeviceRegisterService deviceRegisterService;
    @Autowired
    private Tenant tenant;

    private List<Device> registeredDevices;
    private ServiceResponse<Device> response;
    MultiValueMap<String, String> deviceData;
    private Device device;
    private DeviceRegistrationForm deviceForm;
    private Device savedDevice;

    @Before
    public void setUp() {
        registeredDevices = new ArrayList<>();
        registeredDevices.add(Device.builder().build());

        deviceData = new LinkedMultiValueMap<>();
        deviceData.add("name", "Device name");
        deviceData.add("deviceId", DEVICE_ID_95C14B36BA2B43F1);
        deviceData.add("description", "Some description");

        Device.DeviceBuilder builder = Device.builder().deviceId(deviceData.getFirst("deviceId")).name(deviceData.getFirst("name"))
                .description(deviceData.getFirst("description"));

        device = builder.build();

        savedDevice = builder.id("deviceId").build();

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
        when(deviceRegisterService.findAll(tenant))
        .thenReturn(ServiceResponse.<List<Device>>builder()
                .status(ServiceResponse.Status.OK)
                .result(registeredDevices)
                .build());

        getMockMvc().perform(get("/devices")).andExpect(model().attribute("devices", equalTo(registeredDevices)))
                .andExpect(view().name("devices/index"));
    }

    @Test
    public void shouldShowRegistrationForm() throws Exception {
        getMockMvc().perform(get("/devices/new"))
                .andExpect(view().name("devices/form"))
                .andExpect(model().attribute("device",any(DeviceRegistrationForm.class)))
                .andExpect(model().attribute("action","/devices/save"));
    }

    @Test
    public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
        response = ServiceResponse.<Device>builder().responseMessages(Arrays.asList(new String[] { "Some error" }))
                .status(ServiceResponse.Status.ERROR).<Device>build();

        when(deviceRegisterService.register(eq(tenant),eq(device))).thenReturn(response);

        getMockMvc().perform(post("/devices/save").params(deviceData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("device", equalTo(deviceForm))).andExpect(view().name("devices/form"));

        verify(deviceRegisterService).register(eq(tenant),eq(device));
    }

    @Test
    public void shouldRedirectToShowAfterRegistrationSucceed() throws Exception {
        response = ServiceResponse.<Device>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDevice)
                .<Device>build();

        when(deviceRegisterService.register(eq(tenant),eq(device))).thenReturn(response);

        getMockMvc().perform(post("/devices/save").params(deviceData))
                .andExpect(flash().attribute("message", "Device saved successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/devices/{0}", savedDevice.getId())));

        verify(deviceRegisterService).register(eq(tenant),eq(device));
    }

    @Test
    public void shouldShowDeviceDetails() throws Exception {
        savedDevice.setRegistrationDate(Instant.now());
        when(deviceRegisterService.getByDeviceId(tenant, savedDevice.getId())).thenReturn(
                ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(savedDevice).<Device>build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}", savedDevice.getId())))
                .andExpect(model().attribute("device", savedDevice)).andExpect(view().name("devices/show"));

        verify(deviceRegisterService).getByDeviceId(tenant, savedDevice.getId());
    }

    @Test
    public void shouldShowDeviceEventList() throws Exception {
        savedDevice.setRegistrationDate(Instant.now());
        when(deviceRegisterService.getByDeviceId(tenant, savedDevice.getId())).thenReturn(
                ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(savedDevice).build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}/events", savedDevice.getId())))
                .andExpect(model().attribute("device", savedDevice)).andExpect(view().name("devices/events"));

        verify(deviceRegisterService).getByDeviceId(tenant, savedDevice.getId());
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        when(deviceRegisterService.getByDeviceId(tenant, savedDevice.getId())).thenReturn(ServiceResponse.<Device>builder().status(ServiceResponse.Status.OK).result(savedDevice).build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}/edit", savedDevice.getId())))
                .andExpect(model().attribute("device", equalTo(deviceForm)))
                .andExpect(model().attribute("isEditing",true))
                .andExpect(model().attribute("action", MessageFormat.format("/devices/{0}",savedDevice.getId())))
                .andExpect(view().name("devices/form"));
    }

    @Test
    public void shouldBindErrorMessagesWhenEditFailsAndGoBackToEditForm() throws Exception {
        response = ServiceResponse.<Device>builder().responseMessages(Arrays.asList(new String[] { "Some error" }))
                .status(ServiceResponse.Status.ERROR).<Device>build();

        when(deviceRegisterService.update(Matchers.anyObject(), Matchers.anyString(), Matchers.anyObject())).thenReturn(response);

        getMockMvc().perform(post(MessageFormat.format("/devices/{0}", DEVICE_ID_95C14B36BA2B43F1)).params(deviceData))
                .andExpect(model().attribute("errors", equalTo(response.getResponseMessages())))
                .andExpect(model().attribute("device", equalTo(deviceForm)))
                .andExpect(view().name("devices/form"));

        verify(deviceRegisterService).update(eq(tenant), eq(DEVICE_ID_95C14B36BA2B43F1), eq(device));
    }

    @Test
    public void shouldRedirectToShowAfterEditSucceed() throws Exception {
        response = ServiceResponse.<Device>builder()
                .status(ServiceResponse.Status.OK)
                .result(savedDevice)
                .<Device>build();

        when(deviceRegisterService.update(eq(tenant), eq(savedDevice.getId()), eq(device))).thenReturn(response);

        getMockMvc().perform(post(MessageFormat.format("/devices/{0}", savedDevice.getId())).params(deviceData))
                .andExpect(flash().attribute("message", "Device saved successfully"))
                .andExpect(redirectedUrl(MessageFormat.format("/devices/{0}", savedDevice.getId())));

        verify(deviceRegisterService).update(eq(tenant), eq(savedDevice.getId()), eq(device));
    }

    @Configuration
    static class DeviceTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }
    }
}