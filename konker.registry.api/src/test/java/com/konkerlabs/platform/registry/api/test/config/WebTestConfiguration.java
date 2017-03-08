package com.konkerlabs.platform.registry.api.test.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;

@Configuration
public class WebTestConfiguration {

    @Bean
    public Tenant tenant() {
        return Tenant.builder().name("konker").domainName("konker").id("id").build();
    }

    @Bean
    public User user() {
        return User.builder()
                .email("user@domain.com")
                .zoneId(TimeZone.AMERICA_SAO_PAULO)
                .language(Language.EN)
                .avatar("default.jpg")
                .dateFormat(DateFormat.YYYYMMDD)
                .tenant(tenant()).build();
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
    public RestDestinationService restDestinationService() {
        return Mockito.mock(RestDestinationService.class);
    }

}
