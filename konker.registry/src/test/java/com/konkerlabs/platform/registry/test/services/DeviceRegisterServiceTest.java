package com.konkerlabs.platform.registry.test.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.IntegrationTestBase;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DeviceRegisterServiceTest extends IntegrationTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    private Device device;

    @Before
    public void setUp() throws Exception {
        device = spy(Device.builder()
            .deviceId("94c32b36cd2b43f1")
            .name("Device name")
            .description("Description")
            .build());
    }

    @Test
    public void shouldRaiseAnExceptionIfRecordIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Record cannot be null");

        deviceRegisterService.register(null);
    }

    @Test
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        List<String> errorMessages = Arrays.asList(new String[]{"Some error"});
        when(device.applyValidations()).thenReturn(errorMessages);

        ServiceResponse response = deviceRegisterService.register(device);

        assertThat(response,notNullValue());
        assertThat(response.getStatus(),equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(),equalTo(errorMessages));
    }

    @Test
    public void shouldReturnResponseMessagesIfRecordsTenantDoesNotExist() throws Exception {
        Tenant fakeTenant = Tenant.builder()
            .id("fake_id").build();

        device.setTenant(fakeTenant);

        List<String> errorMessages = Arrays.asList(new String[]{"Tenant does not exist"});

        ServiceResponse response = deviceRegisterService.register(device);

        assertThat(response,notNullValue());
        assertThat(response.getStatus(),equalTo(ServiceResponse.Status.ERROR));
        assertThat(response.getResponseMessages(),equalTo(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldPersistIfDeviceIsValid() throws Exception {
        Tenant tenant = tenantRepository.findOne("71fb0d48-674b-4f64-a3e5-0256ff3a63af");
        device.setTenant(tenant);

        ServiceResponse response = deviceRegisterService.register(device);

        assertThat(deviceRepository.findByDeviceId(device.getDeviceId()),notNullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidResponseIfRegisterWasSuccessful() throws Exception {
        Tenant tenant = tenantRepository.findOne("71fb0d48-674b-4f64-a3e5-0256ff3a63af");
        device.setTenant(tenant);

        ServiceResponse response = deviceRegisterService.register(device);

        assertThat(response,notNullValue());
        assertThat(response.getStatus(),equalTo(ServiceResponse.Status.OK));
        assertThat(response.getResponseMessages(),nullValue());
    }
}