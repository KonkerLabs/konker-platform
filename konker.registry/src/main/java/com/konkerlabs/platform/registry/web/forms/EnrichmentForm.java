package com.konkerlabs.platform.registry.web.forms;

import java.net.URI;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EnrichmentForm implements ModelBuilder<DataEnrichmentExtension, EnrichmentForm, String>, URIDealer {

    private String id;
    private String name;
    private String type;
    private String description;
    private String incomingAuthority;
    private String containerKey;
    //FIXME Retrieve parameters associated with enrichment type from database?
    private Map<String, String> parameters = new LinkedHashMap<String, String>(){{
        put(DataEnrichmentExtension.URL, "");
        put(DataEnrichmentExtension.USERNAME, "");
        put(DataEnrichmentExtension.PASSWORD, "");
    }};
    private boolean active;
    private Supplier<String> tenantDomainSupplier;

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

    private URI toURI(String ctx, String guid) {
        return URI.create(
                MessageFormat.format(URI_TEMPLATE, getUriScheme(), ctx, guid)
        );
    }
    
    public EnrichmentForm() {
		setActive(Boolean.TRUE);
	}

    @Override
    public DataEnrichmentExtension toModel() {
        Optional.ofNullable(tenantDomainSupplier)
                .orElseThrow(() -> new IllegalStateException("Tenant domain name supplier cannot be null"));

        Optional.ofNullable(tenantDomainSupplier.get())
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalStateException("Tenant domain name supplier cannot return null or empty"));

        DataEnrichmentExtension dataEnrichmentExtension = DataEnrichmentExtension.builder()
                .id(getId())
                .name(getName())
                .type(IntegrationType.valueOf(getType()))
                .description(getDescription())
                .incoming(toURI(tenantDomainSupplier.get(), getIncomingAuthority()))
                .containerKey(getContainerKey())
                .parameters(getParameters())
                .active(isActive())
                .build();

        return dataEnrichmentExtension;
    }

    @Override
    public EnrichmentForm fillFrom(DataEnrichmentExtension model) {
        this.setId(model.getId());
        this.setName(model.getName());
        this.setType(model.getType().name());
        this.setDescription(model.getDescription());
        this.setIncomingAuthority(model.getIncoming().getPath().replaceAll("/",""));
        this.setContainerKey(model.getContainerKey());
        this.setParameters(model.getParameters());
        this.setActive(model.isActive());
        return this;
    }

    @Override
    public void setAdditionalSupplier(Supplier<String> supplier) {
        tenantDomainSupplier = supplier;
    }
}
