package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.DeviceFirmwareRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeviceFirmwareRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class DeviceFirmwareRestControllerTest extends WebLayerTestContext {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private Application application;

    @Autowired
    private DeviceFirmwareService deviceConfigSetupService;

    @Autowired
    private DeviceModelService deviceModelService;

    private DeviceModel deviceModel;

    private DeviceFirmware deviceFirmware;

    private String BASEPATH = "firmwares";

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
    public void shouldListDeviceFirmwares() throws Exception {

        List<DeviceFirmware> firmwares = new ArrayList<>();
        firmwares.add(deviceFirmware);
        firmwares.add(deviceFirmware);

        when(deviceConfigSetupService.listByDeviceModel(tenant, application, deviceModel))
            .thenReturn(ServiceResponseBuilder.<List<DeviceFirmware>>ok()
                    .withResult(firmwares)
                    .build());

        getMockMvc()
                .perform(MockMvcRequestBuilders
        		.get(MessageFormat.format("/{0}/{1}/{2}/", application.getName(), BASEPATH, deviceModel.getName()))
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
                .andExpect(jsonPath("$.result[1].uploadTimestamp", notNullValue()))
                ;

    }

    @Test
    public void shouldReturnInternalErrorWhenListDeviceFirmwares() throws Exception {

        when(deviceConfigSetupService.listByDeviceModel(tenant, application, deviceModel))
            .thenReturn(ServiceResponseBuilder.<List<DeviceFirmware>>error()
                    .build());

        getMockMvc().perform(MockMvcRequestBuilders
                .get(MessageFormat.format("/{0}/{1}/{2}/", application.getName(), BASEPATH, deviceModel.getName()))
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
    public void shouldCreateDeviceFirmwareMD5() throws Exception {

        when(deviceConfigSetupService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(DeviceFirmware.class)))
            .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok()
                    .withResult(deviceFirmware).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel.getName()))
                .file(new MockMultipartFile("firmware", "00000".getBytes()))
                .file(new MockMultipartFile("checksum", "dcddb75469b4b4875094e14561e573d8   file.bin".getBytes()))
                .param("version", deviceFirmware.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result.uploadTimestamp", notNullValue()))
                ;

    }

    @Test
    public void shouldCreateDeviceFirmwareSHA1() throws Exception {

        when(deviceConfigSetupService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(DeviceFirmware.class)))
            .thenReturn(ServiceResponseBuilder.<DeviceFirmware>ok()
                    .withResult(deviceFirmware).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel.getName()))
                .file(new MockMultipartFile("firmware", "00000".getBytes()))
                .file(new MockMultipartFile("checksum", "6934105ad50010b814c933314b1da6841431bc8b".getBytes()))
                .param("version", deviceFirmware.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").doesNotExist())
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.version", is(deviceFirmware.getVersion())))
                .andExpect(jsonPath("$.result.uploadTimestamp", notNullValue()))
                ;

    }

    @Test
    public void shouldTryCreateDeviceFirmwareWithInvalidChecksum() throws Exception {

        when(deviceConfigSetupService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(DeviceFirmware.class)))
            .thenReturn(ServiceResponseBuilder.<DeviceFirmware>error()
                .withMessage(DeviceFirmwareService.Validations.FIRMWARE_ALREADY_REGISTERED.getCode())
                .withResult(deviceFirmware).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel.getName()))
                .file(new MockMultipartFile("firmware", "00000".getBytes()))
                .file(new MockMultipartFile("checksum", "00000".getBytes()))
                .param("version", deviceFirmware.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Invalid checksum (MD5 or SHA1)")))
                .andExpect(jsonPath("$.result").doesNotExist())
                ;

    }

    @Test
    public void shouldTryCreateDeviceFirmwareWithBadRequest() throws Exception {

        when(deviceConfigSetupService.save(org.mockito.Matchers.any(Tenant.class), org.mockito.Matchers.any(Application.class), org.mockito.Matchers.any(DeviceFirmware.class)))
            .thenReturn(ServiceResponseBuilder.<DeviceFirmware>error()
                .withMessage(DeviceFirmwareService.Validations.FIRMWARE_ALREADY_REGISTERED.getCode())
                .withResult(deviceFirmware).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, deviceModel.getName()))
                .file(new MockMultipartFile("firmware", "00000".getBytes()))
                .file(new MockMultipartFile("checksum", "dcddb75469b4b4875094e14561e573d8".getBytes()))
                .param("version", deviceFirmware.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Firmware already registered")))
                .andExpect(jsonPath("$.result").doesNotExist())
                ;

    }

    @Test
    public void shouldTryCreateDeviceFirmwareWithBadDeviceModel() throws Exception {

        when(deviceModelService.getByTenantApplicationAndName(tenant, application, "invalid model"))
            .thenReturn(ServiceResponseBuilder.<DeviceModel> error()
                .withMessage(DeviceModelService.Validations.DEVICE_MODEL_NOT_FOUND.getCode())
                .withResult(deviceModel).build());

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload(MessageFormat.format("/{0}/{1}/{2}", application.getName(), BASEPATH, "invalid model"))
                .file(new MockMultipartFile("firmware", "00000".getBytes()))
                .file(new MockMultipartFile("checksum", "dcddb75469b4b4875094e14561e573d8".getBytes()))
                .param("version", deviceFirmware.getVersion())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages[0]", is("Device model not found")))
                .andExpect(jsonPath("$.result").doesNotExist())
                ;

    }

}
