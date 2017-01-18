package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RestTransformationStepTest {

    private RestTransformationStep subject;

    @Before
    public void setUp() {
        subject = RestTransformationStep.builder()
            .attributes(new HashMap<String,String>() {
                {
                    put("method", "POST");
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

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_NULL_EMPTY.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfAttributesMapIsEmpty() throws Exception {
        subject = RestTransformationStep.builder().attributes(new HashMap<>()).build();

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_NULL_EMPTY.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfURLAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("url");

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_URL_MISSING.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfURLAttributeIsEmpty() throws Exception {
        subject.getAttributes().put("url","");

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_URL_MISSING.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfURLIsMalformed() throws Exception {
        subject.getAttributes().put("url","xttp://host.com");

        String expectedError = InterpolableURIValidator.Validations.URI_PROTOCOL_INVALID.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfUsernameAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("username");

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_USERNAME_MISSING.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnValidationMessageIfPasswordAttributeIsMissing() throws Exception {
        subject.getAttributes().remove("password");

        String expectedError = RestTransformationStep.Validations.ATTRIBUTES_PASSWORD_MISSING.getCode();
        Optional<Map<String, Object[]>> validations = subject.applyValidations();

        assertThat(validations.isPresent(), is(true));
        assertThat(validations.get(),hasEntry(expectedError,null));
    }
    @Test
    public void shouldReturnNoValidationMessageIfRecordIsValid() throws Exception {
        assertThat(subject.applyValidations().isPresent(),is(false));
    }
}