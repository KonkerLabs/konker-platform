package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService.Messages;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService.Validations;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
		PubServerConfig.class,
        EventStorageConfig.class})
public class DeviceModelServiceTest extends BusinessLayerTestSupport {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private TenantRepository tenantRepository;

    private DeviceModel deviceModel;
    private DeviceModel tempDeviceModel;
    private DeviceModel newDeviceModel;
    private Application application;
    private Application otherApplication;
    private Tenant currentTenant;
    private Tenant otherTenant;

    @Before
    public void setUp() {
    	currentTenant = tenantRepository.findByName("Konker");
    	otherTenant = Tenant.builder()
    			.id("71fb0d48-674b-4f64-a3e5-0256ff3a0000")
    			.name("MyCompany")
    			.domainName("MyCompany")
    			.build();

    	application = Application.builder()
    					.name("smartffkonker")
    					.friendlyName("Konker Smart Frig")
    					.description("Konker Smart Frig - take pic, tells temperatue")
    					.tenant(currentTenant)
                        .qualifier("konker")
                .registrationDate(Instant.ofEpochMilli(1453320973747L))
                        .build();

    	otherApplication = Application.builder()
				.name("smartffkonkerother")
				.friendlyName("Konker Smart Frig")
				.description("Konker Smart Frig - take pic, tells temperatue")
				.tenant(currentTenant)
                .qualifier("konker")
                .registrationDate(Instant.ofEpochMilli(1453320973747L))
				.build();

    	deviceModel = DeviceModel.builder()
    					.guid("7d51c242-81db-11e6-a8c2-0746f908e887")
		    			.name("SmartFF")
		    			.description("Smart ff model")
    					.application(application)
    					.defaultModel(true)
    					.tenant(currentTenant)
    					.build();

    	tempDeviceModel = DeviceModel.builder()
				.guid("7d51c242-81db-11e6-a8c2-0746f908e997")
    			.name("sensor")
    			.description("temperature sensor")
				.application(application)
				.defaultModel(false)
				.tenant(currentTenant)
				.build();

    	newDeviceModel = DeviceModel.builder()
    			.name("SensorAC")
    			.description("Sensor AC model")
				.application(application)
				.defaultModel(false)
				.tenant(currentTenant)
				.build();

    }

