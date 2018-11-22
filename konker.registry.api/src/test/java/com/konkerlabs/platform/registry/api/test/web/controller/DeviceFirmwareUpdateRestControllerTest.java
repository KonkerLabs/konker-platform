package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceFirmwareUpdateInputVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceFirmwareRestController;
import com.konkerlabs.platform.registry.api.web.controller.DeviceFirmwareUpdateRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.services.api.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceFirmwareUpdateRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceFirmwareUpdateRestControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private DeviceFirmwareService deviceFirmwareService;

    @Autowired
    private DeviceFirmwareUpdateService deviceFirmwareUpdateService;


    private DeviceModel deviceModel;

    private DeviceFwUpdate deviceFwUpdate;

    private DeviceFwUpdate deviceFwUpdate2;

    private DeviceFirmware deviceFirmware;

    private Device device;

    private final String BASEPATH = "firmwares";
    private final String BASEPATH_UPDATE = "firmwaresupdates";

    @Before
    public void setUp() {

        deviceModel = DeviceModel.builder()
                .tenant(tenant)
                .application(application)
        		.guid(UUID.randomUUID().toString())
        		.name("air conditioner")
        		.build();

        deviceFirmware = DeviceFirmware.builder()
                .version("0.4.4")
                .deviceModel(deviceModel)
                .uploadDate(Instant.now())
                .build();

        deviceFwUpdate = DeviceFwUpdate.builder()
                .version("0.4.3")
                .status(FirmwareUpdateStatus.UPDATED)
                .application(application)
                .deviceFirmware(deviceFirmware)
                .deviceGuid(UUID.randomUUID().toString())
                .build();




        deviceFwUpdate2 = DeviceFwUpdate.builder()
                .version("0.4.2")
                .status(FirmwareUpdateStatus.UPDATED)
                .application(application)
                .deviceFirmware(deviceFirmware)
                .deviceGuid(UUID.randomUUID().toString())
                .build();

        device = Device.builder()
                .id("LAjQnH2GMB")
                .deviceId("id1")
                .name("name1")
                .guid("guid1")
                .tenant(tenant)
                .application(application)
                .active(true)
                .build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
            .thenReturn(ServiceResponseBuilder.<Application> ok().withResult(application).build());

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> ok().withResult(deviceModel).build());

    }

    @After
    public void tearDown() {
        Mockito.reset(applicationService);
    }

    @Test
    public void shouldTryCreateDeviceFirmwareUpdate() throws Exception {

        when(deviceFirmwareUpdateService.save(tenant, application, device, deviceFirmware))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>ok().withResult(deviceFwUpdate).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post("/")
                .content(getJson(new DeviceFirmwareUpdateInputVO().apply(deviceFwUpdate)))
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
    public void shouldTryCreateDeviceFirmwareUpdateWithBadRequest() throws Exception {


        when(deviceFirmwareUpdateService.save(tenant, application, device, deviceFirmware))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>error()
                .withMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_ALREADY_EXISTS.getCode())
                .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post("/")
                .content(getJson(new DeviceFirmwareUpdateInputVO().apply(deviceFwUpdate)))
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
    public void shouldTrySuspendDeviceFirmwareUpdate() throws Exception {


        when(deviceFirmwareUpdateService.setDeviceAsSuspended(tenant, application,  device))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>ok().withResult(deviceFwUpdate2).build());



        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/", "suspend"))
                .param("deviceGuid", deviceFwUpdate.getDeviceGuid())
                .param("version", "100.0.0")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result[0].uploadTimestamp", notNullValue()))
                .andExpect(jsonPath("$.result[1].version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result[1].uploadTimestamp", notNullValue()));
        ;

    }

    @Test
    public void shouldTrySuspendDeviceFirmwareUpdateWithBadRequest() throws Exception {


        when(deviceFirmwareUpdateService.setDeviceAsSuspended(tenant, application,  device))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>error()
                        .withMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_PENDING_STATUS_DOES_NOT_EXIST.getCode())
                        .build());


        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/", "suspend"))
                .param("deviceGuid", deviceFwUpdate.getDeviceGuid())
                .param("version", "100.0.0")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Firmware_update does not exist")))
                .andExpect(jsonPath("$.result").doesNotExist());




    }

    @Test
    public void shouldListDeviceFirmwaresVersionUpdates() throws Exception {

        List<DeviceFirmware> firmwares = new ArrayList<>();
        firmwares.add(deviceFirmware);
        firmwares.add(deviceFirmware);

        List<DeviceFwUpdate>  firmwaresUpdates = new ArrayList<>();
        firmwares.add(deviceFirmware);
        firmwares.add(deviceFirmware);

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
                .thenReturn(ServiceResponseBuilder.<DeviceModel> ok()
                        .withResult(deviceModel)
                        .build());

        when(deviceFirmwareService.findByVersion(tenant, application, deviceModel, deviceFirmware.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>  ok()
                        .withResult(deviceFirmware)
                        .build());

        when(deviceFirmwareUpdateService.findByDeviceFirmware(tenant, application, deviceFirmware))
                .thenReturn(ServiceResponseBuilder.<List<DeviceFwUpdate>> ok()
                        .withResult(firmwaresUpdates)
                        .build());
        getMockMvc()
                .perform(MockMvcRequestBuilders
                        .get(MessageFormat.format("/{0}/{1}/", deviceModel, deviceFirmware.getVersion()))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result[0].uploadTimestamp", notNullValue()))
                .andExpect(jsonPath("$.result[1].version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result[1].uploadTimestamp", notNullValue()));

    }

    @Test
    public void shouldReturnInternalErrorWhenListDeviceFirmwaresVersionUpdates() throws Exception {

        List<DeviceFirmware> firmwares = new ArrayList<>();
        firmwares.add(deviceFirmware);
        firmwares.add(deviceFirmware);

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, deviceModel.getName()))
                .thenReturn(ServiceResponseBuilder.<DeviceModel> ok()
                        .withResult(deviceModel)
                        .build());

        when(deviceFirmwareService.findByVersion(tenant, application, deviceModel, deviceFirmware.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>  ok()
                        .withResult(deviceFirmware)
                        .build());

        when(deviceFirmwareUpdateService.findByDeviceFirmware(tenant, application, deviceFirmware))
                .thenReturn(ServiceResponseBuilder.<List<DeviceFwUpdate>> error().build());



        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/", deviceModel, deviceFirmware.getVersion()))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Firmware already registered")))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.result[1].uploadTimestamp", notNullValue()));
    }

}
