package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.business.model.validation.CommonValidations.TENANT_NULL;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.rules.ExpectedException.none;

import java.time.Instant;
import java.util.List;

import org.bson.types.Binary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceFirmwareRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class})
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/applications.json" })
public class DeviceFirmwareServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private DeviceFirmwareService subject;

    @Autowired
    private DeviceFirmwareRepository deviceFirmwareRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Tenant tenant;

    private Application application;
    private Application otherApplication;

    private DeviceModel deviceModel1;

    private byte firmwareBinary[] = "{ 'code' : 'tschuss' }".getBytes();

    @Before
    public void setUp() {
        tenant = tenantRepository.findByName("Konker");

        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        otherApplication = applicationRepository.findByTenantAndName(tenant.getId(), "smartffkonker");

        deviceModel1 = DeviceModel.builder()
                                 .tenant(tenant)
                                 .application(application)
                                 .name("air conditioner")
                                 .guid("be68c474-b961-4974-829d-daeed9e4142b")
                                 .build();
        deviceModelRepository.save(deviceModel1);

        DeviceFirmware deviceFirmware = DeviceFirmware.builder()
                                                .tenant(tenant)
                                                .application(application)
                                                .deviceModel(deviceModel1)
                                                .firmware(new Binary(firmwareBinary))
                                                .uploadDate(Instant.now())
                                                .version("0.1.0")
                                                .build();
        deviceFirmwareRepository.save(deviceFirmware);

    }

    // ============================== listByDeviceModel ==============================//

    @Test
    public void shouldListByDeviceModel() {

        ServiceResponse<List<DeviceFirmware>> response = subject.listByDeviceModel(tenant, application, deviceModel1);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().size(), is(1));
        assertThat(response.getResult().get(0).getVersion(), is("0.1.0"));
        assertThat(response.getResult().get(0).getFirmware().getData(), is(firmwareBinary));

    }

    @Test
    public void shouldTryListByDeviceModelWithNullTenant() {

        ServiceResponse<List<DeviceFirmware>> response = subject.listByDeviceModel(null, application, deviceModel1);
        assertThat(response, hasErrorMessage(TENANT_NULL.getCode()));

    }

    // ============================== save ==============================//

    @Test
    public void shouldSave() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                                                   .tenant(tenant)
                                                   .application(application)
                                                   .deviceModel(deviceModel1)
                                                   .version("0.2.0")
                                                   .firmware(new Binary(firmwareBinary))
                                                   .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, application, newFirmware);
        assertThat(response, isResponseOk());
        assertThat(response.getResult(), notNullValue());

        DeviceFirmware firmwareFromDB = deviceFirmwareRepository.findUnique(tenant.getId(), application.getName(), deviceModel1.getId(), "0.2.0");
        assertThat(firmwareFromDB.getVersion(), is("0.2.0"));
        assertThat(firmwareFromDB.getFirmware().getData(), is(firmwareBinary));
        assertThat(firmwareFromDB.getUploadDate(), notNullValue());

    }

    @Test
    public void shouldTrySaveWithNullApplication() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel1)
                .version("0.2.0")
                .firmware(new Binary(firmwareBinary))
                .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, null, newFirmware);
        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldTrySaveWithNullBinary() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel1)
                .version("0.2.0")
                .firmware(null)
                .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, application, newFirmware);
        assertThat(response, hasErrorMessage(DeviceFirmware.Validations.FIRMWARE_NULL.getCode()));

    }

    @Test
    public void shouldTrySaveWithExistingBinary() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel1)
                .version("0.1.0")
                .firmware(new Binary(firmwareBinary))
                .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, application, newFirmware);
        assertThat(response, hasErrorMessage(DeviceFirmwareService.Validations.FIRMWARE_ALREADY_REGISTERED.getCode()));

    }

    @Test
    public void shouldTrySaveWithInvalidVersion() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(application)
                .deviceModel(deviceModel1)
                .version("0.1.0cccc")
                .firmware(new Binary(firmwareBinary))
                .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, application, newFirmware);
        assertThat(response, hasErrorMessage(DeviceFirmware.Validations.INVALID_VERSION.getCode()));

    }

    @Test
    public void shouldTryListByDeviceModelWithOtherApplication() {

        DeviceFirmware newFirmware = DeviceFirmware.builder()
                .tenant(tenant)
                .application(otherApplication)
                .deviceModel(deviceModel1)
                .version("0.2.0")
                .firmware(new Binary(firmwareBinary))
                .build();

        ServiceResponse<DeviceFirmware> response = subject.save(tenant, otherApplication, newFirmware);
        assertThat(response, hasErrorMessage(DeviceModelService.Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()));

    }


}
