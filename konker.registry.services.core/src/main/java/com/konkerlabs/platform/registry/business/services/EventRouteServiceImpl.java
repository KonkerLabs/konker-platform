package com.konkerlabs.platform.registry.business.services;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.SmsDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRouteServiceImpl implements EventRouteService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private RestDestinationRepository restRepository;
    @Autowired
    private SmsDestinationRepository smsDestinationRepository;

    @Override
    public ServiceResponse<EventRoute> save(Tenant tenant, Application application, EventRoute route) {
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

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        route.setId(null);
        route.setTenant(existingTenant);
        route.setApplication(existingApplication);
        route.setGuid(UUID.randomUUID().toString());

        Optional<Map<String,Object[]>> validations = route.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<EventRoute>error()
                .withMessages(validations.get()).build();
        }

        if (Optional.ofNullable(eventRouteRepository.findByRouteName(tenant.getId(),
                                                                     application.getName(),
                                                                     route.getName())).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.NAME_IN_USE.getCode()).build();

        fillRouteActorsDisplayName(tenant.getId(), route);

        EventRoute saved = eventRouteRepository.save(route);

        LOGGER.info("Route created. Name: {}", route.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<EventRoute>ok().withResult(saved).build();
    }



	@Override
    public ServiceResponse<EventRoute> update(Tenant tenant, Application application, String guid, EventRoute eventRoute) {
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

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent()) {
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();
        }

        EventRoute current = eventRouteRepository.findByGuid(
            tenant.getId(),
            application.getName(),
            guid
        );

        if (!Optional.ofNullable(current).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode())
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

        if (Optional.ofNullable(eventRouteRepository.findByRouteName(tenant.getId(),
                                                                     application.getName(),
                                                                     current.getName()))
                .filter(eventRoute1 -> !eventRoute1.getGuid().equals(current.getGuid()))
                .isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.NAME_IN_USE.getCode())
                    .build();

        fillRouteActorsDisplayName(tenant.getId(), current);

        EventRoute saved = eventRouteRepository.save(current);

        LOGGER.info("Route updated. Name: {}", saved.getName(), tenant.toURI(), tenant.getLogLevel());

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
    public ServiceResponse<List<EventRoute>> getAll(Tenant tenant, Application application) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        return ServiceResponseBuilder.<List<EventRoute>>ok()
            .withResult(eventRouteRepository.findAll(existingTenant.getId(), existingApplication.getName()))
            .build();
    }

    @Override
    public ServiceResponse<EventRoute> getByGUID(Tenant tenant, Application application, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();

        EventRoute route = eventRouteRepository.findByGuid(tenant.getId(), application.getName(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode())
                    .build();

        return ServiceResponseBuilder.<EventRoute>ok().withResult(route)
                .build();
    }

    @Override
    public ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri) {
        if (!Optional.ofNullable(uri).isPresent())
            return ServiceResponseBuilder.<List<EventRoute>>error()
                    .withMessage(Validations.EVENT_ROUTE_URI_NULL.getCode())
                    .build();

        List<EventRoute> eventRoutes = eventRouteRepository.findByIncomingUri(uri);

        return ServiceResponseBuilder.<List<EventRoute>>ok()
                .withResult(eventRoutes)
                .build();
    }

    @Override
    public ServiceResponse<EventRoute> remove(Tenant tenant, Application application, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(CommonValidations.APPLICATION_NULL.getCode()).build();

        EventRoute route = eventRouteRepository.findByGuid(existingTenant.getId(), application.getName(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<EventRoute>error()
                    .withMessage(Validations.EVENT_ROUTE_NOT_FOUND.getCode())
                    .build();

        eventRouteRepository.delete(route);

        LOGGER.info("Route removed. Name: {}", route.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<EventRoute>ok()
                .withResult(route)
                .build();
    }
}
