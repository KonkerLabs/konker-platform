package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TransformationTest {

    private Tenant tenant;
    private Transformation transformation;
    private RestTransformationStep restTransformation;

    @Before
    public void setUp() {
        tenant = Tenant.builder()
                .domainName("domain")
                .name("Name")
                .id("id").build();

        restTransformation = spy(RestTransformationStep.builder()
                .attributes(new HashMap<String, Object>() {
                    {
                        put("url","http://host:8080/path?query=1");
                        put("username","username");
                        put("password","password");
                    }
                })
                .build());

        transformation = Transformation.builder()
            .tenant(tenant)
            .name("Transformation Name")
            .description("Description")
            .step(restTransformation)
            .build();
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        transformation.setTenant(null);

        String expectedMessage = CommonValidations.TENANT_NULL.getCode();
        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        assertThat(validations, not(sameInstance(Optional.empty())));
        assertThat(transformation.applyValidations().get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        transformation.setName(null);

        String expectedMessage = Transformation.Validations.NAME_NULL.getCode();
        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        assertThat(validations, not(sameInstance(Optional.empty())));
        assertThat(transformation.applyValidations().get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        transformation.setName("");

        String expectedMessage = Transformation.Validations.NAME_NULL.getCode();
        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        assertThat(validations, not(sameInstance(Optional.empty())));
        assertThat(transformation.applyValidations().get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfStepsCollectionIsEmpty() throws Exception {
        transformation.setSteps(Collections.emptyList());

        String expectedMessage = Transformation.Validations.STEPS_EMPTY.getCode();
        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        assertThat(validations, not(sameInstance(Optional.empty())));
        assertThat(transformation.applyValidations().get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldForwardValidationMessagesFromAnyOfItsSteps() throws Exception {
        Map<String,Object[]> expectedStepErrors = new HashMap();
        expectedStepErrors.put("Some error",null);


        when(restTransformation.applyValidations()).thenReturn(
            Optional.of(expectedStepErrors)
        );

        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        assertThat(validations, not(sameInstance(Optional.empty())));
        assertThat(transformation.applyValidations().get(),hasEntry("Some error",null));
    }
}