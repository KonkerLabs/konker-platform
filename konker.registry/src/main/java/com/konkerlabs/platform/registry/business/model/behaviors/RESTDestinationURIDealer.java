package com.konkerlabs.platform.registry.business.model.behaviors;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

public interface RESTDestinationURIDealer {

    String REST_DESTINATION_URI_SCHEME = "rest";
    String SMS_DESTNATION_TEMPLATE = REST_DESTINATION_URI_SCHEME + "://{0}";

    default URI toRestDestinationURI(String name) {
        Optional.ofNullable(name)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException(
                "REST Destination name cannot be null or empty"
            ));

        return URI.create(
                MessageFormat.format(REST_DESTINATION_URI_SCHEME, name)
        );
    }

}
