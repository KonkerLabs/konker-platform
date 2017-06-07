package com.konkerlabs.platform.registry.business.model;

import java.text.MessageFormat;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceModelLocation implements URIDealer {

	private Tenant tenant;
    private Application application;
    private DeviceModel deviceModel;
    private Location location;

	public static final String URI_SCHEME = "modelLocation";

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
		return MessageFormat.format("{0}/{1}", deviceModel.getGuid(), location.getGuid());
	}

}
