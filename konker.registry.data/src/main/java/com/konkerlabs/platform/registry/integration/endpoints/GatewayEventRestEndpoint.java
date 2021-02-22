package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.data.core.services.JedisTaskService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.GatewayEventProcessor;
import com.konkerlabs.platform.registry.integration.serializers.EventJsonView;
import com.konkerlabs.platform.registry.integration.serializers.EventVO;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.text.MessageFormat.format;

@RestController
public class GatewayEventRestEndpoint extends AbstractEventRestEndpoint {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    private GatewayEventProcessor gatewayEventProcessor;
    private JsonParsingService jsonParsingService;
    private DeviceRegisterService deviceRegisterService;
    private OAuthClientDetailsService oAuthClientDetailsService;
    private RestTemplate restTemplate;
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    public GatewayEventRestEndpoint(ApplicationContext applicationContext,
                                    GatewayEventProcessor gatewayEventProcessor,
                                    JsonParsingService jsonParsingService,
                                    DeviceRegisterService deviceRegisterService,
                                    OAuthClientDetailsService oAuthClientDetailsService,
                                    RestTemplate restTemplate,
                                    RabbitMQConfig rabbitMQConfig,
                                    DeviceEventService deviceEventService,
                                    JedisTaskService jedisTaskService,
                                    Executor executor) {

        super(applicationContext, deviceEventService, jedisTaskService, executor);

        this.gatewayEventProcessor = gatewayEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.deviceRegisterService = deviceRegisterService;
        this.oAuthClientDetailsService = oAuthClientDetailsService;
        this.restTemplate = restTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
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

        ResponseEntity<String> exchange = getHealthCheckRabbit();

        if (exchange.getStatusCode().equals(HttpStatus.OK)) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    gatewayEventProcessor.process(gateway, body);
                } catch (BusinessException | JsonProcessingException e) {
                    LOGGER.error("Error for processing Gateway data", e);
                }
                return "Processing";
            });

            return new ResponseEntity<EventResponse>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
                            .message(HttpStatus.OK.name()).build(),
                    HttpStatus.OK);

        } else {
            return new ResponseEntity<EventResponse>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .message(HttpStatus.SERVICE_UNAVAILABLE.name()).build(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

    }

    private ResponseEntity<String> getHealthCheckRabbit() {
        try {
            HttpHeaders headers = new HttpHeaders();
            String encodedCredentials = Base64Utils
                    .encodeToString(format("{0}:{1}", rabbitMQConfig.getApiUsername(), rabbitMQConfig.getApiPassword()).getBytes());
            headers.add("Authorization", format("Basic {0}", encodedCredentials));
            HttpEntity<String> entity = new HttpEntity(
                    null,
                    headers
            );

            LOGGER.info(format("Connecting to {0}:{1}", rabbitMQConfig.getApiHost(), rabbitMQConfig.getApiPort()));
            return restTemplate.exchange(
                    format("http://{0}:{1}/{2}", rabbitMQConfig.getApiHost(), rabbitMQConfig.getApiPort(), "api/healthchecks/node"),
                    HttpMethod.GET,
                    entity,
                    String.class);

        } catch (Exception e) {
            LOGGER.error("Error to connect in ", e);
            return new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
        }
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
        String deviceNameFieldName = servletRequest.getHeader("X-Konker-DeviceNameField");
        String deviceChannelFieldName = servletRequest.getHeader("X-Konker-DeviceChannelField");

        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

        if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        if (deviceIdFieldName == null) {
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_HEADER_DEVICE_ID_FIELD.getCode(),locale), HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> exchange = getHealthCheckRabbit();

        if (exchange.getStatusCode().equals(HttpStatus.OK)) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    gatewayEventProcessor.process(gateway, body, deviceIdFieldName, deviceNameFieldName, deviceChannelFieldName);
                } catch (BusinessException | JsonProcessingException e) {
                    LOGGER.error("Error for processing Gateway data", e);
                }
                return "Processing";
            });

            return new ResponseEntity<EventResponse>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
                            .message(HttpStatus.OK.name()).build(),
                    HttpStatus.OK);

        } else {
            return new ResponseEntity<EventResponse>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .message(HttpStatus.SERVICE_UNAVAILABLE.name()).build(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @RequestMapping(value = "gateway/data/sub/{deviceId}/{channel}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @JsonView(EventJsonView.class)
    public DeferredResult<List<EventVO>> subDataEvent(HttpServletRequest servletRequest,
                                                      HttpServletResponse httpResponse,
                                                      OAuth2Authentication oAuth2Authentication,
                                                      @PathVariable("deviceId") String deviceId,
                                                      @PathVariable(name="channel", required=false) String channel,
                                                      @RequestParam(name = "offset", required = false) Optional<Long> offset,
                                                      @RequestParam(name = "waitTime", required = false) Optional<Long> waitTime,
                                                      Locale locale) {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OauthClientDetails clientDetails = oAuthClientDetailsService.loadClientByIdAsRoot(principal).getResult();
        Gateway gateway = clientDetails.getParentGateway();
        DeferredResult<List<EventVO>> deferredResult = new DeferredResult<>(waitTime.orElse(new Long("0")), Collections.emptyList());

        ServiceResponse<Device> response = deviceRegisterService.findByDeviceId(gateway.getTenant(),
                gateway.getApplication(),
                deviceId);

        if (!response.isOk()) {
            deferredResult.setErrorResult(
                    applicationContext.getMessage(
                            DeviceRegisterService.Validations.DEVICE_ID_DOES_NOT_EXIST.getCode(),
                            null,
                            locale));
            httpResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return deferredResult;
        }

        Device device = response.getResult();

        if (!LocationTreeUtils.isSublocationOf(gateway.getLocation(), device.getLocation())) {
            deferredResult.setErrorResult(
                    applicationContext.getMessage(
                            DeviceRegisterService.Validations.DEVICE_LOCATION_IS_NOT_CHILD.getCode(),
                            null,
                            locale));
            httpResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return deferredResult;
        }

        return subscribeForEvents(httpResponse,
                deferredResult,
                device,
                channel,
                waitTime,
                offset,
                locale,
                true);
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }
}
