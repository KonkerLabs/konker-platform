package com.konkerlabs.platform.registry.integration.endpoints;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

import lombok.Builder;
import lombok.Data;

@RestController
@Profile("dataInternal")
public class ApplicationEventRestEndpoint {

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_WAITTIME("integration.rest.invalid.waitTime"),
        INVALID_CHANNEL_PATTERN("integration.rest.invalid.channel"),
    	DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
    	INVALID_REQUEST_ORIGIN("integration.rest.invalid_requrest_origin"),
    	INVALID_TENANT("integration.rest.invalid_tenant");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private ApplicationContext applicationContext;
    private DeviceEventProcessor deviceEventProcessor;
    private JsonParsingService jsonParsingService;
    private TenantService tenantService;
    private ApplicationService applicationService;

    @Autowired
    public ApplicationEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService,
                                   TenantService tenantService,
                                   ApplicationService applicationService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.tenantService = tenantService;
        this.applicationService = applicationService;
    }

    private EventResponse buildResponse(String message, Locale locale) {
        return EventResponse.builder()
                .code(message)
                .message(applicationContext.getMessage(message,null, locale)).build();
    }

    @RequestMapping(value = "{tenantDomain}/{applicationName}/pub", method = RequestMethod.POST)
    public ResponseEntity<EventResponse> onEvent(HttpServletRequest servletRequest,
									    		 @PathVariable("tenantDomain") String tenantDomain,
									             @PathVariable("applicationName") String applicationName,
                                                 @RequestBody String body,
                                                 Locale locale) {
    	
    	ServiceResponse<Tenant> serviceResponse = tenantService.findByDomainName(tenantDomain);
    	if (!serviceResponse.isOk()) {
    		return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_TENANT.getCode(), locale), HttpStatus.BAD_REQUEST);
    	}
    	
    	ServiceResponse<Application> serviceResponseApp = applicationService.getByApplicationName(serviceResponse.getResult(), applicationName);
    	if (!serviceResponseApp.isOk()) {
    		return new ResponseEntity<EventResponse>(
    				buildResponse(serviceResponseApp.getResponseMessages().keySet().iterator().next(), locale), 
    				HttpStatus.BAD_REQUEST);
    	}

    	if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

		if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
			return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        try {
        	deviceEventProcessor.process(serviceResponseApp.getResult(), body);
        } catch (BusinessException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(
        		EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
        		.message(HttpStatus.OK.name()).build(),
        		HttpStatus.OK);
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }
}
