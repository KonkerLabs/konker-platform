package com.konkerlabs.platform.registry.api.web.controller;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.api.config.PubServerInternalConfig;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.EventVO;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/sendEvents")
@Api(tags = "events")
public class SendEventsRestController extends AbstractRestController implements InitializingBean {
	
	@Autowired
	private PubServerInternalConfig pubServerIntenalConfig;
	
	@Autowired
	private RestTemplate restTemplate;

    private Set<String> validationsCode = new HashSet<>();

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    @ApiOperation(
            value = "Send data events to devices belong this application",
            response = EventVO.class,
            produces = "application/json"
            )
    public EventVO send(
            @PathVariable(value = "application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody String jsonPayload) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        
        if (!Optional.ofNullable(tenant).isPresent())
	        throw new NotFoundResponseException(
	        		ServiceResponseBuilder.<String>error()
	        			.withMessage(TenantService.Validations.NO_EXIST_TENANT.getCode())
	        			.build());

	    if (!Optional.ofNullable(application).isPresent())
	    	throw new NotFoundResponseException(
	    			ServiceResponseBuilder.<String>error()
	    				.withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
	    				.build());
        
	    HttpEntity<String> request = new HttpEntity<String>(jsonPayload);
        String response = restTemplate
        		.postForObject(
        				MessageFormat.format(pubServerIntenalConfig.getUrl(), tenant.getDomainName(), application.getName()), 
        				request,
        				String.class);
        
        if (response.contains("200")) {
        	return new EventVO();
        } else {
        	throw new NotFoundResponseException(
	        		ServiceResponseBuilder.<String>error()
	        			.withMessage(EventRouteService.Validations.EVENT_ROUTE_NOT_FOUND.getCode())
	        			.build());
        }
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	for (DeviceEventService.Validations value : DeviceEventService.Validations.values()) {
    		validationsCode.add(value.getCode());
    	}

    	for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
    		validationsCode.add(value.getCode());
    	}
    }

}
