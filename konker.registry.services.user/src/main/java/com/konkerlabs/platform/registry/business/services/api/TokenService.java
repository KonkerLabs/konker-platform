package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;

import java.time.temporal.TemporalAmount;


public interface TokenService {
    enum Purpose {
        RESET_PASSWORD("service.token.purpose.resetPassword");

        private String name;

        Purpose(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }
    enum Validations {
        EXPIRED_TOKEN("service.token.validation.uuid.expired"),
        INVALID_TOKEN("service.token.validation.uuid.invalid");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
    ServiceResponse<Token> getToken(String token);
    ServiceResponse<String> generateToken(Purpose purpose, User user, TemporalAmount temporalAmount);
    ServiceResponse<Boolean> isValidToken(String token);
    ServiceResponse<Boolean> invalidateToken(String token);
}
