package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Data
@EqualsAndHashCode(exclude={"tenantDomainSupplier"})
public class EnrichmentForm implements ModelBuilder<DataEnrichmentExtension, EnrichmentForm, String>, DeviceURIDealer {

    private String id;
    private String name;
    private String type;
    private String description;
    private String incomingAuthority;
    private String containerKey;
    //FIXME Retrieve parameters associated with enrichment type from database?
    private Map<String, String> parameters = new LinkedHashMap<String, String>(){{put("URL", "");put("User", "");put("Password", "");}};
    private boolean active;
    private Supplier<String> tenantDomainSupplier;

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
                .type(DataEnrichmentExtension.EnrichmentType.valueOf(getType()))
                .description(getDescription())
                .incoming(toDeviceRuleURI(tenantDomainSupplier.get(), getIncomingAuthority()))
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
