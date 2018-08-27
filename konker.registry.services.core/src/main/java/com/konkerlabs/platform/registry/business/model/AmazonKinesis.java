package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AmazonKinesis implements URIDealer {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public static final String SECRET_KEY = "secret";

    public enum Validations {
		AMAZON_KINESIS_INVALID_KEY("service.event_route.amazonkinesis.invalid_key"),
        AMAZON_KINESIS_INVALID_SECRET("service.event_route.amazonkinesis.invalid_secret"),
        AMAZON_KINESIS_INVALID_REGION("service.event_route.amazonkinesis.invalid_region"),
		AMAZON_KINESIS_INVALID_STREAM_NAME("service.event_route.amazonkinesis.invalid_stream_name"),
        AMAZON_KINESIS_INVALID_SHARD_COUNT("service.event_route.amazonkinesis.invalid_shard_count");

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

    private String key;
    private String secret;
    private String region;
    private String streamName;

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
        this.secret = values.get(SECRET_KEY);
        this.streamName = values.get("streamName");
	    this.region = values.get("region");
    }

    public Map<String,String> getValues() {
        Map<String, String> values = new HashMap<>();
        values.put("key", this.getKey());
        values.put(SECRET_KEY, this.getSecret());
        values.put("streamName", this.getStreamName());
        values.put("region", this.getRegion());

        return values;
    }

}
