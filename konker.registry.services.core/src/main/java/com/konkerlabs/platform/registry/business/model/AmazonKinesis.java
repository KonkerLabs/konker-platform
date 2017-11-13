package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import lombok.Builder;
import lombok.Data;

import java.text.MessageFormat;
import java.util.Map;

@Data
@Builder
public class AmazonKinesis implements URIDealer {

	public enum Validations {
		AMAZON_KINESIS_INVALID_KEY("service.event_route.amazonkinesis.invalid_key"),
        AMAZON_KINESIS_INVALID_SECRET("service.event_route.amazonkinesis.invalid_secret"),
        AMAZON_KINESIS_INVALID_REGION("service.event_route.amazonkinesis.invalid_region"),
		AMAZON_KINESIS_NAME_IS_STREAM_NAME("service.event_route.amazonkinesis.invalid_stream_name");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}
	
	private Tenant tenant;
    private Application application;
    private String region;
    private String streamName;
    private String key;
    private String secret;

	public static final String URI_SCHEME = "amazonKinesis";

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
		return MessageFormat.format("{0}/{1}", region, streamName);
	}

	public void setValues(Map<String, String> values) {
        this.key = values.get("key");
        this.secret = values.get("secret");
        this.streamName = values.get("streamName");
	    this.region = values.get("region");
	}

}
