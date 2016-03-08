package com.konkerlabs.platform.registry.business.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.integration.gateways.HttpEnrichmentGateway;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EnrichmentExecutorImpl implements EnrichmentExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentExecutorImpl.class);

    @Autowired
    private DataEnrichmentExtensionService dataEnrichmentExtensionService;
    @Autowired
    private HttpEnrichmentGateway httpEnrichmentGateway;
    @Autowired
    private ExpressionEvaluationService expressionEvaluationService;
    @Autowired
    private JsonParsingService jsonParsingService;

    @Override
    public ServiceResponse<Event> enrich(Event incomingEvent, Device device) {
        if (!Optional.ofNullable(incomingEvent).isPresent())
            return ServiceResponse.<Event>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .result(incomingEvent)
                    .responseMessage("Incoming event can not be null").<Event>build();

        if (!Optional.ofNullable(device).isPresent())
            return ServiceResponse.<Event>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .result(incomingEvent)
                    .responseMessage("Incoming device can not be null").<Event>build();

        ServiceResponse<List<DataEnrichmentExtension>> listServiceResponse = dataEnrichmentExtensionService.getByTenantAndByIncomingURI(device.getTenant(), device.toURI());

        if (listServiceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
            for (DataEnrichmentExtension dee : listServiceResponse.getResult()) {
                if (!dee.isActive())
                    continue;

                String url = dee.getParameters().get("URL");
                String user = dee.getParameters().get("User");
                String password = dee.getParameters().get("Password");

                try {
                    Map<String, Object> incomingPayloadMap = jsonParsingService.toMap(incomingEvent.getPayload());

                    URL finalUrl = new URL(expressionEvaluationService.evaluateTemplate(url, incomingPayloadMap));

                    String body = httpEnrichmentGateway.get(finalUrl.toURI(), user, password);

                    Map<String, Object> enrichmentResultMap = jsonParsingService.toMap(body);

                    Object containerKey = incomingPayloadMap.get(dee.getContainerKey());
                    if (!Optional.ofNullable(containerKey).isPresent()) {
                        LOGGER.warn(MessageFormat.format("There is no container key [{0}] for device {1} in incoming payload: [{2}]. It will be created.",
                                dee.getContainerKey(),
                                device.getName(),
                                containerKey));
                        containerKey = new String();
                        incomingPayloadMap.put(dee.getContainerKey(), containerKey);
                    }

                    if (!containerKey.toString().isEmpty()) {
                        LOGGER.warn(MessageFormat.format("Overwriting container key [{0}] for device {1}. Original state: [{2}], substituted by: [{3}]",
                                dee.getContainerKey(),
                                device.getName(),
                                containerKey,
                                body));
                    }

                    incomingPayloadMap.put(dee.getContainerKey(), enrichmentResultMap);

                    incomingEvent.setPayload(jsonParsingService.toJsonString(incomingPayloadMap));

                } catch (Exception e) {
                    return ServiceResponse.<Event>builder()
                            .status(ServiceResponse.Status.ERROR)
                            .result(incomingEvent)
                            .responseMessage(e.getMessage()).<Event>build();
                }
            }
        } else
            return ServiceResponse.<Event>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .result(incomingEvent)
                    .responseMessages(listServiceResponse.getResponseMessages()).<Event>build();

        return ServiceResponse.<Event>builder()
                .status(ServiceResponse.Status.OK)
                .result(incomingEvent)
                .<Event>build();
    }
}
