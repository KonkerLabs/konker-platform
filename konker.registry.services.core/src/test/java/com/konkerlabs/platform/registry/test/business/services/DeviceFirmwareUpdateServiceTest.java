package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService.Validations;
import com.konkerlabs.platform.registry.business.services.api.DeviceFirmwareUpdateService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    private Device deviceA;
    private Device deviceB;
    private Tenant tenant;
    private Application application;

    private DeviceFwUpdate deviceFwUpdateB;

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


        deviceB = Device.builder()
                .tenant(tenant)
                .application(application)
                .guid("0b39af34-d504-41d2-affe-be307285db42")
                .build();
        deviceA = deviceRepository.save(deviceA);

        deviceFwUpdateB = DeviceFwUpdate.builder()
                               .tenant(tenant)
                               .application(application)
                               .deviceGuid("0b39af34-d504-41d2-affe-be307285db41")
                               .status(FirmwareUpdateStatus.PENDING)
                               .build();
        deviceFwUpdateB = deviceFirmwareUpdateRepository.save(deviceFwUpdateB);

    }


    @Test
    public void shouldFindByTenantAndApplication() throws Exception {


        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceA.getTenant(),
                deviceA.getApplication(),
                deviceA);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getId(), is("????"));

    }

    @Test
    public void shouldFindNonExistingByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                deviceB.getTenant(),
                deviceB.getApplication(),
                deviceB);
        assertThat(serviceResponse, is("{ }"));

    }

}