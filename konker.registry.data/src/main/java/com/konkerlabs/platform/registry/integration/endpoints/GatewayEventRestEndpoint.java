package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@RestController
public class GatewayEventRestEndpoint {

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_WAITTIME("integration.rest.invalid.waitTime"),
        INVALID_CHANNEL_PATTERN("integration.rest.invalid.channel"),
    	DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
    	INVALID_REQUEST_ORIGIN("integration.rest.invalid_request_origin"),
        INVALID_HEADER_DEVICE_ID_FIELD("integration.rest.invalid.device_id_field"),
        INVALID_HEADER_DEVICE_CHANNEL_FIELD("integration.rest.invalid.device_channel_field");

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
    private DeviceRegisterService deviceRegisterService;
    private OAuthClientDetailsService oAuthClientDetailsService;

    @Autowired
    public GatewayEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService,
                                   DeviceRegisterService deviceRegisterService,
                                   OAuthClientDetailsService oAuthClientDetailsService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.deviceRegisterService = deviceRegisterService;
        this.oAuthClientDetailsService = oAuthClientDetailsService;
    }

    private EventResponse buildResponse(String message, Locale locale) {
        return EventResponse.builder()
                .code(message)
                .message(applicationContext.getMessage(message,null, locale)).build();
    }

    @RequestMapping(value = "gateway/pub", method = RequestMethod.POST)
    public ResponseEntity<EventResponse> onEvent(HttpServletRequest servletRequest,
    											 OAuth2Authentication oAuth2Authentication,
                                                 @RequestBody String body,
                                                 Locale locale) {
    	String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	OauthClientDetails clientDetails = oAuthClientDetailsService.loadClientByIdAsRoot(principal).getResult();
        Gateway gateway = clientDetails.getParentGateway();
        
        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

		if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
			return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        try {
        	deviceEventProcessor.process(gateway, body);
        } catch (BusinessException | JsonProcessingException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(
        		EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
        		.message(HttpStatus.OK.name()).build(),
        		HttpStatus.OK);
    }

    @RequestMapping(value = "gateway/data/pub", method = RequestMethod.POST)
    public ResponseEntity<EventResponse> onDataEvent(HttpServletRequest servletRequest,
                                                 OAuth2Authentication oAuth2Authentication,
                                                 @RequestBody String body,
                                                 Locale locale) {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OauthClientDetails clientDetails = oAuthClientDetailsService.loadClientByIdAsRoot(principal).getResult();
        Gateway gateway = clientDetails.getParentGateway();
        String deviceIdFieldName = servletRequest.getHeader("X-Konker-DeviceIdField");
        String deviceChannelFieldName = servletRequest.getHeader("X-Konker-DeviceChannelField");

        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

        if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        if (deviceIdFieldName == null) {
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_HEADER_DEVICE_ID_FIELD.getCode(),locale), HttpStatus.BAD_REQUEST);
        }

        if (deviceChannelFieldName == null) {
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_HEADER_DEVICE_CHANNEL_FIELD.getCode(),locale), HttpStatus.BAD_REQUEST);
        }

        try {
            deviceEventProcessor.process(gateway, body, deviceIdFieldName, deviceChannelFieldName);
        } catch (BusinessException | JsonProcessingException e) {
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
