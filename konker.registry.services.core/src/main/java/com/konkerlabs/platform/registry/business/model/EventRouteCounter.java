package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Document(collection = "eventRouteCounter")
@Data
@Builder
public class EventRouteCounter implements URIDealer, Validatable {

    public static final String DEVICE_MQTT_CHANNEL = "channel";

    public enum Validations {
        GUID_NULL("model.event_route_counter.guid.not_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private EventRoute eventRoute;

    private Long performedTimes;
    private Instant creationDate;
    private String guid;

    public static final String URI_SCHEME = "eventroutecounter";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return getTenant() != null ? getTenant().getDomainName() : null;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public Optional<Map<String,Object[]>> applyValidations() {
        Map<String,Object[]> validations = new HashMap<>();

        if (getTenant() == null)
            validations.put(CommonValidations.TENANT_NULL.getCode(),null);
        if (getGuid() == null || getGuid().isEmpty())
            validations.put(Validations.GUID_NULL.getCode(),null);

 		return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
 	}

 	public void addPerformedTimes() {
        this.performedTimes += 1;
    }

}
