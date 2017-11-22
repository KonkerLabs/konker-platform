package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.annotation.JsonView;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.JedisTaskService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.integration.serializers.EventJsonView;
import com.konkerlabs.platform.registry.integration.serializers.EventVO;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

@RestController
public class GatewayAuthEventRestEndpoint {

    @Autowired
    private OAuthClientDetailsService oAuthClientDetailsService;


    @RequestMapping(
            value = { "gateway/auth" },
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EventResponse> configEvent(HttpServletRequest servletRequest,
                                                 OAuth2Authentication oAuth2Authentication,
                                                 Locale locale) {

        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OauthClientDetails clientDetails = oAuthClientDetailsService.loadClientByIdAsRoot(principal).getResult();
        Gateway gateway = clientDetails.getParentGateway();

        return new ResponseEntity<EventResponse>(
                EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
                .message(gateway.getName()).build(),
                HttpStatus.OK);

    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }

}
