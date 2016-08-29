package com.konkerlabs.platform.utilities.validations;

import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterpolableURIValidator implements Validatable {

    public enum Validations {
        URI_NULL("domain.uri.not_null"),
        URI_FORMAT_INVALID("domain.uri.format_not_valid"),
        URI_MALFORMED("domain.uri.malformed"),
        URI_PROTOCOL_INVALID("domain.uri.protocol.not_valid"),
        URI_USERINFO_NOT_ACCEPTED("domain.uri.user_info.not_accepted"),
        URI_HOST_NULL("domain.uri.host.not_null");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    private String uri;

    private InterpolableURIValidator(String uri) {
        this.uri = uri;
    }

    public static InterpolableURIValidator to(String uri) {
        return new InterpolableURIValidator(uri);
    }

    @Override
    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        if (uri == null) {
            validations.put(Validations.URI_NULL.getCode(),null);
        } else {

            Pattern p = Pattern.compile("(.*?\\://[^/]+)(/.*)?");
            Matcher m = p.matcher(uri);
            if (!m.matches()) {
                validations.put(Validations.URI_FORMAT_INVALID.getCode(), null);
            } else {
                String base = m.group(1);

                URI parsedURI = null;
                try {
                    parsedURI = new URI(base);
                } catch (URISyntaxException e) {
                    validations.put(Validations.URI_MALFORMED.getCode(), new Object[]{uri});
                }

                Optional.ofNullable(parsedURI).ifPresent(uri -> {
                    if (!("HTTP".equalsIgnoreCase(uri.getScheme()) || "HTTPS".equalsIgnoreCase(uri.getScheme()))) {
                        validations.put(Validations.URI_PROTOCOL_INVALID.getCode(), null);
                    }

                    if (uri.getUserInfo() != null) {
                        validations.put(Validations.URI_USERINFO_NOT_ACCEPTED.getCode(), null);
                    }

                    if (uri.getHost() == null || "".equals(uri.getHost())) {
                        validations.put(Validations.URI_HOST_NULL.getCode(), null);
                    }
                });
            }
        }

        return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
    }
}
