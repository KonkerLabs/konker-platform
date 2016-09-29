package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasAllErrors;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.TransformationRepository;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class
})
public class TransformationServiceTest extends BusinessLayerTestSupport {

    private static final String TRANSFORMATION_GUID_IN_USE = "2747ec73-6910-43a1-8ddc-5a4a134ebab3";
    private static final String ANOTHER_TENANTS_TRANSFORMATION_GUID = "6eed0ed6-8542-40ff-b984-63c914827d24";
    private static final String TRANSFORMATION_GUID_NO_ROUTES = "00d86f6a-648e-43f5-bb12-066d48a667c1";
    private static final String TRANFORMATION_NAME_IN_USE = "Transformation name 01";
    private static final String ANOTHER_TRANSFORMATION_NAME_IN_USE = "Another Transformation name 01";

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private TransformationRepository transformationRepository;

    @Autowired
    private TransformationService subject;
    private Tenant tenant;
    private Transformation transformation;

    @Before
    public void setUp() throws Exception {
        tenant = tenantRepository.findByDomainName("konker");

        transformation = Transformation.builder()
            .name("Test name")
            .description("Description")
            .step(
                RestTransformationStep.builder()
                .attributes(new HashMap<String,String>() {{
                    put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,"http://host.com");
                    put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,"username");
                    put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,"password");
                }}).build()
            ).build();
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldReturnAllRegisteredTransformations() throws Exception {
        tenantRepository.findAll().stream().forEach(tenant1 -> {
            List<Transformation> transformations = transformationRepository.findAllByTenantId(tenant1.getId());

            ServiceResponse<List<Transformation>> response;
            response = subject.getAll(tenant1);

            assertThat(response,isResponseOk());
            assertThat(response.getResult(),equalTo(transformations));
        });
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.register(null,transformation);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantDoesNotExist() throws Exception {
        tenant = Tenant.builder().id("unknown_id").build();

        ServiceResponse<Transformation> serviceResponse = subject.register(tenant,transformation);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidationMessageIfTransformationIsNull() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.register(tenant,null);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidationMessageIfTransformationIsInvalid() throws Exception {
        Map<String, Object[]> validationErrors = new HashMap() {{
            put("Some error",null);
        }};

        transformation = spy(transformation);
        when(transformation.applyValidations()).thenReturn(Optional.of(validationErrors));

        ServiceResponse<Transformation> serviceResponse = subject.register(tenant,transformation);

        assertThat(serviceResponse,hasAllErrors(validationErrors));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldReturnAValidationMessageIfTransformationNameIsAlreadyInUse() throws Exception {
        transformation.setName(TRANFORMATION_NAME_IN_USE);

        ServiceResponse<Transformation> serviceResponse = subject.register(tenant,transformation);

        assertThat(serviceResponse,hasErrorMessage(TransformationService.Validations.TRANSFORMATION_NAME_IN_USE.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldPersistWhenValid() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.register(tenant,transformation);

        assertThat(serviceResponse,isResponseOk());
        assertThat(serviceResponse.getResult(),equalTo(transformation));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldFindATransformationByItsGuid() throws Exception {
        Transformation found = transformationRepository.findByGuid(TRANSFORMATION_GUID_IN_USE);

        ServiceResponse<Transformation> response = subject.get(tenant, TRANSFORMATION_GUID_IN_USE);

        assertThat(response,isResponseOk());
        assertThat(response.getResult(),equalTo(found));

        response = subject.get(tenant,ANOTHER_TENANTS_TRANSFORMATION_GUID);

        assertThat(response,isResponseOk());
        assertThat(response.getResult(),nullValue());
    }

    @Test
    public void shouldReturnValidationMessageIfTenantIsNullWhenUpdating() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.update(null,TRANSFORMATION_GUID_IN_USE,transformation);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantDoesNotExistWhenUpdating() throws Exception {
        tenant = Tenant.builder().id("unknown_id").build();

        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,TRANSFORMATION_GUID_IN_USE,transformation);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidationMessageIfTransformationIsNullWhenUpdating() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,TRANSFORMATION_GUID_IN_USE,null);

        assertThat(serviceResponse,hasErrorMessage(CommonValidations.RECORD_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnAValidationMessageIfTranformationGuidDoesNotExistWithinTenant() throws Exception {
        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,ANOTHER_TENANTS_TRANSFORMATION_GUID,transformation);

        assertThat(serviceResponse,hasErrorMessage(TransformationService.Validations.TRANSFORMATION_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldReturnAValidationMessageIfTransformationIsInvalidWhenUpdating() throws Exception {
        transformation.setName(null);

        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,TRANSFORMATION_GUID_IN_USE,transformation);

        assertThat(serviceResponse.getResponseMessages().isEmpty(),is(false));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldReturnAValidationMessageIfTransformationNameIsAlreadyInUseWhenUpdating() throws Exception {
        transformation.setName(ANOTHER_TRANSFORMATION_NAME_IN_USE);

        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,TRANSFORMATION_GUID_IN_USE,transformation);

        assertThat(serviceResponse,hasErrorMessage(TransformationService.Validations.TRANSFORMATION_NAME_IN_USE.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/transformations.json"})
    public void shouldUpdateWhenValid() throws Exception {
        transformation.setName("Updated name");
        transformation.setSteps(Arrays.asList(new TransformationStep[] {
            RestTransformationStep.builder()
            .attributes(new HashMap<String,String>() {{
                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,"https://otherhost.com");
                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,"username");
                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,"password");
            }}).build()})
        );

        ServiceResponse<Transformation> serviceResponse = subject.update(tenant,TRANSFORMATION_GUID_IN_USE,transformation);

        assertThat(serviceResponse,isResponseOk());
        assertThat(serviceResponse.getResult().getName(),equalTo(transformation.getName()));
        assertThat(serviceResponse.getResult().getSteps(),equalTo(transformation.getSteps()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnAValidationMessageIfTransformationBelongAnotherTenantWhenRemoving() throws Exception {
    	ServiceResponse<Transformation> serviceResponse = subject.remove(tenant, ANOTHER_TENANTS_TRANSFORMATION_GUID);
    	
    	assertThat(serviceResponse, hasErrorMessage(TransformationService.Validations.TRANSFORMATION_BELONG_ANOTHER_TENANT.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldReturnAValidationMessageIfTransformationHasRoutesWhenRemoving() throws Exception {
    	ServiceResponse<Transformation> serviceResponse = subject.remove(tenant, TRANSFORMATION_GUID_IN_USE);
    	
    	assertThat(serviceResponse, hasErrorMessage(TransformationService.Validations.TRANSFORMATION_HAS_ROUTE.getCode()));
    }
    
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/transformations.json", "/fixtures/event-routes.json"})
    public void shouldRemoveSuccessfully() throws Exception {
    	ServiceResponse<Transformation> serviceResponse = subject.remove(tenant, TRANSFORMATION_GUID_NO_ROUTES);

    	Transformation removedTransformation = subject.get(tenant, TRANSFORMATION_GUID_NO_ROUTES).getResult();
    	
    	assertThat(serviceResponse, isResponseOk());
    	assertThat(removedTransformation, nullValue());
    }
}