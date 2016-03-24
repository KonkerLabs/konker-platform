package com.konkerlabs.platform.registry.business.services.routes;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRouteServiceImpl implements EventRouteService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;

    @Override
    public ServiceResponse<EventRoute> save(Tenant tenant, EventRoute route) {
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(route).isPresent())
            return invalidServiceResponse("Record cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenantRepository.findOne(tenant.getId())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<EventRoute>build();

        route.setId(null);
        route.setTenant(tenant);
        route.setGuid(UUID.randomUUID().toString());

        List<String> validations = route.applyValidations();

        if (validations != null) {
            return invalidServiceResponse(validations.toArray(new String[validations.size()])).<EventRoute>build();
        }

//        String incomingChannel = route.getIncoming().getData().get("channel");
//        String outgoingChannel = route.getOutgoing().getData().get("channel");
//
//        if (incomingChannel != null && outgoingChannel != null && incomingChannel.equals(outgoingChannel)) {
//            return ServiceResponse.<EventRoute>builder()
//                    .responseMessages(Arrays.asList(new String[] { "Incoming and outgoing device channels cannot be the same" }))
//                    .status(ServiceResponse.Status.ERROR).<EventRoute>build();
//        }

        if (Optional.ofNullable(eventRouteRepository.findByTenantIdAndRouteName(tenant.getId(),route.getName())).isPresent())
            return invalidServiceResponse("Event route name is already in use").<EventRoute>build();

        EventRoute saved = eventRouteRepository.save(route);

        return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.OK).result(saved).<EventRoute>build();
    }

    @Override
    public ServiceResponse<EventRoute> update(Tenant tenant, String guid, EventRoute eventRoute) {
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenantRepository.findOne(tenant.getId())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<EventRoute>build();
        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return invalidServiceResponse("GUID cannot be null or empty").<EventRoute>build();
        if (!Optional.ofNullable(eventRoute).isPresent())
            return invalidServiceResponse("Event route record cannot be null").<EventRoute>build();

        EventRoute current = eventRouteRepository.findByTenantIdAndGuid(
            tenant.getId(),
            guid
        );

        if (!Optional.ofNullable(current).isPresent())
            return invalidServiceResponse("Event route GUID does not exists").<EventRoute>build();

        current.setActive(eventRoute.isActive());
        current.setDescription(eventRoute.getDescription());
        current.setFilteringExpression(eventRoute.getFilteringExpression());
        current.setIncoming(eventRoute.getIncoming());
        current.setName(eventRoute.getName());
        current.setOutgoing(eventRoute.getOutgoing());
        current.setTransformation(eventRoute.getTransformation());

        List<String> validations = current.applyValidations();

        if (Optional.ofNullable(validations).filter(strings -> !strings.isEmpty()).isPresent())
            return invalidServiceResponse(validations.toArray(new String[validations.size()])).<EventRoute>build();

        if (Optional.ofNullable(eventRouteRepository.findByTenantIdAndRouteName(tenant.getId(),current.getName())).isPresent())
            return invalidServiceResponse("Event route name is already in use").<EventRoute>build();

        EventRoute saved = eventRouteRepository.save(current);

        return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.OK).result(saved).<EventRoute>build();
    }

    @Override
    public ServiceResponse<List<EventRoute>> getAll(Tenant tenant) {
        return ServiceResponse.<List<EventRoute>>builder()
            .status(ServiceResponse.Status.OK)
            .result(eventRouteRepository.findAllByTenant(tenant.getId()))
            .<List<EventRoute>>build();
    }

    @Override
    public ServiceResponse<EventRoute> getByGUID(Tenant tenant, String guid) {
        if (!Optional.ofNullable(guid).isPresent())
            return invalidServiceResponse("Id cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenantRepository.findByName(tenant.getName())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<EventRoute>build();

        EventRoute route = eventRouteRepository.findByTenantIdAndGuid(tenant.getId(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return invalidServiceResponse("Event Route does not exist").<EventRoute>build();

        return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.OK).result(route)
                .<EventRoute>build();
    }

    @Override
    public ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri) {
        if (!Optional.ofNullable(uri).isPresent())
            return invalidServiceResponse("URI cannot be null").<List<EventRoute>>build();

        List<EventRoute> eventRoutes = eventRouteRepository.findByIncomingUri(uri);

        return ServiceResponse.<List<EventRoute>>builder()
                .status(ServiceResponse.Status.OK)
                .result(eventRoutes)
                .<List<EventRoute>>build();
    }

    @Override
    public ServiceResponse<EventRoute> remove(Tenant tenant, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenantRepository.findByName(tenant.getName())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<EventRoute>build();
        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return invalidServiceResponse("GUID cannot be null or empty").<EventRoute>build();

        EventRoute route = eventRouteRepository.findByTenantIdAndGuid(tenant.getId(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return invalidServiceResponse("Event Route does not exist").<EventRoute>build();

        eventRouteRepository.delete(route);

        return ServiceResponse.<EventRoute>builder()
                .status(ServiceResponse.Status.OK)
                .result(route)
                .<EventRoute>build();
    }

    private ServiceResponse.ServiceResponseBuilder invalidServiceResponse(String... errors) {
        ServiceResponse.ServiceResponseBuilder invalidBuilder = ServiceResponse.builder()
                .status(ServiceResponse.Status.ERROR);
        for (String error : errors)
            invalidBuilder.responseMessage(error);

        return invalidBuilder;
    }
}
