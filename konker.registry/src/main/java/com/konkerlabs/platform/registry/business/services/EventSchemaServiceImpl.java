package com.konkerlabs.platform.registry.business.services;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.BasicDBObject;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventSchemaServiceImpl implements EventSchemaService {

    private enum Type {
        INCOMING,
        OUTGOING;
    }

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /*private KonkerLogger LOG = KonkerLoggerFactory.getLogger(EventSchemaServiceImpl.class);*/
    private Logger LOG = LoggerFactory.getLogger(EventSchemaServiceImpl.class);


    @Override
    public ServiceResponse<EventSchema> appendIncomingSchema(Event event) {

        Optional<Validations> invalid = validateForSchemaAppending(event, Type.INCOMING);

        if (invalid.isPresent())
            return ServiceResponseBuilder.<EventSchema>error()
                .withMessage(invalid.get().getCode()).build();

        EventSchema toBeSaved = null;
        try {
            toBeSaved = prepareSchemaFor(event.getIncoming().getDeviceGuid(),event.getIncoming().getChannel(),event.getPayload());
        } catch (JsonProcessingException e) {
            return ServiceResponseBuilder.<EventSchema>error()
                    .withMessage(Validations.EVENT_INVALID_PAYLOAD.getCode()).build();
        }

        mongoTemplate.save(toBeSaved, EventSchemaService.INCOMING_COLLECTION_NAME);

        return ServiceResponseBuilder.<EventSchema>ok().withResult(toBeSaved).build();
    }

    @Override
    public ServiceResponse<EventSchema> appendOutgoingSchema(Event event) {
        return ServiceResponseBuilder.<EventSchema>ok().build();
    }

    private EventSchema prepareSchemaFor(String deviceId, String channel, String payload) throws JsonProcessingException {
        ServiceResponse<EventSchema> existing = findIncomingBy(deviceId,channel);

        Map<String,JsonParsingService.JsonPathData> data = jsonParsingService.toFlatMap(payload);

        EventSchema eventSchema = Optional.of(existing)
                .filter(eventSchemaServiceResponse -> eventSchemaServiceResponse.isOk())
                .map(eventSchemaServiceResponse -> eventSchemaServiceResponse.getResult())
                .orElseGet(() -> {
                    EventSchema schema = EventSchema.builder()
                            .deviceGuid(deviceId).channel(channel).build();
                    schema.setFields(new LinkedHashSet<>());
                    return schema;
                });

        data.forEach((path, pathData) -> eventSchema.upsertTypeFor(path,pathData));

        return eventSchema;
    }

    private Optional<Validations> validateForSchemaAppending(Event event, Type type) {
        if (!Optional.ofNullable(event).isPresent())
            return Optional.of(Validations.EVENT_NULL);

        switch (type) {
            case INCOMING: {
                if (!Optional.ofNullable(event.getIncoming()).isPresent())
                    return Optional.of(Validations.EVENT_INCOMING_NULL);
                if (!Optional.ofNullable(event.getIncoming().getDeviceGuid()).filter(s -> !s.isEmpty()).isPresent())
                    return Optional.of(Validations.EVENT_INCOMING_DEVICE_ID_NULL);
                if (!Optional.ofNullable(event.getIncoming().getChannel()).filter(s -> !s.isEmpty()).isPresent())
                    return Optional.of(Validations.EVENT_INCOMING_CHANNEL_NULL);
                break;
            }
            case OUTGOING: {
                if (!Optional.ofNullable(event.getOutgoing()).isPresent())
                    return Optional.of(Validations.EVENT_OUTGOING_NULL);
                if (!Optional.ofNullable(event.getOutgoing().getDeviceGuid()).filter(s -> !s.isEmpty()).isPresent())
                    return Optional.of(Validations.EVENT_OUTGOING_DEVICE_ID_NULL);
                if (!Optional.ofNullable(event.getOutgoing().getChannel()).filter(s -> !s.isEmpty()).isPresent())
                    return Optional.of(Validations.EVENT_OUTGOING_CHANNEL_NULL);
                break;
            }
            default: break;
        }


        return Optional.empty();
    }

    @Override
    public ServiceResponse<EventSchema> findIncomingBy(String deviceGuid, String channel) {
        EventSchema existing = mongoTemplate.findOne(
                Query.query(Criteria.where("deviceGuid")
                        .is(deviceGuid).andOperator(Criteria.where("channel").is(channel))),
                EventSchema.class,EventSchemaService.INCOMING_COLLECTION_NAME
        );

        return ServiceResponseBuilder.<EventSchema>ok()
            .withResult(existing).build();
    }

    @Override
    public ServiceResponse<EventSchema> findOutgoingBy(String deviceGuid, String channel) {
        return ServiceResponseBuilder.<EventSchema>ok().build();
    }

    @Override
    public ServiceResponse<List<String>> findKnownIncomingChannelsBy(Tenant tenant, String deviceGuid) {
        ServiceResponse<Device> deviceServiceResponse = deviceRegisterService.getByDeviceGuid(tenant,deviceGuid);

        if (deviceServiceResponse.isOk()) {
            return ServiceResponseBuilder.<List<String>>ok()
                .withResult(
                    mongoTemplate.getCollection(INCOMING_COLLECTION_NAME)
                            .distinct("channel",new BasicDBObject("deviceGuid",deviceGuid))
                ).build();
        } else {
            return ServiceResponseBuilder.<List<String>>error()
                    .withMessages(deviceServiceResponse.getResponseMessages()).build();
        }
    }

	@Override
	public ServiceResponse<List<String>> findKnownIncomingMetricsBy(Tenant tenant, String deviceGuid, String channel, JsonNodeType nodeType) {
    	
		ServiceResponse<EventSchema> metricsResponse = findIncomingBy(deviceGuid, channel);

		if (metricsResponse.isOk()) {
		
	    	List<String> listMetrics = metricsResponse.getResult()
					.getFields().stream()
					.filter(schemaField -> schemaField.getKnownTypes().contains(JsonNodeType.NUMBER))
					.map(m -> m.getPath()).collect(Collectors.toList());
	    	
	    	return ServiceResponseBuilder.<List<String>>ok()
	                .withResult(listMetrics).build();
	    	
		} else {

            return ServiceResponseBuilder.<List<String>>error()
                    .withMessages(metricsResponse.getResponseMessages()).build();
			
		}
    	
	}

	@Override
	public ServiceResponse<EventSchema> findLastIncomingBy(Tenant tenant, String deviceGuid, JsonNodeType nodeType) {

		EventSchema lastEvent = null;

		try {
			// List all last events
			List<Event> lastEvents = eventRepository.findIncomingBy(tenant, deviceGuid, null, null, null, false,
					1000);

			ObjectMapper mapper = new ObjectMapper();
			
			for (Event event : lastEvents) {
				if (event.getIncoming() == null) {
					continue;
				}

				ServiceResponse<EventSchema> schemaResponse = findIncomingBy(deviceGuid,
						event.getIncoming().getChannel());
				EventSchema schema = schemaResponse.getResult();
				if (schema == null) {
					continue;
				}

				for (SchemaField field : schema.getFields()) {
					if (field.getKnownTypes().contains(nodeType)) {
						try {
							// check if node exists
							JsonNode root = mapper.readTree(event.getPayload());
							if (root.path(field.getPath()) != null) {
								lastEvent = EventSchema.builder()
										.channel(schema.getChannel())
										.field(field)
										.build();

								break;
							}
						} catch (IOException e) {
							LOG.warn(e.getMessage());
						}
					}
				}

				// found?
				if (lastEvent != null) {
					break;
				}
			}

			return ServiceResponseBuilder.<EventSchema>ok().withResult(lastEvent).build();

		} catch (BusinessException e1) {

			return ServiceResponseBuilder.<EventSchema>error().withMessage(e1.getLocalizedMessage()).build();

		}

	}

}
