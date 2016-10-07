package com.konkerlabs.platform.registry.business.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.AbstractURLBlacklistValidation;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
public class DataEnrichmentExtensionServiceImpl extends AbstractURLBlacklistValidation
		implements DataEnrichmentExtensionService {

	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private DataEnrichmentExtensionRepository repository;

	// @Autowired
	// private MongoTemplate mongoTemplate;

	// private boolean isContainerKeyRegisteredFor(Tenant tenant, URI incoming,
	// String containerKey) {
	// return mongoTemplate.exists(
	// new Query(
	// Criteria.where("tenant.id").is(tenant.getId())
	// .andOperator(
	// Criteria.where("incoming").is(incoming),
	// Criteria.where("containerKey").is(containerKey)
	// )
	// ), DataEnrichmentExtension.class
	// );
	// }

	@Override
	public ServiceResponse<DataEnrichmentExtension> register(Tenant tenant, DataEnrichmentExtension dee) {
		if (!Optional.ofNullable(dee).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.RECORD_NULL.getCode()).build();

		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		if (Optional.ofNullable(repository.findByTenantIdAndName(existingTenant.getId(), dee.getName())).isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_NAME_UNIQUE.getCode()).build();
		}

		dee.setTenant(existingTenant);
		dee.setId(null);
		dee.setGuid(UUID.randomUUID().toString());

		Optional<Map<String, Object[]>> validations = dee.applyValidations();

		if (validations.isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error().withMessages(validations.get()).build();
		}

		if (!repository.findByTenantIdAndIncomingAndContainerKey(existingTenant.getId(), dee.getIncoming(),
				dee.getContainerKey()).isEmpty()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_CONTAINER_KEY_ALREADY_REGISTERED.getCode()).build();
		}

		Optional<Map<String, Object[]>> blacklistValidations = verifyIfUrlMatchesBlacklist(
				dee.getParameters().get("URL"));

		if (blacklistValidations.isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error().withMessages(blacklistValidations.get())
					.build();
		}

		fillDataEnrichmentExtensionDisplayName(tenant.getId(), dee);
		DataEnrichmentExtension saved = repository.save(dee);

		return ServiceResponseBuilder.<DataEnrichmentExtension> ok().withResult(saved).build();
	}

	@Override
	public ServiceResponse<DataEnrichmentExtension> update(Tenant tenant, String uuid, DataEnrichmentExtension dee) {
		if (!Optional.ofNullable(dee).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.RECORD_NULL.getCode()).build();

		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		if (!Optional.ofNullable(uuid).filter(id -> !id.isEmpty()).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_ID_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		DataEnrichmentExtension oldDee = repository.findByTenantIdAndGUID(existingTenant.getId(), uuid);

		if (!Optional.ofNullable(oldDee).isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_NOT_FOUND.getCode()).build();
		}

		dee.setTenant(existingTenant);

		Optional<Map<String, Object[]>> validations = dee.applyValidations();

		if (validations.isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error().withMessages(validations.get()).build();

		if (Optional.ofNullable(repository.findByTenantIdAndName(existingTenant.getId(), dee.getName()))
				.filter(found -> !found.getGuid().equals(oldDee.getGuid())).isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_NAME_UNIQUE.getCode()).build();
		}

		boolean isContainerKeyInUse = Optional
				.of(repository.findByTenantIdAndIncomingAndContainerKey(existingTenant.getId(), dee.getIncoming(),
						dee.getContainerKey()))
				.filter(dataEnrichmentExtensions -> !dataEnrichmentExtensions.isEmpty())
				.orElseGet(ArrayList<DataEnrichmentExtension>::new).stream()
				.anyMatch(currentDee -> !currentDee.getGuid().equals(oldDee.getGuid()));

		if (isContainerKeyInUse)
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_CONTAINER_KEY_ALREADY_REGISTERED.getCode()).build();

		dee.setId(oldDee.getId());
		dee.setGuid(oldDee.getGuid());

		Optional<Map<String, Object[]>> blacklistValidations = verifyIfUrlMatchesBlacklist(
				dee.getParameters().get("URL"));

		if (blacklistValidations.isPresent()) {
			return ServiceResponseBuilder.<DataEnrichmentExtension> error().withMessages(blacklistValidations.get())
					.build();
		}

		fillDataEnrichmentExtensionDisplayName(tenant.getId(), dee);
		DataEnrichmentExtension saved = repository.save(dee);

		return ServiceResponseBuilder.<DataEnrichmentExtension> ok().withResult(saved).build();
	}

	private void fillDataEnrichmentExtensionDisplayName(String tenantId, DataEnrichmentExtension dee) {
		// setting incoming display name (incoming is always a device)
		Device incomingDevice = deviceRepository.findByTenantAndGuid(tenantId,
				dee.getIncoming().getPath().replace("/", ""));
		if (Optional.ofNullable(incomingDevice).isPresent())
			dee.setIncomingDisplayName(incomingDevice.getDeviceId());

	}

	@Override
	public ServiceResponse<List<DataEnrichmentExtension>> getAll(Tenant tenant) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<List<DataEnrichmentExtension>> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<List<DataEnrichmentExtension>> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		List<DataEnrichmentExtension> l = repository.findAllByTenantId(existingTenant.getId());

		return ServiceResponseBuilder.<List<DataEnrichmentExtension>> ok().withResult(l).build();
	}

	@Override
	public ServiceResponse<DataEnrichmentExtension> getByGUID(Tenant tenant, String guid) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		if (!Optional.ofNullable(guid).filter(id -> !id.isEmpty()).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_ID_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		DataEnrichmentExtension dee = repository.findByTenantIdAndGUID(existingTenant.getId(), guid);

		if (!Optional.ofNullable(dee).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_NOT_FOUND.getCode()).build();

		return ServiceResponseBuilder.<DataEnrichmentExtension> ok().withResult(dee).build();
	}

	@Override
	public ServiceResponse<List<DataEnrichmentExtension>> getByTenantAndByIncomingURI(Tenant tenant, URI incomingUri) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<List<DataEnrichmentExtension>> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		if (!Optional.ofNullable(incomingUri).isPresent())
			return ServiceResponseBuilder.<List<DataEnrichmentExtension>> error()
					.withMessage(Validations.ENRICHMENT_INCOMING_URI_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<List<DataEnrichmentExtension>> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		List<DataEnrichmentExtension> result = repository.findByTenantIdAndIncoming(existingTenant.getId(),
				incomingUri);

		return ServiceResponseBuilder.<List<DataEnrichmentExtension>> ok().withResult(result).build();
	}

	@Override
	public ServiceResponse<DataEnrichmentExtension> remove(Tenant tenant, String guid) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_NULL.getCode()).build();

		if (!Optional.ofNullable(guid).filter(id -> !id.isEmpty()).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_ID_NULL.getCode()).build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(existingTenant).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		DataEnrichmentExtension dee = repository.findByTenantIdAndGUID(existingTenant.getId(), guid);

		if (!Optional.ofNullable(dee).isPresent())
			return ServiceResponseBuilder.<DataEnrichmentExtension> error()
					.withMessage(Validations.ENRICHMENT_NOT_FOUND.getCode()).build();

		repository.delete(dee);

		return ServiceResponseBuilder.<DataEnrichmentExtension> ok().withResult(dee).build();
	}
}
