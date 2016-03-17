package com.konkerlabs.platform.registry.business.model.behaviors;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

public interface SmsDestinationURIDealer {

    String SMS_URI_SCHEME = "sms";
    String SMS_URI_TEMPLATE = SMS_URI_SCHEME + "://{0}/{1}";

    default URI toSmsURI(String tenantDomain, String guid) {
        Optional.ofNullable(guid)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException(
                "SMS GUID cannot be null or empty"
            ));

        Optional.ofNullable(tenantDomain)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException(
                    "SMS Destination tenant domain cannot be null or empty"
            ));

        return URI.create(
                MessageFormat.format(SMS_URI_TEMPLATE,tenantDomain,guid)
        );
    }

}
