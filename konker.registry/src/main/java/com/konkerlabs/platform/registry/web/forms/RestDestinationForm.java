package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Optional;

@Data
public class RestDestinationForm implements ModelBuilder<RestDestination, RestDestinationForm, Void> {


    private String name;
    private String method;
    private String serviceURI;
    private String serviceUsername;
    private String servicePassword;
    private boolean active;

    public RestDestinationForm() {
        setActive(Boolean.TRUE);
    }

    @Override
    public RestDestination toModel() {
        return RestDestination.builder()
                .name(getName())
                .serviceURI(getServiceURI())
                .serviceUsername(getServiceUsername())
                .servicePassword(getServicePassword())
                .active(isActive())
                .method(getMethod())
                .build();
    }

    @Override
    public RestDestinationForm fillFrom(RestDestination model) {
        setName(model.getName());
        setServiceURI(model.getServiceURI());
        setServiceUsername(model.getServiceUsername());
        setServicePassword(model.getServicePassword());
        setActive(model.isActive());
        setMethod(model.getMethod());
        setMethod(Optional.ofNullable(model.getMethod()).isPresent() ? model.getMethod() :
                SupportedHttpMethod.POST.getCode());
        return this;
    }

    public SupportedHttpMethod[] getMethodList() {
        return SupportedHttpMethod.values();
    }

}
