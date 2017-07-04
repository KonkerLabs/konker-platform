package com.konkerlabs.platform.registry.web.services.api;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

/**
 * Created by Felipe on 03/01/17.
 */
public interface CaptchaService {
    ServiceResponse<Boolean> validateCaptcha(String secret, String response, String host);
}