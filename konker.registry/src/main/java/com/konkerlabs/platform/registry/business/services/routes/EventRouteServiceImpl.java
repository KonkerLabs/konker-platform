package com.konkerlabs.platform.registry.business.services.routes;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
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
        try {
            Optional.ofNullable(tenant)
                    .orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(route)
                    .orElseThrow(() -> new BusinessException("Record cannot be null"));
            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            route.setTenant(tenant);

            List<String> validations = route.applyValidations();

            if (validations != null) {
                return ServiceResponse.<EventRoute>builder()
                        .responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).<EventRoute>build();
            }

            Optional.ofNullable(eventRouteRepository.findByIncomingUri(route.getIncoming().getUri())).filter(l -> !l.isEmpty())
                    .orElseThrow(() -> new BusinessException("Incoming actor cannot be null"));

            Optional.ofNullable(eventRouteRepository.findByOutgoingUri(route.getOutgoing().getUri())).filter(l -> !l.isEmpty())
                    .orElseThrow(() -> new BusinessException("Outgoing actor cannot be null"));

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
        } catch (BusinessException be) {
            return ServiceResponse.<EventRoute>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<EventRoute>build();
        }
    }

    @Override
    public List<EventRoute> getAll(Tenant tenant) {
        return eventRouteRepository.findAllByTenant(tenant.getId());
    }

    @Override
    public ServiceResponse<EventRoute> getById(Tenant tenant, String id) {
        try {
            Optional.ofNullable(id).orElseThrow(() -> new BusinessException("Id cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            EventRoute route = ofNullable(eventRouteRepository.findByTenantIdAndRouteName(t.getId(), id))
                    .orElseThrow(() -> new BusinessException("Event Route does not exist"));

            return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.OK).result(route)
                    .<EventRoute>build();
        } catch (BusinessException be) {
            return ServiceResponse.<EventRoute>builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<EventRoute>build();
        }
    }

    @Override
    public ServiceResponse<List<EventRoute>> findByIncomingUri(URI uri) {
        try {
            Optional.ofNullable(uri).orElseThrow(() -> new BusinessException("URI cannot be null"));

            List<EventRoute> eventRoutes = Optional.ofNullable(eventRouteRepository.findByIncomingUri(uri))
                    .orElseThrow(() -> new BusinessException("Route actor from specified URI does not exist"));

            return ServiceResponse.<List<EventRoute>>builder()
                    .status(ServiceResponse.Status.OK)
                    .result(eventRoutes)
                    .<List<EventRoute>>build();

        } catch (BusinessException be) {
            return ServiceResponse.<List<EventRoute>>builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<List<EventRoute>>build();
        }

    }
}
