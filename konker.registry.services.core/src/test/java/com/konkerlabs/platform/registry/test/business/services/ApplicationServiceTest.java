package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService.Messages;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService.Validations;
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
public class ApplicationServiceTest extends BusinessLayerTestSupport {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TenantRepository tenantRepository;

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
    }

    @Test
    public void shouldReturnErrorIfSavingAppTenantIsNull() throws Exception {
        ServiceResponse<Application> serviceResponse = applicationService.register(null, application);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppTenantNotExists() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.register(otherTenant, application);
    	
    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppIsNull() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, null);
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppNameWithespace() throws Exception {
    	application.setName("smartff konker");
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, application);
    	
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.NAME_INVALID.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppNameSpecialChar() throws Exception {
    	application.setName("smartff#$%^ konker");
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, application);
    	
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.NAME_INVALID.getCode()));
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppNameNullOrEmpty() throws Exception {
    	application.setName(null);
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.NAME_NULL_EMPTY.getCode()));
    	
    	application.setName("");
    	serviceResponse = applicationService.register(currentTenant, application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.NAME_NULL_EMPTY.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfSavingAppFriendlyNameNullOrEmpty() throws Exception {
    	application.setFriendlyName(null);
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.FRIENDLY_NAME_NULL_EMPTY.getCode()));
    	
    	application.setFriendlyName("");
    	serviceResponse = applicationService.register(currentTenant, application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.FRIENDLY_NAME_NULL_EMPTY.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfSavingAppExists() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.register(currentTenant, application);
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_ALREADY_REGISTERED.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldSaveApp() throws Exception {
        ServiceResponse<Application> response = applicationService.register(currentTenant, otherApplication);

        assertThat(response, isResponseOk());
    }
    
    @Test
    public void shouldReturnErrorIfUpdatingAppTenantIsNull() throws Exception {
        ServiceResponse<Application> serviceResponse = applicationService.update(null, "smartffkonker", application);

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingAppTenantNotExists() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.update(otherTenant, "smartffkonker", application);
    	
    	assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingAppIsNull() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.update(currentTenant, "smartffkonker", null);
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfUpdatingAppNameNull() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.update(currentTenant, null, application);
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_NAME_IS_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingAppNotExists() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.update(currentTenant, otherApplication.getName(), application);
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorIfUpdatingAppBlankFriendlyName() throws Exception {
    	application.setFriendlyName(null);
    	ServiceResponse<Application> serviceResponse = applicationService.update(application.getTenant(), application.getName(), application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.FRIENDLY_NAME_NULL_EMPTY.getCode()));
    	
    	application.setFriendlyName("");
    	serviceResponse = applicationService.update(application.getTenant(), application.getName(), application);
    	assertThat(serviceResponse, hasErrorMessage(Application.Validations.FRIENDLY_NAME_NULL_EMPTY.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldUpdateApp() throws Exception {
    	application.setFriendlyName("Updating friendly name");
    	application.setDescription("Updating description");
    	ServiceResponse<Application> serviceResponse = applicationService.update(application.getTenant(), application.getName(), application);

        assertThat(serviceResponse, isResponseOk());
        assertThat(serviceResponse.getResult().getFriendlyName(), equalTo(application.getFriendlyName()));
        assertThat(serviceResponse.getResult().getDescription(), equalTo(application.getDescription()));
    }
    
    @Test
    public void shouldReturnErrorIfRemovingAppTenantIsNull() throws Exception {
        ServiceResponse<Application> serviceResponse = applicationService.remove(null, "smartffkonker");

        assertThat(serviceResponse, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingAppNameNull() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.remove(currentTenant, null);
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_NAME_IS_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnErrorIfRemovingAppNotExists() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.remove(currentTenant, otherApplication.getName());
    	
    	assertThat(serviceResponse, hasErrorMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldRemoveApp() throws Exception {
    	ServiceResponse<Application> serviceResponse = applicationService.remove(application.getTenant(), application.getName());
    	
    	assertThat(serviceResponse.getStatus(), equalTo(ServiceResponse.Status.OK));
    	assertThat(serviceResponse.getResponseMessages(), hasEntry(Messages.APPLICATION_REMOVED_SUCCESSFULLY.getCode(), null));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnAllApp() throws Exception {
    	ServiceResponse<List<Application>> response = applicationService.findAll(application.getTenant());
    	
    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), notNullValue());
    	assertThat(response.getResult(), hasSize(2));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetAppByNameTenantNull() throws Exception {
    	ServiceResponse<Application> response = applicationService.getByApplicationName(null, application.getName());
    	
    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetAppByNameNull() throws Exception {
    	ServiceResponse<Application> response = applicationService.getByApplicationName(application.getTenant(), null);
    	
    	assertThat(response, hasErrorMessage(Validations.APPLICATION_NAME_IS_NULL.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetAppByNameTenantNotExists() throws Exception {
    	ServiceResponse<Application> response = applicationService.getByApplicationName(otherTenant, application.getName());
    	
    	assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldReturnErrorGetAppByNameAppNotExists() throws Exception {
    	ServiceResponse<Application> response = applicationService.getByApplicationName(application.getTenant(), otherApplication.getName());
    	
    	assertThat(response, hasErrorMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
    public void shouldGetAppByName() throws Exception {
    	ServiceResponse<Application> response = applicationService.getByApplicationName(application.getTenant(), application.getName());
    	
    	assertThat(response, isResponseOk());
    	assertThat(response.getResult(), equalTo(application));
    }

}