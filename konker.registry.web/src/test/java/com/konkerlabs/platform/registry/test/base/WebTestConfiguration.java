package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.LoginAuditService;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public LoginAuditService loginAuditService(){
        LoginAuditService loginAuditService = mock(LoginAuditService.class);

        return loginAuditService;
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

    @Bean
    public ConverterUtils converterUtils(){
        ConverterUtils converterUtils = Mockito.mock(ConverterUtils.class);
        when(converterUtils.getUserZoneID()).thenReturn("America/Sao_Paulo");
        when(converterUtils.getCurrentLocale()).thenReturn(
                new Locale("pt", "BR")
        );
        when(converterUtils.getDateTimeFormatPattern())
                .thenReturn("dd/MM/yyyy HH:mm:ss.SSS zzz");
        return converterUtils;
    }

    @Bean
    public DeviceRegisterService deviceRegistryService() {
    	return Mockito.mock(DeviceRegisterService.class);
    }

}
