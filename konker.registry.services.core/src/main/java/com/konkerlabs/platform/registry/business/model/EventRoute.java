package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.api.Validatable;
import lombok.Builder;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Document(collection = "eventRoutes")
@Data
@Builder
public class EventRoute implements URIDealer, Validatable {

    public static final String DEVICE_MQTT_CHANNEL = "channel";

    public enum Validations {
        NAME_NULL("model.event_route.name.not_null"),
        INCOMING_ACTOR_NULL("model.event_route.incoming_actor.not_null"),
        INCOMING_ACTOR_URI_NULL("model.event_route.incoming_actor_uri.not_null"),
        INCOMING_ACTOR_URI_MUST_BE_A_DEVICE("model.event_route.incoming_uri.must_be_a_device"),
        INCOMING_ACTOR_CHANNEL_NULL("model.event_route.incoming_actor.channel.not_null"),
        INCOMING_ACTOR_CHANNEL_INVALID_NAME("model.event_route.incoming_actor.channel.invalid_name"),
        OUTGOING_ACTOR_NULL("model.event_route.outgoing_actor.not_null"),
        OUTGOING_ACTOR_URI_NULL("model.event_route.outgoing_actor_uri.not_null"),
        OUTGOING_ACTOR_URI_MUST_BE_A_DEVICE("model.event_route.outgoing_uri.must_be_a_device"),
        OUTGOING_ACTOR_CHANNEL_NULL("model.event_route.outgoing_actor.channel.not_null"),
        OUTGOING_ACTOR_CHANNEL_INVALID_NAME("model.event_route.outgoing_actor.channel.invalid_name"),
        GUID_NULL("model.event_route.guid.not_null"),
        INCOMING_OUTGOING_DEVICE_CHANNELS_SAME("model.event_route.incoming_outgoing_devices_channels.same");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private String name;
    private String description;
    private RouteActor incoming;
    private RouteActor outgoing;
    private String filteringExpression;
    @DBRef
    private Transformation transformation;
    private String guid;
    private boolean active;

    public static final String URI_SCHEME = "eventroute";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return getTenant() != null ? getTenant().getDomainName() : null;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public Optional<Map<String,Object[]>> applyValidations() {
        Map<String,Object[]> validations = new HashMap<>();

        if (getTenant() == null)
            validations.put(CommonValidations.TENANT_NULL.getCode(),null);
        if (getName() == null || getName().isEmpty())
            validations.put(Validations.NAME_NULL.getCode(),null);
        if (getIncoming() == null)
            validations.put(Validations.INCOMING_ACTOR_NULL.getCode(),null);
        if (getIncoming() != null && getIncoming().getUri() == null)
            validations.put(Validations.INCOMING_ACTOR_URI_NULL.getCode(),null);
        if (getIncoming() != null && getIncoming().getUri() != null && getIncoming().getUri().toString().isEmpty())
            validations.put(Validations.INCOMING_ACTOR_URI_NULL.getCode(),null);
        if (getOutgoing() == null)
            validations.put(Validations.OUTGOING_ACTOR_NULL.getCode(),null);
        if (getOutgoing() != null && getOutgoing().getUri() == null)
            validations.put(Validations.OUTGOING_ACTOR_URI_NULL.getCode(),null);
        if (getOutgoing() != null && getOutgoing().getUri() != null && getOutgoing().getUri().toString().isEmpty())
            validations.put(Validations.OUTGOING_ACTOR_URI_NULL.getCode(),null);
        if (!Optional.ofNullable(getGuid()).filter(s -> !s.isEmpty()).isPresent())
            validations.put(Validations.GUID_NULL.getCode(),null);

        if (Device.URI_SCHEME.equals(Optional.ofNullable(getIncoming()).map(RouteActor::getUri).map(URI::getScheme).orElse(""))) {
            applyDeviceIncomingValidations(validations);
        }
        if (Device.URI_SCHEME.equals(Optional.ofNullable(getOutgoing()).map(RouteActor::getUri).map(URI::getScheme).orElse(""))) {
            applyDeviceOutgoingValidations(validations);
        }
        if (DeviceModelLocation.URI_SCHEME.equals(Optional.ofNullable(getIncoming()).map(RouteActor::getUri).map(URI::getScheme).orElse(""))) {
            applyModelLocationIncomingValidations(validations);
        }
        if (DeviceModelLocation.URI_SCHEME.equals(Optional.ofNullable(getOutgoing()).map(RouteActor::getUri).map(URI::getScheme).orElse(""))) {
            applyModelLocationOutgoingValidations(validations);
        }
		if (compareAndCheckChannelsAreEqual(getIncoming(), getOutgoing())) {
            validations.put(Validations.INCOMING_OUTGOING_DEVICE_CHANNELS_SAME.getCode(), null);
        }

 		return Optional.of(validations).filter(stringMap -> !stringMap.isEmpty());
 	}

