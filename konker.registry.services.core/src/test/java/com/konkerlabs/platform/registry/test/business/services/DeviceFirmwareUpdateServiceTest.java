package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService.Validations;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class DeviceFirmwareUpdateServiceTest extends BusinessLayerTestSupport {


    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeviceFirmwareUpdateRepository deviceFirmwareUpdateRepository;

    @Autowired
    private DeviceFirmwareUpdateService deviceFirmwareUpdateService;

    @Autowired
    private DeviceRepository deviceRepository;

    private static final String VERSION_1 = "10.30";
    private static final String VERSION_2 = "10.40";

    private Device deviceA;
    private Device deviceB;
    private Tenant tenant;
    private Application application;

    private DeviceFwUpdate deviceFwUpdateA;
    private DeviceFwUpdate deviceFwUpdateB1;
    private DeviceFwUpdate deviceFwUpdateB2;

    @Before
    public void setUp() {
    	tenant = tenantRepository.findByDomainName("konker");
    	application = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        deviceA = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid("0b39af34-d504-41d2-affe-be307285db41")
                .build();
        deviceA = deviceRepository.save(deviceA);

        deviceFwUpdateA = DeviceFwUpdate.builder()
                .tenant(tenant)
                .application(application)
                .deviceGuid(deviceA.getGuid())
                .status(FirmwareUpdateStatus.UPDATED)
                .version(VERSION_1)
                .build();
        deviceFwUpdateA = deviceFirmwareUpdateRepository.save(deviceFwUpdateA);

        deviceB = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid("0b39af34-d504-41d2-affe-be307285db42")
                .build();
        deviceB = deviceRepository.save(deviceB);

        deviceFwUpdateB1 = DeviceFwUpdate.builder()
                               .tenant(tenant)
                               .application(application)
                               .deviceGuid(deviceB.getGuid())
                               .status(FirmwareUpdateStatus.UPDATED)
                               .version(VERSION_1)
                               .build();
        deviceFwUpdateB1 = deviceFirmwareUpdateRepository.save(deviceFwUpdateB1);

        deviceFwUpdateB2 = DeviceFwUpdate.builder()
                .tenant(tenant)
                .application(application)
                .deviceGuid(deviceB.getGuid())
                .status(FirmwareUpdateStatus.PENDING)
                .version(VERSION_2)
                .build();
        deviceFwUpdateB2 = deviceFirmwareUpdateRepository.save(deviceFwUpdateB2);

    }

    @Test
    public void shouldFindByTenantAndApplication() throws Exception {

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceB.getTenant(),
                deviceB.getApplication(),
                deviceB);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getVersion(), is(VERSION_2));
        assertThat(serviceResponse.getResult().getStatus(), is(FirmwareUpdateStatus.PENDING));

    }

    @Test
    public void shouldNotFindByTenantAndApplication() throws Exception {

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceA.getTenant(),
                deviceA.getApplication(),
                deviceA);
        assertThat(serviceResponse.isOk(), is(false));

    }

    @Test
    public void shouldSetStatusAsUpdated() throws Exception {

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceB.getTenant(),
                deviceB.getApplication(),
                deviceB);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getVersion(), is(VERSION_2));
        assertThat(serviceResponse.getResult().getStatus(), is(FirmwareUpdateStatus.PENDING));

        deviceFirmwareUpdateService.setDeviceAsUpdated(tenant, application, deviceB);

        serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceB.getTenant(),
                deviceB.getApplication(),
                deviceB);
        assertThat(serviceResponse.isOk(), is(false));

    }

    @Test
    public void shouldNotSaveExistingFirmwareUpdate() throws Exception {

        DeviceFirmware deviceFirmware = DeviceFirmware
                .builder()
                .version(VERSION_1)
                .build();

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.save(
                deviceB.getTenant(),
                deviceB.getApplication(),
                deviceB,
                deviceFirmware);

        assertThat(serviceResponse.isOk(), is(false));
        Assert.assertTrue(serviceResponse.getResponseMessages().containsKey(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_ALREADY_EXISTS.getCode()));

    }


}