    @Test
    public void shouldReturnErrorIfSavingDevModelTenantIsNull() throws Exception {
        ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(null, application, deviceModel);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingDevModelTenantNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(otherTenant, application, deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingDevModelAppIsNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, null, deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingDevModelAppNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, otherApplication, deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingDevModelIsNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, application, null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingDevModelNameNullOrEmpty() throws Exception {
    	deviceModel.setName(null);
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, application, deviceModel);
    	assertThat(serviceResponse, hasErrorMessage(DeviceModel.Validations.NAME_NULL_EMPTY.getCode()));

    	deviceModel.setName("");
    	serviceResponse = deviceModelService.register(currentTenant, application, deviceModel);
    	assertThat(serviceResponse, hasErrorMessage(DeviceModel.Validations.NAME_NULL_EMPTY.getCode()));
    }


    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorIfSavingDevModelExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, application, deviceModel);
    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_ALREADY_REGISTERED.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldSavingNewDevModelDefault() throws Exception {
    	newDeviceModel.setDefaultModel(true);
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.register(currentTenant, application, newDeviceModel);
    	assertThat(serviceResponse, isResponseOk());

    	ServiceResponse<DeviceModel> serviceResponseOldDefault = deviceModelService
    			.getByTenantApplicationAndName(currentTenant, application, deviceModel.getName());

    	deviceModel.setDefaultModel(false);
    	assertThat(serviceResponseOldDefault, isResponseOk());
    	assertThat(serviceResponseOldDefault.getResult(), equalTo(deviceModel));
    	assertThat(serviceResponseOldDefault.getResult().isDefaultModel(), equalTo(false));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldSaveTheFirstDevModel() throws Exception {
        ServiceResponse<DeviceModel> response = deviceModelService.register(currentTenant, application, newDeviceModel);

        assertThat(response, isResponseOk());
        assertThat(response.getResult().isDefaultModel(), equalTo(true));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldSaveDevModel() throws Exception {
        ServiceResponse<DeviceModel> response = deviceModelService.register(currentTenant, application, newDeviceModel);

        assertThat(response, isResponseOk());
    }

    @Test
    public void shouldReturnErrorIfUpdatingDevModelTenantIsNull() throws Exception {
        ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(null, application, "smartffkonker", deviceModel);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingDevModelTenantNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(otherTenant, application, "smartffkonker", deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingDevModelAppIsNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, null, "smartffkonker", deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingDevModelAppNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, otherApplication, "smartffkonker", deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingDevModelIsNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, application, "smartffkonker", null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingDevModelNameNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, application, null, deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorIfUpdatingDevModelNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, application, newDeviceModel.getName(), deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorIfUpdatingDevModelDefault() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(currentTenant, application, deviceModel.getName(), deviceModel);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NOT_UPDATED_IS_DEFAULT.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldUpdateApp() throws Exception {
    	tempDeviceModel.setDescription("Updating description");
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.update(application.getTenant(), application, tempDeviceModel.getName(), tempDeviceModel);

        assertThat(serviceResponse, isResponseOk());
        assertThat(serviceResponse.getResult().getDescription(), equalTo(tempDeviceModel.getDescription()));
    }

    @Test
    public void shouldReturnErrorIfRemovingDevModelTenantIsNull() throws Exception {
        ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(null, application, deviceModel.getName());

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingDevModelAppIsNull() throws Exception {
        ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, null, deviceModel.getName());

        assertThat(serviceResponse, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingDevModelNameNull() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, application, null);

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorIfRemovingDevModelNotExists() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, application, otherApplication.getName());

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json", "/fixtures/devices.json"})
    public void shouldReturnErrorIfRemovingDevModelHasDevice() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, application, tempDeviceModel.getName());

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_HAS_DEVICE.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorIfRemovingDevModelDefault() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, application, deviceModel.getName());

    	assertThat(serviceResponse, hasErrorMessage(Validations.DEVICE_MODEL_NOT_REMOVED_IS_DEFAULT.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldRemoveDevModel() throws Exception {
    	ServiceResponse<DeviceModel> serviceResponse = deviceModelService.remove(currentTenant, application, tempDeviceModel.getName());

    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.DEVICE_MODEL_REMOVED_SUCCESSFULLY.getCode(), null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnAllApp() throws Exception {
    	ServiceResponse<List<DeviceModel>> response = deviceModelService.findAll(currentTenant, application);

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), notNullValue());
    	assertThat(response.getResult(), hasSize(2));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorGetDevModelByNameTenantNull() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(null, application, deviceModel.getName());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorGetDevModelByNameAppNull() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(currentTenant, null, deviceModel.getName());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetDevModelByNameNull() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(currentTenant, application, null);

    	assertThat(response, hasErrorMessage(Validations.DEVICE_MODEL_NAME_IS_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetDevModelByNameTenantNotExists() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(otherTenant, application, deviceModel.getName());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetDevModelByNameAppNotExists() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(currentTenant, otherApplication, deviceModel.getName());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetDevModelByNameDevModelNotExists() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(currentTenant, application, newDeviceModel.getName());

    	assertThat(response, hasErrorMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldGetDevModelByName() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.getByTenantApplicationAndName(currentTenant, application, deviceModel.getName());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(deviceModel));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorGetDevicesByDeviceModelTenantNull() throws Exception {
    	ServiceResponse<List<Device>> response = deviceModelService.listDevicesByDeviceModelName(null, application, deviceModel.getName());

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorGetDevicesByDeviceModelAppNUll() throws Exception {
    	ServiceResponse<List<Device>> response = deviceModelService.listDevicesByDeviceModelName(currentTenant, null, deviceModel.getName());

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorGetDevicesByDeviceModelNotExist() throws Exception {
    	ServiceResponse<List<Device>> response = deviceModelService.listDevicesByDeviceModelName(currentTenant, application, deviceModel.getName());

    	assertThat(response, hasErrorMessage(Validations.DEVICE_MODEL_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json", "/fixtures/devices.json"})
    public void shouldGetDevicesByDeviceModel() throws Exception {
    	ServiceResponse<List<Device>> response = deviceModelService.listDevicesByDeviceModelName(currentTenant, application, deviceModel.getName());

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), hasSize(1));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorGetDefaultModelByTenantNull() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.findDefault(null, application);

    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldReturnErrorGetDefaultModelByAppNUll() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.findDefault(currentTenant, null);

    	assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/device-model.json"})
    public void shouldGetDefaultModel() throws Exception {
    	ServiceResponse<DeviceModel> response = deviceModelService.findDefault(currentTenant, application);

    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(deviceModel));
    }

}