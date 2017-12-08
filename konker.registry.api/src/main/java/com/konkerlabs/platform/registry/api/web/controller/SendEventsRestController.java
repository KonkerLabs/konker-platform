package com.konkerlabs.platform.registry.api.web.controller;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.EventVO;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/sendEvents")
@Api(tags = "events")
public class SendEventsRestController extends AbstractRestController implements InitializingBean {

    private Set<String> validationsCode = new HashSet<>();

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    @ApiOperation(
            value = "Send data events to devices belong this application",
            response = EventVO.class,
            produces = "application/json"
            )
    public String send(
            @PathVariable(value = "application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody String jsonPayload) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        
        if (!Optional.ofNullable(tenant).isPresent())
	        throw new NotFoundResponseException(user, null);

	    if (!Optional.ofNullable(application).isPresent())
	    	throw new NotFoundResponseException(user, null);
        
	    HttpEntity<String> request = new HttpEntity<String>(jsonPayload);
	    RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate
        		.postForObject(
        				MessageFormat.format("http://localhost:8082/registry-data/{0}/{1}/pub", tenant.getDomainName(), application.getName()), 
        				request,
        				String.class);
        
        return response;
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
