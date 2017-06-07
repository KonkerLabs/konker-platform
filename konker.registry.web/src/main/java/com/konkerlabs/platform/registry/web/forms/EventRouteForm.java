package com.konkerlabs.platform.registry.web.forms;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EventRouteForm
        implements ModelBuilder<EventRoute,EventRouteForm,String>, URIDealer {

    private String id;
    private String name;
    private String description;
    private String incomingScheme = Device.URI_SCHEME;
    private EventRouteActorForm incoming = new EventRouteActorForm();
    private String outgoingScheme = Device.URI_SCHEME;
    private EventRouteActorForm outgoing = new EventRouteActorForm();
    private String filteringExpression;
    private String transformation;
    private boolean active;
    private Supplier<String> tenantDomainSupplier;

    public EventRouteForm() {
		setActive(Boolean.TRUE);
	}

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
                .incoming(RouteActor.builder().uri(toURI(URI_TEMPLATE, tenantDomainSupplier.get(),
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
        return toURI(
                URI_TEMPLATE,
                tenantDomainSupplier.get(),
                getOutgoing().getAuthorityId(),
                getOutgoingScheme()
        );
    }

    private URI toURI(String tpl, String ctx, String guid, String uriScheme) {
        return URI.create(
            MessageFormat.format(tpl, uriScheme, ctx, guid)
        );
    }

    private URI toURI(String tpl, String ctx, String guid) {
        return URI.create(
            MessageFormat.format(tpl, getUriScheme(), ctx, guid)
        );
    }

    public static final String URI_SCHEME = Device.URI_SCHEME;

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return name;
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public EventRouteForm fillFrom(EventRoute model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setIncomingScheme(model.getIncoming().getUri().getScheme());
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
