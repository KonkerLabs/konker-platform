package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceCustomData;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceCustomDataRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService;
import com.konkerlabs.platform.registry.business.services.api.DeviceCustomDataService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class DeviceCustomDataServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private DeviceCustomDataService deviceCustomDataService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceCustomDataRepository deviceCustomDataRepository;

    private Device deviceA;
    private Device deviceB;

    private Tenant tenant;
    private Application application;

    private DeviceCustomData customDataB;

    private String jsonA = "{ 'barCode' : '00000' }";
    private String jsonB = "{'a': 2}";

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
                        .guid("0b39af34-d504-41d2-affe-be307285db41")
                        .build();
        deviceB = deviceRepository.save(deviceB);

        customDataB = DeviceCustomData.builder()
                               .tenant(tenant)
                               .application(application)
                               .device(deviceB)
                               .json(jsonB)
                               .build();
        customDataB = deviceCustomDataRepository.save(customDataB);

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullTenant() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(null, application, deviceA, jsonA);
        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingWithNullApplication() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(tenant, null, deviceA, jsonA);
        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingNullJson() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(tenant, application, deviceA, null);
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldReturnErrorIfSavingInvalidJson() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(tenant, application, deviceA, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldSaveDeviceCustomData() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(tenant, application, deviceA, jsonA);
        assertThat(serviceResponse.isOk(), is(true));

    }

    @Test
    public void shouldReturnErrorIfSaveExistingDeviceCustomData() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.save(tenant, application, deviceA, jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        serviceResponse = deviceCustomDataService.save(tenant, application, deviceA, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_ALREADY_REGISTERED.getCode()));

    }

    @Test
    public void shouldReturnErrorIfUpdateDeviceCustomDataWithInvalidJson() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.update(tenant, application, deviceB, "a=4");
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_INVALID_JSON.getCode()));

    }

    @Test
    public void shouldUpdateDeviceCustomData() throws Exception {

        assertThat(deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(tenant.getId(), application.getName(), deviceB.getId()).getJson(), is(jsonB));

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.update(tenant, application, deviceB, jsonA);
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(tenant.getId(), application.getName(), deviceB.getId()).getJson(), is(jsonA));

    }

    @Test
    public void shouldTryUpdateNonExistingDeviceCustomData() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.update(tenant, application, deviceA, jsonA);
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldRemoveDeviceCustomData() throws Exception {

        assertThat(deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(tenant.getId(), application.getName(), deviceB.getId()), notNullValue());

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.remove(tenant, application, deviceB);
        assertThat(serviceResponse.isOk(), is(true));

        assertThat(deviceCustomDataRepository.findByTenantIdApplicationNameAndDeviceId(tenant.getId(), application.getName(), deviceB.getId()), nullValue());

    }

    @Test
    public void shouldTryToRemoveNonExistingDeviceCustomData() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.remove(tenant, application, deviceA);
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void shouldFindByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.getByTenantApplicationAndDevice(tenant, application, deviceB);
        assertThat(serviceResponse.isOk(), is(true));
        assertThat(serviceResponse.getResult().getJson(), is(jsonB));

    }

    @Test
    public void shouldFindNonExistingByTenantAndApplicationAndModelAndLocation() throws Exception {

        ServiceResponse<DeviceCustomData> serviceResponse = deviceCustomDataService.getByTenantApplicationAndDevice(tenant, application, deviceA);
        assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_CUSTOM_DATA_DOES_NOT_EXIST.getCode()));

    }

}