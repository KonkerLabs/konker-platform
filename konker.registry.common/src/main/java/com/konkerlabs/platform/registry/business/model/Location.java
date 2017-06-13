package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Builder
@Document(collection = "locations")
@EqualsAndHashCode(of = {"guid", "name"})
public class Location implements URIDealer, Validatable {

    public static final String URI_SCHEME = "location";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return getTenant() != null ? getTenant().getDomainName() : null;
    }

    public enum Validations {
        NAME_INVALID("model.location.name.invalid"),
        NAME_NULL_EMPTY("model.location.name.not_null");

        public String getCode() {
            return code;
        }

        private String code;

        Validations(String code) {
            this.code = code;
        }
    }

    public enum Messages {

    }

    @Tolerate
    public Location() {
    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    @DBRef
    private Location parent;
    private String name;
    private String description;
    private String guid;
    private boolean defaultLocation;

    @Transient
    private List<Location> childrens;

    public Optional<Map<String, Object[]>> applyValidations() {
        Pattern regex = Pattern.compile("[a-zA-Z0-9\u00C0-\u00FF .\\-+_]{2,100}");

        Map<String, Object[]> validations = new HashMap<>();

        if (getTenant() == null) {
            validations.put(CommonValidations.TENANT_NULL.getCode(), null);
        }
        if (getName() == null) {
            validations.put(Validations.NAME_NULL_EMPTY.code, null);
        }
        if (getName() != null && !regex.matcher(getName()).matches()) {
            validations.put(Validations.NAME_INVALID.code,null);
        }

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

}
