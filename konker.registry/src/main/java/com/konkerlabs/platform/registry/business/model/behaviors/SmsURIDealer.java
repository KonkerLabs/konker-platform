package com.konkerlabs.platform.registry.business.model.behaviors;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

public interface SmsURIDealer {

    String SMS_URI_SCHEME = "sms";
    String SMS_URI_TEMPLATE = SMS_URI_SCHEME + "://{0}";

    default URI toSmsURI(String phoneNumber) {
        Optional.ofNullable(phoneNumber)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException(
                "SMS Phone number cannot be null or empty"
            ));

        return URI.create(
                MessageFormat.format(SMS_URI_TEMPLATE,phoneNumber)
        );
    }

}
