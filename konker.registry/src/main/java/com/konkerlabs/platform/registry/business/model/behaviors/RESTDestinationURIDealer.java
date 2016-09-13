package com.konkerlabs.platform.registry.business.model.behaviors;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

public interface RESTDestinationURIDealer {

    String REST_DESTINATION_URI_SCHEME = "rest";
    String REST_DESTINATION_URI_TEMPLATE = REST_DESTINATION_URI_SCHEME + "://{0}/{1}";

    default URI toRestDestinationURI(String tenantDomain, String guid) {
        Optional.ofNullable(guid)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException(
                "REST Destination ID cannot be null or empty"
            ));

        Optional.ofNullable(tenantDomain)
        .filter(s -> !s.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException(
            "REST Destination tenant domain cannot be null or empty"
        ));

        return URI.create(
                MessageFormat.format(REST_DESTINATION_URI_TEMPLATE, tenantDomain, guid)
        );
    }

}
