package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.net.URI;
import java.util.List;

public interface DataEnrichmentExtensionService {

    enum Validations {
        ENRICHMENT_ID_NULL("service.enrichment.id.not_null"),
        ENRICHMENT_INCOMING_URI_NULL("service.enrichment.incoming_uri.not_null"),
        ENRICHMENT_NAME_UNIQUE("service.enrichment.name.in_use"),
        ENRICHMENT_CONTAINER_KEY_ALREADY_REGISTERED("service.enrichment.container_key.already_registered"),
        ENRICHMENT_DOES_NOT_EXIST("service.enrichment.not_found");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

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
    NewServiceResponse<DataEnrichmentExtension> register(Tenant tenant, DataEnrichmentExtension dee);

    NewServiceResponse<DataEnrichmentExtension> update(Tenant tenant, String uuid, DataEnrichmentExtension dee);

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
    NewServiceResponse<List<DataEnrichmentExtension>> getAll(Tenant tenant);

    /**
     * Gets a previously registered {@link DataEnrichmentExtension} by
     * {@link Tenant} and guid.
     * 
     * @param tenant
     *            The tenant
     * @param guid
     *            The guid of the registered item
     * @return a {@link ServiceResponse} containing the item, if it exists and
     *         status code OK. If it does not exist, returns a
     *         {@link ServiceResponse} with an error message and status code
     *         ERROR
     */
    NewServiceResponse<DataEnrichmentExtension> getByGUID(Tenant tenant, String guid);

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
    NewServiceResponse<List<DataEnrichmentExtension>> getByTenantAndByIncomingURI(Tenant tenant, URI incomingUri);

    /**
     * Removes a previously registered {@link DataEnrichmentExtension} by
     * {@link Tenant} and guid.
     *
     * @param tenant
     *            The tenant
     * @param guid
     *            The guid of the registered item
     * @return a {@link ServiceResponse} containing the item, if it was removed
     *         successfully and status code OK. If it was not removed, returns a
     *         {@link ServiceResponse} with an error message and status code
     *         ERROR
     */
    NewServiceResponse<DataEnrichmentExtension> remove(Tenant tenant, String guid);
}
