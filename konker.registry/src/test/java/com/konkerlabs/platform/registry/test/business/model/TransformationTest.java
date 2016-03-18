package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
                .attributes(new HashMap<String,String>() {
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

        String expectedMessage = "Tenant cannot be null";

        assertThat(transformation.applyValidation(),hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        transformation.setName(null);

        String expectedMessage = "Name cannot be null or empty";

        assertThat(transformation.applyValidation(),hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        transformation.setName("");

        String expectedMessage = "Name cannot be null or empty";

        assertThat(transformation.applyValidation(),hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfStepsCollectionIsEmpty() throws Exception {
        transformation.setSteps(Collections.emptyList());

        String expectedMessage = "At least one transformation step is needed";

        assertThat(transformation.applyValidation(),hasItem(expectedMessage));
    }

    @Test
    public void shouldForwardValidationMessagesFromAnyOfItsSteps() throws Exception {
        String expectedStepError = "Some error";

        when(restTransformation.applyValidations()).thenReturn(
            new HashSet(Arrays.asList(new String[] {expectedStepError}))
        );

        assertThat(transformation.applyValidation(),hasItem(expectedStepError));
    }
}