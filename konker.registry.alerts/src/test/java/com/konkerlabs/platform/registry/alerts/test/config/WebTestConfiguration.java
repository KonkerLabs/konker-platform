package com.konkerlabs.platform.registry.alerts.test.config;

import com.konkerlabs.platform.registry.alerts.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.alerts.services.api.HealthAlertService;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public LocationSearchService locationSearchService() {
        return Mockito.mock(LocationSearchService.class);
    }

    @Bean
    public ApplicationService applicationService() {
        return Mockito.mock(ApplicationService.class);
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
    public TenantService tenantService() {
        return Mockito.mock(TenantService.class);
    }




}
