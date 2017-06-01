package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Optional;

@Document(collection = "tenants")
@Data
@Builder
public class Tenant implements URIDealer {

    @Id
    private String id;
    private String name;
    private String domainName;
    private String dataApiDomain; // subscrive and publish events API domain name
    private LogLevel logLevel = LogLevel.WARNING;
    private Long devicesLimit;

    public static final String URI_SCHEME = "tenant";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return domainName;
    }

    @Override
    public String getGuid() {
        return id;
    }

	public LogLevel getLogLevel() {
		return Optional.ofNullable(logLevel).orElse(LogLevel.WARNING);
	}

}
