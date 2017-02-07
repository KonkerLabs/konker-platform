package com.konkerlabs.platform.registry.business.services.api;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface EventSchemaService {

    enum Validations {
        DEVICE_NULL("service.event_schema.device.not_null"),
        DEVICE_DOES_NOT_EXIST("service.event_schema.device.not_found"),
        EVENT_NULL("service.event_schema.event.not_null"),
        EVENT_INVALID_PAYLOAD("service.event_schema.event.invalid_payload"),
        EVENT_INCOMING_NULL("service.event_schema.event_incoming.not_null"),
        EVENT_INCOMING_DEVICE_ID_NULL("service.event_schema.event_incoming.device_id.not_null"),
        EVENT_INCOMING_CHANNEL_NULL("service.event_schema.event_incoming.channel.not_null"),
        EVENT_OUTGOING_NULL("service.event_schema.event_outgoing.not_null"),
        EVENT_OUTGOING_DEVICE_ID_NULL("service.event_schema.event_outgoing.device_id.not_null"),
        EVENT_OUTGOING_CHANNEL_NULL("service.event_schema.event_outgoing.channel.not_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    String INCOMING_COLLECTION_NAME = "incomingEventSchema";
    String OUTGOING_COLLECTION_NAME = "outgoingEventSchema";

    ServiceResponse<EventSchema> appendIncomingSchema(Event event);
    ServiceResponse<EventSchema> appendOutgoingSchema(Event event);
    ServiceResponse<EventSchema> findIncomingBy(Tenant tenant, String deviceGuid, String channel);
	ServiceResponse<EventSchema> findIncomingBy(Tenant tenant, String deviceGuid);
    ServiceResponse<EventSchema> findOutgoingBy(Tenant tenant, String deviceGuid, String channel);
    ServiceResponse<List<String>> findKnownIncomingChannelsBy(Tenant tenant, String deviceGuid);
	ServiceResponse<List<String>> findKnownIncomingMetricsBy(Tenant tenant, String deviceGuid, String channel, JsonNodeType nodeType);
	ServiceResponse<List<String>> findKnownIncomingMetricsBy(Tenant tenant, String deviceGuid, JsonNodeType nodeType);
	ServiceResponse<EventSchema> findLastIncomingBy(Tenant tenant, String deviceGuid, JsonNodeType nodeType);

}
