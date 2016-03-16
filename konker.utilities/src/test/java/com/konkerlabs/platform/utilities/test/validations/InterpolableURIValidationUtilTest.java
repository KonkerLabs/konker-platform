package com.konkerlabs.platform.utilities.test.validations;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.konkerlabs.platform.utilities.validations.InterpolableURIValidationUtil;
import com.konkerlabs.platform.utilities.validations.ValidationException;

public class InterpolableURIValidationUtilTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    
    
    @Test
    public void shouldThrowExceptionWhenURIIsnull() throws ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("URI cannot be null");

        String uri = null;
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }
    
    @Test
    public void shouldThrowExceptionWhenURIEmpty() throws ValidationException {
        thrown.expect(ValidationException.class);

        String uri = "";
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldThrowExceptionWhenSchemeIsInvalid() throws ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Protocol");

        String uri = "xyz://host.com";
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }
    
    @Test
    public void shouldThrowExceptionWhenSchemeIsEmpty() throws ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("format");

        String uri = "//host.com";
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }
    
    @Test
    public void shouldThrowExceptionWhenHostHasInterpolation() throws ValidationException {
        thrown.expect(ValidationException.class);

        String uri = "http://www.@{#x}/path";
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldThrowExceptionWhenHasUserInfo() throws ValidationException {
        thrown.expect(ValidationException.class);

        String uri = "http://user@www.host/path";
        
        assertFalse(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldAcceptIfHasInterpolationOnPath() throws ValidationException {
        String uri = "http://host/@{#path}/";
        
        assertTrue(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldAcceptIfHasInterpolationOnQuery() throws ValidationException {
        String uri = "http://host/?@{#inter}";
        
        assertTrue(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldAcceptIfHasPort() throws ValidationException {
        String uri = "http://host:10/?@{#inter}";
        
        assertTrue(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

    @Test
    public void shouldAcceptIfHasIP() throws ValidationException {
        String uri = "http://0.0.0.0:10/?@{#inter}";
        
        assertTrue(InterpolableURIValidationUtil.isValid(uri));
        InterpolableURIValidationUtil.validate(uri);
    }

}
