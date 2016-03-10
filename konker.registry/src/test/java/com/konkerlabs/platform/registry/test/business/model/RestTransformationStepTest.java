package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

public class RestTransformationStepTest {

    private RestTransformationStep subject;

    @Before
    public void setUp() {
        subject = RestTransformationStep.builder()
            .attributes(new HashMap<String,String>() {
                {
                    put("url","http://host:8080/path?query=1");
                    put("username","username");
                    put("password","password");
                }
            }).build();
    }

    @Test
    public void shoultReturnValidationMessageIfTypeIsNotREST() throws Exception {
        assertThat(subject.getType(),equalTo(IntegrationType.REST));
    }
    @Test
    public void shouldReturnValidationMessageIfAttributesMapIsNull() throws Exception {
        subject = RestTransformationStep.builder().build();

        String expectedError = "REST step attributes cannot be null or empty";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
    @Test
    public void shouldReturnValidationMessageIfAttributesMapIsEmpty() throws Exception {
        subject = RestTransformationStep.builder().attributes(new HashMap<>()).build();

        String expectedError = "REST step attributes cannot be null or empty";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
    @Test
    public void shouldReturnValidationMessageIfURLAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("url");

        String expectedError = "REST step: URL attribute is missing";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
    @Test
    public void shouldReturnValidationMessageIfURLAttributeIsEmpty() throws Exception {
        subject.getAttributes().put("url","");

        String expectedError = "REST step: URL attribute is missing";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
    @Test
    public void shouldReturnValidationMessageIfUsernameAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("username");

        String expectedError = "REST step: Username attribute is missing";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
    @Test
    public void shouldReturnValidationMessageIfPasswordAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("password");

        String expectedError = "REST step: Password attribute is missing";

        assertThat(subject.applyValidations(),hasItem(expectedError));
    }
}