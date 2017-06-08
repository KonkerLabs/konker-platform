package com.konkerlabs.platform.registry.web.forms;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.util.StringUtils;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EventRouteForm
        implements ModelBuilder<EventRoute,EventRouteForm,String> {

    private static final int AUTH_IDS_SIZE = 4;
    private String id;
    private String name;
    private String applicationName;
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
                .incoming(RouteActor.builder()
                                    .uri(buildIncomingURI())
                                    .data(getIncoming().getAuthorityData())
                                    .build())
                .outgoing(RouteActor.builder()
                                    .uri(buildOutgoingURI())
                                    .data(getOutgoing().getAuthorityData())
                                    .build())
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

    private final String URI_TEMPLATE = "{0}://{1}/{2}";

    private URI buildIncomingURI() {
        return toURI(
                getIncomingScheme(),
                tenantDomainSupplier.get(),
                getIncoming().getAuthorityIds()
        );
    }

    private URI buildOutgoingURI() {
        return toURI(
                getOutgoingScheme(),
                tenantDomainSupplier.get(),
                getOutgoing().getAuthorityIds()
        );
    }

    private URI toURI(String scheme, String tenantDomain, String[] authorityIds) {
        StringBuilder sb = new StringBuilder();

        sb.append(scheme);
        sb.append("://");
        sb.append(tenantDomain);

        for (int i = 0; i < authorityIds.length; i++) {
            if (StringUtils.hasText(authorityIds[i])) {
                sb.append("/");
                sb.append(authorityIds[i]);
            }
        }

        return URI.create(sb.toString());
    }

    @Override
    public EventRouteForm fillFrom(EventRoute model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setApplicationName(model.getApplication().getName());
        this.setDescription(model.getDescription());
        this.setIncomingScheme(model.getIncoming().getUri().getScheme());
        this.getIncoming().setAuthorityIds(getAuthorityIds(model.getIncoming().getUri()));
        this.getIncoming().setAuthorityData(model.getIncoming().getData());
        this.setOutgoingScheme(model.getOutgoing().getUri().getScheme());
        this.getOutgoing().setAuthorityIds(getAuthorityIds(model.getOutgoing().getUri()));
        this.getOutgoing().setAuthorityData(model.getOutgoing().getData());
        this.setFilteringExpression(model.getFilteringExpression());
        this.setTransformation(
            Optional.ofNullable(model.getTransformation())
                .map(t -> t.getId()).orElseGet(() -> null)
        );
        this.setActive(model.isActive());
        return this;
    }

    private String[] getAuthorityIds(URI uri) {
        String[] authorityIds = new String[AUTH_IDS_SIZE];

        int pos = 0;

        String tokens[] = uri.getPath().split("\\/");
        for (String token: tokens) {
            if (StringUtils.hasLength(token)) {
                authorityIds[pos] = token;
                pos++;
            }
        }

        return authorityIds;
    }

    @Override
    public void setAdditionalSupplier(Supplier<String> supplier) {
        tenantDomainSupplier = supplier;
    }

    @Data
    public static class EventRouteActorForm {

    	private String[] authorityIds = new String[AUTH_IDS_SIZE];
        private Map<String,String> authorityData = new HashMap<>();

        public void setAuthorityId(String authorityId) {
            authorityIds[0] = authorityId;
        }

        public String getAuthorityId() {
            return authorityIds[0];
        }

    }

}
