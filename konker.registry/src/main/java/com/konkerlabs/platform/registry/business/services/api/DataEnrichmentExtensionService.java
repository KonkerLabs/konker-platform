package com.konkerlabs.platform.registry.business.services.api;

import java.net.URI;
import java.util.List;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface DataEnrichmentExtensionService {

    /**
     * Creates a {@link DataEnrichmentExtension} and associates it with a
     * {@link Tenant}. This must be a new {@link DataEnrichmentExtension}. If
     * the name is already associated with the {@link Tenant}, an ERROR response
     * will be returned.
     * 
     * 
     * @param tenant
     *            The tenant
     * @param dee
     *            The {@link DataEnrichmentExtension} that mst be registered.
     * @return a {@link ServiceResponse} containing the saved
     *         {@link DataEnrichmentExtension} (with the generated ID) and
     *         status code OK. If the {@link Tenant} does not exist or the name
     *         of {@link DataEnrichmentExtension} already exists in this tenant,
     *         returns a {@link ServiceResponse} with an error message, status
     *         code ERROR and null result
     */
    ServiceResponse<DataEnrichmentExtension> register(Tenant tenant, DataEnrichmentExtension dee);

    ServiceResponse<DataEnrichmentExtension> update(Tenant tenant, String uuid, DataEnrichmentExtension dee);

    /**
     * Gets all the {@link DataEnrichmentExtension} associated with a
     * {@link Tenant}
     * 
     * @param tenant
     *            The tenant
     * @return a {@link ServiceResponse} containing List of the items and status
     *         code OK. If the {@link Tenant} does not exist, returns a
     *         {@link ServiceResponse} with an error message and status code
     *         ERROR and null result
     */
    ServiceResponse<List<DataEnrichmentExtension>> getAll(Tenant tenant);

    /**
     * Gets a previously registered {@link DataEnrichmentExtension} by
     * {@link Tenant} and name.
     * 
     * @param tenant
     *            The tenant
     * @param guid
     *            The name of the registered item
     * @return a {@link ServiceResponse} containing the item, if it exists and
     *         status code OK. If it does not exist, returns a
     *         {@link ServiceResponse} with an error message and status code
     *         ERROR
     */
    ServiceResponse<DataEnrichmentExtension> getByGUID(Tenant tenant, String guid);

    /**
     * Gets a previously registered {@link DataEnrichmentExtension} by
     * {@link Tenant} and incomingUri.
     *
     * @param tenant
     *            The tenant
     * @param incomingUri
     *            The incoming URI of the registered item
     * @return a {@link ServiceResponse} containing the item, if it exists and
     *         status code OK. If it does not exist, returns a
     *         {@link ServiceResponse} with an error message and status code
     *         ERROR
     */
    ServiceResponse<List<DataEnrichmentExtension>> getByTenantAndByIncomingURI(Tenant tenant, URI incomingUri);
}
