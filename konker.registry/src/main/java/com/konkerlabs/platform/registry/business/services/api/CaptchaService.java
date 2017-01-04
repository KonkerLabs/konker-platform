package com.konkerlabs.platform.registry.business.services.api;

import java.util.Map;

/**
 * Created by Felipe on 03/01/17.
 */
public interface CaptchaService {
    ServiceResponse<Map<String, Object>> validateCaptcha(String secret, String response, String host);
}