    private void applyDeviceIncomingValidations(Map<String,Object[]> validations) {
        Map<String, String> data = getIncoming().getData();
        if (!Device.URI_SCHEME.equals(getIncoming().getUri().getScheme())) {
            validations.put(Validations.INCOMING_ACTOR_URI_MUST_BE_A_DEVICE.getCode(),null);
        } else {
            String channelName = data.get(DEVICE_MQTT_CHANNEL);
			if (StringUtils.isBlank(channelName)) {
                validations.put(Validations.INCOMING_ACTOR_CHANNEL_NULL.getCode(),null);
			} else if (!isValidChannelName(channelName)) {
				validations.put(Validations.INCOMING_ACTOR_CHANNEL_INVALID_NAME.getCode(),null);
			}
        }

    }

    private boolean compareAndCheckChannelsAreEqual(RouteActor incoming, RouteActor outgoing) {

        // check if are elegible to validate channels
        if (incoming == null || outgoing == null) {
            return false;
        } else {
            if (!incoming.isDevice() && !incoming.isModelLocation()) {
                return false;
            }
            if (!outgoing.isDevice() && !outgoing.isModelLocation()) {
                return false;
            }
            // both can't have the same pair (device - channel)
            if (!incoming.getUri().equals(outgoing.getUri())) {
                return false;
            }
        }

        String incomingChannel = incoming.getData().get("channel");
        String outgoingChannel = outgoing.getData().get("channel");

        if (StringUtils.isBlank(incomingChannel) || StringUtils.isBlank(outgoingChannel)) {
            return false;
        }

        return incomingChannel.equals(outgoingChannel);

    }

    private boolean isValidChannelName(String channelName) {
    	return channelName.matches("[a-zA-Z0-9_-]+");
	}

	public void applyDeviceOutgoingValidations(Map<String,Object[]> validations) {
        Map<String, String> data = getOutgoing().getData();
        if (!Device.URI_SCHEME.equals(getOutgoing().getUri().getScheme())) {
            validations.put(Validations.OUTGOING_ACTOR_URI_MUST_BE_A_DEVICE.getCode(),null);
        } else {
            String channelName = data.get(DEVICE_MQTT_CHANNEL);
			if (StringUtils.isBlank(channelName)) {
                validations.put(Validations.OUTGOING_ACTOR_CHANNEL_NULL.getCode(),null);
			} else if (!isValidChannelName(channelName)) {
				validations.put(Validations.OUTGOING_ACTOR_CHANNEL_INVALID_NAME.getCode(),null);
			}
        }
    }

    private void applyModelLocationIncomingValidations(Map<String,Object[]> validations) {
        Map<String, String> data = getIncoming().getData();
        if (getIncoming().isModelLocation()) {
            String channelName = data.get(DEVICE_MQTT_CHANNEL);
            if (StringUtils.isBlank(channelName)) {
                validations.put(Validations.INCOMING_ACTOR_CHANNEL_NULL.getCode(),null);
            } else if (!isValidChannelName(channelName)) {
                validations.put(Validations.INCOMING_ACTOR_CHANNEL_INVALID_NAME.getCode(),null);
            }
        }

    }

    private void applyModelLocationOutgoingValidations(Map<String,Object[]> validations) {
        Map<String, String> data = getOutgoing().getData();
        if (getOutgoing().isModelLocation()) {
            String channelName = data.get(DEVICE_MQTT_CHANNEL);
            if (StringUtils.isBlank(channelName)) {
                validations.put(Validations.OUTGOING_ACTOR_CHANNEL_NULL.getCode(),null);
            } else if (!isValidChannelName(channelName)) {
                validations.put(Validations.OUTGOING_ACTOR_CHANNEL_INVALID_NAME.getCode(),null);
            }
        }

    }

    @Data
	@Builder
	public static class RouteActor {
		private URI uri;
		private String displayName;
		private Map<String, String> data = new HashMap<>();

		public boolean isDevice() {
			return Device.URI_SCHEME.equals(Optional.ofNullable(getUri()).map(URI::getScheme).orElse(""));
		}

	    public boolean isRestDestination() {
	        return RestDestination.URI_SCHEME.equals(Optional.ofNullable(getUri()).map(URI::getScheme).orElse(""));
	    }

        public boolean isModelLocation() {
            return DeviceModelLocation.URI_SCHEME.equals(Optional.ofNullable(getUri()).map(URI::getScheme).orElse(""));
        }
	}


}
