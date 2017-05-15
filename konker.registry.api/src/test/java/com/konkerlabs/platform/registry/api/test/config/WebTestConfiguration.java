package com.konkerlabs.platform.registry.api.test.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceModelService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import com.konkerlabs.platform.registry.business.services.api.UserService;

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
    public DeviceModelService deviceModelService() {
        return Mockito.mock(DeviceModelService.class);
    }

    @Bean
    public DeviceConfigSetupService deviceConfigSetupService() {
        return Mockito.mock(DeviceConfigSetupService.class);
    }

}
