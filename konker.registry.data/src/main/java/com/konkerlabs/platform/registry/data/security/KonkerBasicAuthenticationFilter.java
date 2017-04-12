package com.konkerlabs.platform.registry.data.security;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;

public class KonkerBasicAuthenticationFilter extends BasicAuthenticationFilter {

	private Logger LOG = LoggerFactory.getLogger(KonkerBasicAuthenticationFilter.class);

    private DeviceRegisterService deviceRegisterService;

	public KonkerBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {
		super.onUnsuccessfulAuthentication(request, response, failed);

		String header = request.getHeader("Authorization");
		String[] tokens = extractAndDecodeHeader(header, request);

		String apiKey = tokens[0];
		// String pass = tokens[1];
		String uriApiKey = null;

		Device device = deviceRegisterService.findByApiKey(apiKey);

		if (!Optional.ofNullable(device).isPresent() &&
				request.getRequestURI().contains("pub/")) {
			String aux = request.getRequestURI().substring(request.getRequestURI().indexOf("pub/") + 4);
			uriApiKey = aux.substring(0, aux.indexOf("/"));
			device = deviceRegisterService.findByApiKey(uriApiKey);

		} else if (!Optional.ofNullable(device).isPresent() &&
				request.getRequestURI().contains("sub/")) {
			String aux = request.getRequestURI().substring(request.getRequestURI().indexOf("sub/") + 4);
			uriApiKey = aux.substring(0, aux.indexOf("/"));
			device = deviceRegisterService.findByApiKey(uriApiKey);
		}


		if (Optional.ofNullable(device).isPresent() &&
				!Optional.ofNullable(uriApiKey).isPresent()) {
			LOG.warn(MessageFormat.format("The password of device \"{0}\" is wrong.",
					apiKey),
        			device.getTenant().toURI(),
        			device.getTenant().getLogLevel(),
        			device);
		} else if (Optional.ofNullable(device).isPresent() &&
				Optional.ofNullable(uriApiKey).isPresent() &&
				!uriApiKey.equals(apiKey)) {
			LOG.warn(MessageFormat.format("The device \"{0}\" or password is invalid.",
					apiKey),
        			device.getTenant().toURI(),
        			device.getTenant().getLogLevel(),
        			device);
		} else {
			LOG.warn(
					MessageFormat.format("Device is not exists with this apiKey \"{0}\"",
        			apiKey));
		}
	}

	private String[] extractAndDecodeHeader(String header, HttpServletRequest request)
			throws IOException {

		byte[] base64Token = header.substring(6).getBytes("UTF-8");
		byte[] decoded;
		try {
			decoded = Base64.decode(base64Token);
		}
		catch (IllegalArgumentException e) {
			throw new BadCredentialsException(
					"Failed to decode basic authentication token");
		}

		String token = new String(decoded, getCredentialsCharset(request));

		int delim = token.indexOf(":");

		if (delim == -1) {
			throw new BadCredentialsException("Invalid basic authentication token");
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}

    public void setDeviceRegisterService(DeviceRegisterService deviceRegisterService) {
        this.deviceRegisterService = deviceRegisterService;
    }

}
