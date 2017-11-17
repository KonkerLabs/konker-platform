package com.konkerlabs.platform.registry.api.test.config;

import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.idm.config.OAuthClientDetailsService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
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

        return OauthClientDetails.builder().build().setUserProperties(user);
    }

    @Bean
    public DeviceRegisterService deviceRegistryService() {
        return Mockito.mock(DeviceRegisterService.class);
    }

    @Bean
    public EventRouteService eventRouteService() {
        return Mockito.mock(EventRouteService.class);
    }

    @Bean
    public TransformationService transformationService() {
        return Mockito.mock(TransformationService.class);
    }

    @Bean
    public RestDestinationService restDestinationService() {
    	return Mockito.mock(RestDestinationService.class);
    }

    @Bean
    public DeviceEventService deviceEventService() {
        return Mockito.mock(DeviceEventService.class);
    }

    @Bean
    public UserService userService() {
    	return Mockito.mock(UserService.class);
    }

    @Bean
    public RoleService roleService() {
    	return Mockito.mock(RoleService.class);
    }

    @Bean
    public ApplicationService applicationService() {
    	return Mockito.mock(ApplicationService.class);
    }

    @Bean
    public LocationSearchService locationSearchService() {
        return Mockito.mock(LocationSearchService.class);
    }

    @Bean
    public LocationService locationService() {
        return Mockito.mock(LocationService.class);
    }

    @Bean
    public DeviceConfigSetupService deviceConfigSetupService() {
        return Mockito.mock(DeviceConfigSetupService.class);
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
    public DeviceCustomDataService deviceCustomDataService() {
        return Mockito.mock(DeviceCustomDataService.class);
    }

    @Bean
    public GatewayService gatewayService() {
        return Mockito.mock(GatewayService.class);
    }

    @Bean
    public OAuthClientDetailsService oauthClientDetailsService() {
        return Mockito.mock(OAuthClientDetailsService.class);
    }

    @Bean
    public DefaultTokenServices defaultTokenServices() {
        return Mockito.mock(DefaultTokenServices.class);
    }

    @Bean
    public ApplicationDocumentStoreService applicationDocumentStoreService() {
        return Mockito.mock(ApplicationDocumentStoreService.class);
    }

    @Bean
    public DeviceFirmwareService deviceFirmwareServiceeviceFirmwareService() {
        return Mockito.mock(DeviceFirmwareService.class);
    }

}
