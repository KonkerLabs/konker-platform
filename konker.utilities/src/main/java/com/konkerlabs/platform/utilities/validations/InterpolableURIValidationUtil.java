package com.konkerlabs.platform.utilities.validations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InterpolableURIValidationUtil {
    public static boolean isValid(String uri) {
        try {
            validate(uri);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    public static void validate(String uri) throws ValidationException {
        if (uri == null) {
            throw new ValidationException("URI cannot be null");
        }

        Pattern p = Pattern.compile("(.*?\\://[^/]+)(/.*)?");
        Matcher m = p.matcher(uri);
        if (!m.matches()) {
            throw new ValidationException("URI format invalid");
        }

        String base = m.group(1);
        
        URI parsedURI;
        try {
            parsedURI = new URI(base);
        } catch (URISyntaxException e) {
            throw new ValidationException(e.getMessage(), e);
        }

        if (!("HTTP".equalsIgnoreCase(parsedURI.getScheme()) || "HTTPS".equalsIgnoreCase(parsedURI.getScheme()))) {
            throw new ValidationException("Protocol must be HTTP or HTTPS");
        }

        if (parsedURI.getUserInfo() != null) {
            throw new ValidationException("User info may not be provided as part of URL");
        }

        if (parsedURI.getHost() == null || "".equals(parsedURI.getHost())) {
            throw new ValidationException("Host is mandatory");
        }
    }

}
