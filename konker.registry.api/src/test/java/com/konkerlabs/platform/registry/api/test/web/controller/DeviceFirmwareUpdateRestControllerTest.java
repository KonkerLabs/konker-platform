package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.DeviceFirmwareUpdateInputVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceFirmwareUpdateRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.repositories.DeviceFirmwareUpdateRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private DeviceFirmwareUpdateRepository deviceFirmwareUpdateRepository;

    private DeviceModel deviceModel;

    private DeviceFwUpdate deviceFwUpdatePendingUpdate;

    private DeviceFwUpdate deviceFwUpdatePendingRequest;

    private DeviceFwUpdate deviceFwUpdated;

    private DeviceFirmware deviceFirmwareOld;

    private DeviceFirmware deviceFirmwareNew;

    private Device device;

    private final String BASEPATH = "firmwares";
    private final String BASEPATH_UPDATE = "firmwareupdates";

    @Before
    public void setUp() {

        deviceModel = DeviceModel.builder()
                .tenant(tenant)
                .application(application)
        		.guid(UUID.randomUUID().toString())
        		.name("air conditioner")
        		.build();

        deviceFirmwareOld = DeviceFirmware.builder()
                .version("0.4.3")
                .deviceModel(deviceModel)
                .uploadDate(Instant.now())
                .build();

        deviceFirmwareNew = DeviceFirmware.builder()
                .version("0.4.4")
                .deviceModel(deviceModel)
                .uploadDate(Instant.now())
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


        deviceFwUpdatePendingUpdate = DeviceFwUpdate.builder()
                .version("0.4.3")
                .status(FirmwareUpdateStatus.PENDING)
                .application(application)
                .deviceFirmware(deviceFirmwareOld)
                .deviceGuid(UUID.randomUUID().toString())
                .build();

        deviceFwUpdatePendingRequest= DeviceFwUpdate.builder()
                .version("0.4.4")
                .status(FirmwareUpdateStatus.PENDING)
                .application(application)
                .deviceFirmware(deviceFirmwareNew)
                .deviceGuid(deviceFwUpdatePendingUpdate.getGuid())
                .build();


        deviceFwUpdated = DeviceFwUpdate.builder()
                .version("0.4.4")
                .status(FirmwareUpdateStatus.UPDATED)
                .application(application)
                .deviceFirmware(deviceFirmwareNew)
                .deviceGuid(UUID.randomUUID().toString())
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


        DeviceFwUpdate fwUpdate = DeviceFwUpdate
                .builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .deviceGuid(device.getGuid())
                .deviceFirmware(deviceFirmwareNew)
                .version(deviceFirmwareNew.getVersion())
                .status(FirmwareUpdateStatus.PENDING)
                .lastChange(Instant.now())
                .build();



        when(deviceRegisterService.getByDeviceGuid(tenant, application, deviceFwUpdatePendingUpdate.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFirmwareNew.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok().withResult(deviceFirmwareNew).build());


        when(deviceFirmwareUpdateService.save(tenant, application, device, deviceFirmwareNew))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>ok().withResult(fwUpdate).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH_UPDATE))
                .content(getJson(new DeviceFirmwareUpdateInputVO().apply(deviceFwUpdatePendingRequest)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.deviceGuid", is(device.getGuid())))
                .andExpect(jsonPath("$.result.version", is(fwUpdate.getVersion())))
                .andExpect(jsonPath("$.result.status", is(FirmwareUpdateStatus.PENDING.toString())));
    }

    @Test
    public void shouldTryCreateDeviceFirmwareUpdateWithBadRequest() throws Exception {


        when(deviceRegisterService.getByDeviceGuid(tenant, application, device.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFirmwareNew.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok().withResult(deviceFirmwareNew).build());

        when(deviceFirmwareUpdateService.save(tenant, application, device, deviceFirmwareNew))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>error()
                        .withMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_ALREADY_EXISTS.getCode())
                        .build());


        getMockMvc().perform(MockMvcRequestBuilders
                .post(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH_UPDATE))
                .content(getJson(new DeviceFirmwareUpdateInputVO().apply(deviceFwUpdatePendingRequest)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTrySuspendDeviceFirmwareUpdate() throws Exception {

        DeviceFwUpdate fwUpdateSuspended = DeviceFwUpdate
                .builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .deviceGuid(device.getGuid())
                .deviceFirmware(deviceFirmwareNew)
                .version(deviceFirmwareNew.getVersion())
                .status(FirmwareUpdateStatus.SUSPENDED)
                .lastChange(Instant.now())
                .build();

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFwUpdatePendingUpdate.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok().withResult(deviceFirmwareNew).build());


        when(deviceFirmwareUpdateService.setDeviceAsSuspended(tenant, application, device))
                .thenReturn(ServiceResponseBuilder.<DeviceFwUpdate>ok().withResult(fwUpdateSuspended).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH_UPDATE,"suspend"))
                .param("deviceGuid", device.getGuid())
                .param("version", deviceFwUpdatePendingUpdate.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result.version", is(fwUpdateSuspended.getVersion())))
                .andExpect(jsonPath("$.result.status", is(fwUpdateSuspended.getStatus().toString())));

    }

    @Test
    public void shouldTrySuspendDeviceFirmwareUpdateWithBadRequest() throws Exception {

        DeviceFwUpdate fwUpdateSuspended = DeviceFwUpdate
                .builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .deviceGuid(device.getGuid())
                .deviceFirmware(deviceFirmwareNew)
                .version(deviceFirmwareNew.getVersion())
                .status(FirmwareUpdateStatus.SUSPENDED)
                .lastChange(Instant.now())
                .build();

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFwUpdatePendingUpdate.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>error()
                        .withMessage(DeviceFirmwareService.Validations.FIRMWARE_NOT_FOUND.getCode())
                        .build());


        getMockMvc().perform(MockMvcRequestBuilders
                .put(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH_UPDATE,"suspend"))
                .param("deviceGuid", device.getGuid())
                .param("version",  deviceFwUpdatePendingUpdate.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Firmware not found")))
                .andExpect(jsonPath("$.result").doesNotExist());




    }

    @Test
    public void shouldListDeviceFirmwaresVersionUpdates() throws Exception {

        List<DeviceFirmware> firmwares = new ArrayList<>();
        firmwares.add(deviceFirmwareNew);
        firmwares.add(deviceFirmwareNew);


        List<DeviceFwUpdate>  firmwaresUpdates = new ArrayList<>();
        firmwaresUpdates.add(deviceFwUpdatePendingRequest);
        firmwaresUpdates.add(deviceFwUpdatePendingRequest);

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFwUpdatePendingUpdate.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok().withResult(deviceFirmwareNew).build());


        when(deviceFirmwareUpdateService.findByDeviceFirmware(tenant, application, deviceFirmwareNew))
                .thenReturn(ServiceResponseBuilder.<List<DeviceFwUpdate>> ok()
                        .withResult(firmwaresUpdates)
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH_UPDATE))
                .param("deviceGuid", device.getGuid())
                .param("version", deviceFwUpdatePendingUpdate.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].version", is(deviceFirmwareNew.getVersion())));
    }

    @Test
    public void shouldReturnInternalErrorWhenListDeviceFirmwaresVersionUpdates() throws Exception {

        List<DeviceFirmware> firmwares = new ArrayList<>();
        firmwares.add(deviceFirmwareNew);
        firmwares.add(deviceFirmwareNew);


        List<DeviceFwUpdate>  firmwaresUpdates = new ArrayList<>();
        firmwaresUpdates.add(deviceFwUpdatePendingRequest);
        firmwaresUpdates.add(deviceFwUpdatePendingRequest);

        when(deviceRegisterService.getByDeviceGuid(tenant, application, device.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device>ok().withResult(device).build());

        when(deviceFirmwareService.findByVersion(tenant, application, device.getDeviceModel(), deviceFwUpdatePendingUpdate.getVersion()))
                .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok().withResult(deviceFirmwareNew).build());


        when(deviceFirmwareUpdateService.findByDeviceFirmware(tenant, application, deviceFirmwareNew))
                .thenReturn(ServiceResponseBuilder.<List<DeviceFwUpdate>> error()
                        .withMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_NOT_FOUND.getCode()).build());


        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/", application.getName(), BASEPATH_UPDATE))
                .param("deviceGuid", device.getGuid())
                .param("version", deviceFwUpdatePendingUpdate.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Firmware update process not found")))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

}
