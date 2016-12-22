package com.konkerlabs.platform.registry.test.base;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.security.DeviceUserDetailsService;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

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
	public UserContextResolver userContextResolver() {
		return mock(UserContextResolver.class);
	}
	
	@Bean
	public TenantUserDetailsService userDetailsService(){
    	TenantUserDetailsService tenantDetailService = mock(TenantUserDetailsService.class);

    	when(tenantDetailService.loadUserByUsername(Mockito.anyString()))
    		.thenReturn(User.builder().email("admin").password("admin123").build());

		return tenantDetailService;
	}

    @Bean
    public DeviceUserDetailsService deviceUserDetailsService(){
    	DeviceUserDetailsService instance = Mockito.mock(DeviceUserDetailsService.class);

    	Mockito.when(instance.loadUserByUsername(Mockito.anyString()))
    		.thenReturn(Device.builder().deviceId("123").build());

    	return instance;
    }

    @Bean
    public DeviceRepository deviceRepository(){
    	return Mockito.mock(DeviceRepository.class);
    }

    @Bean
    public UserRepository userRepository(){
    	return Mockito.mock(UserRepository.class);
    }

    @Bean
    public LocaleResolver localeResolver(){
        return new SessionLocaleResolver();
    }
}
