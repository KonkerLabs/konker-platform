package com.konkerlabs.platform.registry.alerts.test.config;

import com.konkerlabs.platform.registry.alerts.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.alerts.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.idm.services.OAuth2AccessTokenService;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

@Configuration
public class WebTestConfiguration {

    @Bean
    public Tenant tenant() {
        return Tenant.builder().name("konker").domainName("konker").id("id").build();
    }

    @Bean
    public Application application() {
        return Application.builder().name("konker").build();
    }

    @Bean
    public OauthClientDetails user() {
        User user = User.builder()
                .email("user@domain.com")
                .zoneId(TimeZone.AMERICA_SAO_PAULO)
                .language(Language.EN)
                .avatar("default.jpg")
                .dateFormat(DateFormat.YYYYMMDD)
                .tenant(tenant()).build();

        return OauthClientDetails
                .builder()
                .build()
                .setUserProperties(user);
    }


    @Bean
    public LocationSearchService locationSearchService() {
        return Mockito.mock(LocationSearchService.class);
    }

    @Bean
    public ApplicationService applicationService() {
        return Mockito.mock(ApplicationService.class);
    }

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public DeviceModelService deviceModelService() {
    	return Mockito.mock(DeviceModelService.class);
    }

    @Bean
    public SilenceTriggerService silenceTriggerService() {
        return Mockito.mock(SilenceTriggerService.class);
    }

    @Bean
    public AlertTriggerService alertTriggerService() {
        return Mockito.mock(AlertTriggerService.class);
    }

    @Bean
    public HealthAlertService healthAlertService() {
        return Mockito.mock(HealthAlertService.class);
    }

    @Bean
    public OAuth2AccessTokenService oAuth2AccessTokenService() {
        return Mockito.mock(OAuth2AccessTokenService.class);
    }

    @Bean
    public DefaultTokenServices defaultTokenServices() {
        return Mockito.mock(DefaultTokenServices.class);
    }

    @Bean
    public OAuthClientDetailsService oAuthClientDetailsService() {
        return Mockito.mock(OAuthClientDetailsService.class);
    }

}
