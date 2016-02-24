package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DataEnrichmentExtensionService {
    ServiceResponse<DataEnrichmentExtension> register(Tenant tenant, DataEnrichmentExtension dee);

    ServiceResponse<DataEnrichmentExtension> save(Tenant tenant, DataEnrichmentExtension dee);

    ServiceResponse<List<DataEnrichmentExtension>> getAll(Tenant tenant);

    /**
     * Find a previously registered {@link DataEnrichmentExtension} by
     * {@link Tenant} and name.
     * 
     * @param tenant
     *            The tenant
     * @param name
     *            The name of the registered item
     * @return a {@link ServiceResponse} containing the item, if it exists. If
     *         it does not exist, return a {@link ServiceResponse} with an error
     *         message
     */
    ServiceResponse<DataEnrichmentExtension> findByName(Tenant tenant, String name);
}
