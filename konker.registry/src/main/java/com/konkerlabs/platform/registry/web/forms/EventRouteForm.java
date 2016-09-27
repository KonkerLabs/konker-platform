package com.konkerlabs.platform.registry.web.forms;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EventRouteForm implements ModelBuilder<EventRoute,EventRouteForm,String>,
        DeviceURIDealer,
        SmsDestinationURIDealer,
        RESTDestinationURIDealer {

    private String id;
    private String name;
    private String description;
    private EventRouteActorForm incoming = new EventRouteActorForm();
    private String outgoingScheme = DeviceURIDealer.DEVICE_URI_SCHEME;
    private EventRouteActorForm outgoing = new EventRouteActorForm();
    private String filteringExpression;
    private String transformation;
    private boolean active;
    private Supplier<String> tenantDomainSupplier;

    @Override
    public EventRoute toModel() {
        Optional.ofNullable(tenantDomainSupplier)
            .orElseThrow(() -> new IllegalStateException("Tenant domain name supplier cannot be null"));

        Optional.ofNullable(tenantDomainSupplier.get())
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalStateException("Tenant domain name supplier cannot return null or empty"));

        EventRoute route;

        route = EventRoute.builder()
                .id(id)
                .name(getName())
                .description(getDescription())
                .incoming(RouteActor.builder().uri(toDeviceRouteURI(tenantDomainSupplier.get(),
                        getIncoming().getAuthorityId())).data(getIncoming().getAuthorityData()).build())
                .outgoing(RouteActor.builder().uri(buildOutgoingURI()).data(getOutgoing().getAuthorityData()).build())
                .filteringExpression(getFilteringExpression())
                .transformation(
                    Optional.ofNullable(getTransformation()).filter(value -> !value.isEmpty())
                        .map(selectedId -> Transformation.builder().id(selectedId).build())
                        .orElseGet(() -> null)
                )
                .active(isActive())
                .build();

        return route;
    }

    private URI buildOutgoingURI() {
        switch (getOutgoingScheme()) {
            case DeviceURIDealer.DEVICE_URI_SCHEME : return
                toDeviceRouteURI(tenantDomainSupplier.get(),getOutgoing().getAuthorityId());
            case SmsDestinationURIDealer.SMS_URI_SCHEME : return
                toSmsURI(tenantDomainSupplier.get(), getOutgoing().getAuthorityId());
            case RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME : return
                toRestDestinationURI(tenantDomainSupplier.get(),getOutgoing().getAuthorityId());
            default: return null;
        }
    }

    @Override
    public EventRouteForm fillFrom(EventRoute model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.getIncoming().setAuthorityId(model.getIncoming().getUri().getPath().replaceAll("/",""));
        this.getIncoming().setAuthorityData(model.getIncoming().getData());
        this.setOutgoingScheme(model.getOutgoing().getUri().getScheme());
        this.getOutgoing().setAuthorityId(model.getOutgoing().getUri().getPath().replaceAll("\\/",""));
        this.getOutgoing().setAuthorityData(model.getOutgoing().getData());
        this.setFilteringExpression(model.getFilteringExpression());
        this.setTransformation(
            Optional.ofNullable(model.getTransformation())
                .map(t -> t.getId()).orElseGet(() -> null)
        );
        this.setActive(model.isActive());
        return this;
    }

    @Override
    public void setAdditionalSupplier(Supplier<String> supplier) {
        tenantDomainSupplier = supplier;
    }

    @Data
    public static class EventRouteActorForm {
        private String authorityId;
        private Map<String,String> authorityData = new HashMap<>();
    }
}
