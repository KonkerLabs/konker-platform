package com.konkerlabs.platform.utilities.test.validations;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import com.konkerlabs.platform.utilities.validations.ValidationException;

import java.util.Map;
import java.util.Optional;

public class InterpolableURIValidatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenURIIsnull() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_NULL.getCode();
        String uri = null;

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();
        
        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,null));
    }
    
    @Test
    public void shouldThrowExceptionWhenURIEmpty() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_FORMAT_INVALID.getCode();
        String uri = "";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldThrowExceptionWhenSchemeIsInvalid() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_PROTOCOL_INVALID.getCode();
        String uri = "xyz://host.com";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,null));
    }
    
    @Test
    public void shouldThrowExceptionWhenSchemeIsEmpty() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_FORMAT_INVALID.getCode();
        String uri = "//host.com";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,null));
    }
    
    @Test
    public void shouldThrowExceptionWhenHostHasInterpolation() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_MALFORMED.getCode();
        String uri = "http://www.@{#x}/path";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,new Object[] {uri}));
    }

    @Test
    public void shouldThrowExceptionWhenHasUserInfo() throws ValidationException {
        String expectedMessage = InterpolableURIValidator.Validations.URI_USERINFO_NOT_ACCEPTED.getCode();
        String uri = "http://user@www.host/path";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(),hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldAcceptIfHasInterpolationOnPath() throws ValidationException {
        String uri = "http://host/@{#path}/";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(false));
    }

    @Test
    public void shouldAcceptIfHasInterpolationOnQuery() throws ValidationException {
        String uri = "http://host/?@{#inter}";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(false));
    }

    @Test
    public void shouldAcceptIfHasPort() throws ValidationException {
        String uri = "http://host:10/?@{#inter}";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(false));
    }

    @Test
    public void shouldAcceptIfHasIP() throws ValidationException {
        String uri = "http://0.0.0.0:10/?@{#inter}";

        Optional<Map<String, Object[]>> validations = InterpolableURIValidator.to(uri).applyValidations();

        assertThat(validations.isPresent(),is(false));
    }

}
