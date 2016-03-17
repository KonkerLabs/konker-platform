package com.konkerlabs.platform.registry.web.forms;

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
import java.util.Optional;
import java.util.function.Supplier;

import static com.konkerlabs.platform.registry.business.model.EventRoute.builder;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EventRouteForm implements ModelBuilder<EventRoute,EventRouteForm,String>,
        DeviceURIDealer,
        SmsDestinationURIDealer,
        RESTDestinationURIDealer {

    private String id;
    private String name;
    private String description;
    private String incomingAuthority;
    private String incomingChannel;
    private String outgoingScheme = DeviceURIDealer.DEVICE_URI_SCHEME;
    private String outgoingDeviceAuthority;
    private String outgoingDeviceChannel;
    private String outgoingSmsDestinationGuid;
    private String outgoingSmsTemplateType;
    private String outgoingSmsCustomText;
    private String outgoingRestDestinationGuid;
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

        route = builder()
                .id(id)
                .name(getName())
                .description(getDescription())
                .incoming(RouteActor.builder().uri(toDeviceRouteURI(tenantDomainSupplier.get(), getIncomingAuthority())).data(new HashMap<>()).build())
                .outgoing(RouteActor.builder().uri(buildOutgoingURI()).data(new HashMap<>()).build())
                .filteringExpression(getFilteringExpression())
                .transformation(
                    Optional.ofNullable(getTransformation()).filter(value -> !value.isEmpty())
                        .map(selectedId -> Transformation.builder().id(selectedId).build())
                        .orElseGet(() -> null)
                )
                .active(isActive())
                .build();
        applyIncomingMetadata(route);
        applyOutgoingMetadata(route);

        return route;
    }

    private URI buildOutgoingURI() {
        switch (getOutgoingScheme()) {
            case DeviceURIDealer.DEVICE_URI_SCHEME : return
                toDeviceRouteURI(tenantDomainSupplier.get(),getOutgoingDeviceAuthority());
            case SmsDestinationURIDealer.SMS_URI_SCHEME : return
                toSmsURI(tenantDomainSupplier.get(), getOutgoingSmsDestinationGuid());
            case RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME : return
                toRestDestinationURI(tenantDomainSupplier.get(),getOutgoingRestDestinationGuid());
            default: return null;
        }
    }

    private void applyIncomingMetadata(EventRoute route) {
        route.getIncoming().getData().put("channel",getIncomingChannel());
    }

    private void applyOutgoingMetadata(EventRoute route) {
        switch (getOutgoingScheme()) {
            case DeviceURIDealer.DEVICE_URI_SCHEME : {
                route.getOutgoing().getData().put("channel", getOutgoingDeviceChannel());
                break;
            }
            case SmsDestinationURIDealer.SMS_URI_SCHEME : {
                route.getOutgoing().getData().put("templateType", getOutgoingSmsTemplateType());
                route.getOutgoing().getData().put("customText", getOutgoingSmsCustomText());
                break;
            }
            default: break;
        }
    }

    @Override
    public EventRouteForm fillFrom(EventRoute model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setIncomingAuthority(model.getIncoming().getUri().getPath().replaceAll("/",""));
        this.setIncomingChannel(model.getIncoming().getData().get("channel"));
        this.setOutgoingScheme(model.getOutgoing().getUri().getScheme());
        switch (getOutgoingScheme()) {
            case DeviceURIDealer.DEVICE_URI_SCHEME : {
                this.setOutgoingDeviceAuthority(model.getOutgoing().getUri().getPath().replaceAll("/",""));
                this.setOutgoingDeviceChannel(model.getOutgoing().getData().get("channel"));
                break;
            }
            case SmsDestinationURIDealer.SMS_URI_SCHEME : {
                this.setOutgoingSmsDestinationGuid(model.getOutgoing().getUri().getPath().replaceAll("/",""));
                this.setOutgoingSmsTemplateType(model.getOutgoing().getData().get("templateType"));
                this.setOutgoingSmsCustomText(model.getOutgoing().getData().get("customText"));
                break;
            }
            case RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME : {
                this.setOutgoingRestDestinationGuid(model.getOutgoing().getUri().getPath().replaceAll("/",""));
                break;
            }
            default: break;
        }
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
}
