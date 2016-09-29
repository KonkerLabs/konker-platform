package com.konkerlabs.platform.registry.business.services.routes;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.SmsDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRouteServiceImpl implements EventRouteService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private RestDestinationRepository restRepository;
    @Autowired
    private SmsDestinationRepository smsDestinationRepository;

    @Override
    public ServiceResponse<EventRoute> save(Tenant tenant, EventRoute route) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        route.setId(null);
        route.setTenant(existingTenant);
        route.setGuid(UUID.randomUUID().toString());

        Optional<Map<String,Object[]>> validations = route.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<EventRoute>error()
                .withMessages(validations.get()).build();
        }

        if (Optional.ofNullable(eventRouteRepository.findByTenantIdAndRouteName(tenant.getId(),route.getName())).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.NAME_IN_USE.getCode(),null).build();

        fillRouteActorsDisplayName(tenant.getId(), route);
        
        EventRoute saved = eventRouteRepository.save(route);

        return ServiceResponseBuilder.<EventRoute>ok().withResult(saved).build();
    }

   

	@Override
    public ServiceResponse<EventRoute> update(Tenant tenant, String guid, EventRoute eventRoute) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(eventRoute).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent()) {
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode(),null)
                    .build();
        }

        EventRoute current = eventRouteRepository.findByTenantIdAndGuid(
            tenant.getId(),
            guid
        );

        if (!Optional.ofNullable(current).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode(),null)
                    .build();

        current.setActive(eventRoute.isActive());
        current.setDescription(eventRoute.getDescription());
        current.setFilteringExpression(eventRoute.getFilteringExpression());
        current.setIncoming(eventRoute.getIncoming());
        current.setName(eventRoute.getName());
        current.setOutgoing(eventRoute.getOutgoing());
        current.setTransformation(eventRoute.getTransformation());

        Optional<Map<String,Object[]>> validations = current.applyValidations();

        if (validations.isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessages(validations.get())
                    .build();

        if (Optional.ofNullable(eventRouteRepository.findByTenantIdAndRouteName(tenant.getId(),current.getName()))
                .filter(eventRoute1 -> !eventRoute1.getGuid().equals(current.getGuid()))
                .isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.NAME_IN_USE.getCode(),null)
                    .build();
        
        fillRouteActorsDisplayName(tenant.getId(), current);

        EventRoute saved = eventRouteRepository.save(current);

        return ServiceResponseBuilder.<EventRoute>ok().withResult(saved).build();
    }
	
	private void fillRouteActorsDisplayName(String tenantId, EventRoute route) {
		// setting incoming display name (incoming is always a device)
		Device incomingDevice = deviceRepository.findByTenantAndGuid(tenantId,
				route.getIncoming().getUri().getPath().replace("/", ""));
		if (Optional.ofNullable(incomingDevice).isPresent())
			route.getIncoming().setDisplayName(incomingDevice.getDeviceId());

		// setting outgoing display name
		switch (route.getOutgoing().getUri().getScheme()) {
		case DeviceURIDealer.DEVICE_URI_SCHEME:

			Device outgoingDevice = deviceRepository.findByTenantAndGuid(tenantId,
					route.getOutgoing().getUri().getPath().replace("/", ""));
			if (Optional.ofNullable(outgoingDevice).isPresent())
				route.getOutgoing().setDisplayName(outgoingDevice.getDeviceId());

			break;
		case SmsDestinationURIDealer.SMS_URI_SCHEME:

			SmsDestination outgoingSms = smsDestinationRepository.getByTenantAndGUID(tenantId,
					route.getOutgoing().getUri().getPath().replace("/", ""));
			if (Optional.ofNullable(outgoingSms).isPresent())
				route.getOutgoing().setDisplayName(outgoingSms.getName());

			break;
		case RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME:

			RestDestination outgoingRest = restRepository.getByTenantAndGUID(tenantId,
					route.getOutgoing().getUri().getPath().replace("/", ""));
			if (Optional.ofNullable(outgoingRest).isPresent())
				route.getOutgoing().setDisplayName(outgoingRest.getName());

			break;

		}

	}

    @Override
    public ServiceResponse<List<EventRoute>> getAll(Tenant tenant) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        return ServiceResponseBuilder.<List<EventRoute>>ok()
            .withResult(eventRouteRepository.findAllByTenant(existingTenant.getId()))
            .build();
    }

    @Override
    public ServiceResponse<EventRoute> getByGUID(Tenant tenant, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode(),null)
                    .build();

        EventRoute route = eventRouteRepository.findByTenantIdAndGuid(tenant.getId(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode(),null)
                    .build();

        return ServiceResponseBuilder.<EventRoute>ok().withResult(route)
                .build();
    }

    @Override
    public ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri) {
        if (!Optional.ofNullable(uri).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(Validations.EVENT_ROUTE_URI_NULL.getCode(),null)
                    .build();

        List<EventRoute> eventRoutes = eventRouteRepository.findByIncomingUri(uri);

        return ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(eventRoutes)
                .build();
    }

    @Override
    public ServiceResponse<EventRoute> remove(Tenant tenant, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode(),null)
                    .build();

        EventRoute route = eventRouteRepository.findByTenantIdAndGuid(existingTenant.getId(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode(),null)
                    .build();

        eventRouteRepository.delete(route);

        return ServiceResponseBuilder.<EventRoute>ok()
                .withResult(route)
                .build();
    }
}
