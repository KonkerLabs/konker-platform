package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.TokenRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Felipe on 27/12/16.
 */
@Service
public class TokenServiceImpl implements TokenService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
	
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    public TokenServiceImpl() {
    }

    @Override
    public ServiceResponse<Token> getToken(String token) {
        Token result = null;
        if (token != null) {
            result = tokenRepository.findOne(token);
            if (result != null) {
                return ServiceResponseBuilder.<Token>ok().withResult(result).build();
            } else {
                return ServiceResponseBuilder.<Token>error()
                        .withMessage(Validations.INVALID_TOKEN.getCode())
                        .withResult(result).build();
            }
        } else {
            return ServiceResponseBuilder.<Token>error()
                    .withMessage(Validations.INVALID_TOKEN.getCode())
                    .withResult(result).build();
        }
    }

    @Override
    public ServiceResponse<String> generateToken(Purpose purpose, User user, TemporalAmount temporalAmount) {
        Token token;
        if (user != null && purpose != null && temporalAmount != null &&
        		userRepository.findOne(user.getEmail()) != null) {
        	String userEmail = user.getEmail();
        	token = tokenRepository.findByUserEmail(userEmail, purpose.getName());
        	
        	if (Optional.ofNullable(token).isPresent()) {
        		tokenRepository.delete(token);
        	}
        	
        	token = new Token();
        	UUID uuid = UUID.randomUUID();
        	token.setToken(uuid.toString());

        	Instant creationInstant = Instant.now();
        	token.setCreationDateTime(creationInstant);
        	token.setExpirationDateTime(creationInstant.plus(temporalAmount));
        	token.setIsExpired(false);
        	token.setUserEmail(userEmail);
        	token.setPurpose(purpose.getName());
        	tokenRepository.save(token);

        	return ServiceResponseBuilder.<String>ok().withResult(uuid.toString()).build();
        } else {
            return ServiceResponseBuilder.<String>error()
                    .withMessage(UserService.Validations.INVALID_USER_EMAIL.getCode()).build();
        }
    }

    @Override
    public ServiceResponse<Boolean> isValidToken(String token) {
        if (token != null) {
            Token result = tokenRepository.findOne(token);
            if (!Optional.ofNullable(result).isPresent()
            		|| result.getIsExpired() || result.getExpirationDateTime().isBefore(Instant.now())) {
                return ServiceResponseBuilder.<Boolean>error()
                        .withMessage(Validations.INVALID_TOKEN.getCode())
                        .withResult(false).build();
            } else {
                return ServiceResponseBuilder.<Boolean>ok()
                        .withResult(true).build();
            }
        } else {
            return ServiceResponseBuilder.<Boolean>error()
                    .withMessage(Validations.INVALID_TOKEN.getCode())
                    .withResult(false).build();
        }
    }

    @Override
    public ServiceResponse<Boolean> invalidateToken(String token) {
        if (token != null) {
            Token result = tokenRepository.findOne(token);
            if (result != null && !result.getIsExpired()) {
                result.setUseDateTime(Instant.now());
                result.setIsExpired(true);
                tokenRepository.save(result);
                return ServiceResponseBuilder.<Boolean>ok()
                        .withResult(true).build();
            } else {
                return ServiceResponseBuilder.<Boolean>error()
                        .withMessage(Validations.INVALID_TOKEN.getCode())
                        .withResult(false).build();
            }
        } else {
            return ServiceResponseBuilder.<Boolean>error()
                    .withMessage(Validations.INVALID_TOKEN.getCode())
                    .withResult(false).build();
        }
    }
}
