package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Optional;

@Document(collection = "tenants")
@Data
@Builder
public class Tenant implements URIDealer, Serializable {

    @Id
    private String id;
    private String name;
    private String domainName;
    private LogLevel logLevel = LogLevel.WARNING;
    private Long devicesLimit;
    private Long privateStorageSize;
    private Boolean chargeable;
    private PlanEnum plan;

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

    public enum PlanEnum {
        STARTER("users.form.plan.starter"),
        STANDARD("users.form.plan.standard"),
        CORPORATE("users.form.plan.corporate"),
        ENTERPRISE("users.form.plan.enterprise");

        public String getValue() {
            return value;
        }

        private String value;

        PlanEnum(String value) {
            this.value = value;
        }
    }

}
