package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.DeviceController;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceControllerTest.DeviceTestContextConfig.class
})
public class DeviceControllerTest extends WebLayerTestContext {

    private static final String USER_DEFINED_DEVICE_ID = "SN1234567890";
    private static final String DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";
    
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    DeviceRegisterService deviceRegisterService;
    @Autowired
    DeviceEventService deviceEventService;
    @Autowired
    private Tenant tenant;

    private List<Device> registeredDevices;
    private NewServiceResponse<Device> response;
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
        deviceData.add("deviceId", USER_DEFINED_DEVICE_ID);
        deviceData.add("description", "Some description");
        deviceData.add("guid", DEVICE_GUID);

        Device.DeviceBuilder builder = Device.builder().deviceId(deviceData.getFirst("deviceId")).name(deviceData.getFirst("name"))
                .description(deviceData.getFirst("description")).guid(deviceData.getFirst("guid"));

        device = builder.build();

        savedDevice = builder.id("deviceId").build();

        deviceForm = new DeviceRegistrationForm();
        deviceForm.setDeviceId(device.getDeviceId());
        deviceForm.setName(device.getName());
        deviceForm.setDescription(device.getDescription());
        deviceForm.setGuid(DEVICE_GUID);
    }

    @After
    public void tearDown() {
        Mockito.reset(deviceRegisterService);
    }

    @Test
    public void shouldListAllRegisteredDevices() throws Exception {
        when(deviceRegisterService.findAll(tenant))
        .thenReturn(ServiceResponseBuilder.<List<Device>>ok()
                .withResult(registeredDevices).build());

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
        response = ServiceResponseBuilder.<Device>error()
                .withMessage(CommonValidations.RECORD_NULL.getCode(), null).build();

        when(deviceRegisterService.register(eq(tenant),eq(device))).thenReturn(response);

        getMockMvc().perform(post("/devices/save").params(deviceData))
                .andExpect(model().attribute("errors",
                    equalTo(new ArrayList() {{
                        add(applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH));
                    }})))
                .andExpect(model().attribute("method", ""))
                .andExpect(model().attribute("device", equalTo(deviceForm))).andExpect(view().name("devices/form"));

        verify(deviceRegisterService).register(eq(tenant),eq(device));
    }

    @Test
    public void shouldRedirectToShowAfterRegistrationSucceed() throws Exception {
        response = ServiceResponseBuilder.<Device>ok()
                .withResult(savedDevice).build();

        when(deviceRegisterService.register(eq(tenant),eq(device))).thenReturn(response);

        getMockMvc().perform(post("/devices/save").params(deviceData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(DeviceController.Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/devices/{0}", savedDevice.getGuid())));

        verify(deviceRegisterService).register(eq(tenant),eq(device));
    }

    @Test
    public void shouldShowDeviceDetails() throws Exception {
        savedDevice.setRegistrationDate(Instant.now());
        when(deviceRegisterService.getByDeviceGuid(tenant, savedDevice.getGuid()))
            .thenReturn(
                ServiceResponseBuilder.<Device>ok().withResult(savedDevice).build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}", savedDevice.getGuid())))
                .andExpect(model().attribute("device", savedDevice)).andExpect(view().name("devices/show"));

        verify(deviceRegisterService).getByDeviceGuid(tenant, savedDevice.getGuid());
    }

    @Test
    public void shouldShowDeviceEventList() throws Exception {
        savedDevice.setRegistrationDate(Instant.now());
        when(deviceRegisterService.getByDeviceGuid(tenant, savedDevice.getGuid())).thenReturn(
                ServiceResponseBuilder.<Device>ok().withResult(savedDevice).build());
        when(deviceEventService.findIncomingBy(tenant,savedDevice.getGuid(),null,null,50)).thenReturn(
            ServiceResponseBuilder.<List<Event>>ok().withResult(Collections.emptyList()).build()
        );

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}/events", savedDevice.getGuid())))
                .andExpect(model().attribute("device", savedDevice)).andExpect(view().name("devices/events"));

        verify(deviceRegisterService).getByDeviceGuid(tenant, savedDevice.getGuid());
        verify(deviceEventService).findIncomingBy(tenant, savedDevice.getGuid(),null,null,50);
    }

    @Test
    public void shouldShowEditForm() throws Exception {
        when(deviceRegisterService.getByDeviceGuid(tenant, savedDevice.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(savedDevice).build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}/edit", savedDevice.getGuid())))
                .andExpect(model().attribute("device", equalTo(deviceForm)))
                .andExpect(model().attribute("isEditing",true))
                .andExpect(model().attribute("action", MessageFormat.format("/devices/{0}",savedDevice.getGuid())))
                .andExpect(model().attribute("method", "put"))
                .andExpect(view().name("devices/form"));
    }

    @Test
    public void shouldBindErrorMessagesWhenEditFailsAndGoBackToEditForm() throws Exception {
        response = ServiceResponseBuilder.<Device>error()
                .withMessage(CommonValidations.RECORD_NULL.getCode(), null).build();

        when(deviceRegisterService.update(Matchers.anyObject(), Matchers.anyString(), Matchers.anyObject())).thenReturn(response);

        getMockMvc().perform(put(MessageFormat.format("/devices/{0}", DEVICE_GUID)).params(deviceData))
                .andExpect(model().attribute("errors",
                    equalTo(new ArrayList() {{
                        add(applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null, Locale.ENGLISH));
                    }})))
                .andExpect(model().attribute("device", equalTo(deviceForm)))
                .andExpect(model().attribute("method", "put"))
                .andExpect(view().name("devices/form"));

        verify(deviceRegisterService).update(eq(tenant), eq(DEVICE_GUID), eq(device));
    }

    @Test
    public void shouldRedirectToShowAfterEditSucceed() throws Exception {
        response = ServiceResponseBuilder.<Device>ok()
                .withResult(savedDevice).build();

        when(deviceRegisterService.update(eq(tenant), eq(savedDevice.getId()), eq(device))).thenReturn(response);

        getMockMvc().perform(put(MessageFormat.format("/devices/{0}", savedDevice.getId())).params(deviceData))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(DeviceController.Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)))
                .andExpect(redirectedUrl(MessageFormat.format("/devices/{0}", savedDevice.getGuid())));

        verify(deviceRegisterService).update(eq(tenant), eq(savedDevice.getId()), eq(device));
    }

    @Test
    public void shouldRedirectToListDevicesAndShowSuccessMessageAfterDeletionSucceed() throws Exception {
        device.setId(USER_DEFINED_DEVICE_ID);
        NewServiceResponse<Device> responseRemoval = ServiceResponseBuilder.<Device>ok()
                .withResult(device).build();

        NewServiceResponse<List<Device>> responseListAll = ServiceResponseBuilder.<List<Device>>ok()
                .withResult(registeredDevices).build();

        spy(responseRemoval);
        spy(responseListAll);

        when(deviceRegisterService.remove(tenant, device.getId())).thenReturn(responseRemoval);
        when(deviceRegisterService.findAll(eq(tenant))).thenReturn(responseListAll);

        getMockMvc().perform(delete("/devices/{0}", device.getId()))
                .andExpect(flash().attribute("message",
                        applicationContext.getMessage(DeviceController.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(),null,Locale.ENGLISH)
                ))
                .andExpect(redirectedUrl("/devices"));

        verify(deviceRegisterService).remove(tenant, device.getId());
    }

    @Configuration
    static class DeviceTestContextConfig {
        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }
        @Bean
        public DeviceEventService deviceEventService() { return Mockito.mock(DeviceEventService.class); }
    }
}