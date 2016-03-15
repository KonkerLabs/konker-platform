package com.konkerlabs.platform.registry.business.services.routes;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cglib.beans.BulkBeanException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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

        route.setTenant(tenant);

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

        //TODO Validate route's filter expression language.

        EventRoute saved = eventRouteRepository.save(route);

        return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.OK).result(saved).<EventRoute>build();
    }

    @Override
    public List<EventRoute> getAll(Tenant tenant) {
        return eventRouteRepository.findAllByTenant(tenant.getId());
    }

    @Override
    public ServiceResponse<EventRoute> getById(Tenant tenant, String id) {
        if (!Optional.ofNullable(id).isPresent())
            return invalidServiceResponse("Id cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<EventRoute>build();
        if (!Optional.ofNullable(tenantRepository.findByName(tenant.getName())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<EventRoute>build();

        EventRoute route = eventRouteRepository.findByTenantIdAndRouteName(tenant.getId(), id);

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

    private ServiceResponse.ServiceResponseBuilder invalidServiceResponse(String... errors) {
        ServiceResponse.ServiceResponseBuilder invalidBuilder = ServiceResponse.builder()
                .status(ServiceResponse.Status.ERROR);
        for (String error : errors)
            invalidBuilder.responseMessage(error);

        return invalidBuilder;
    }
}
