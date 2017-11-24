package com.konkerlabs.platform.registry.test.data.base;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.repositories.AccessTokenRepository;
import com.konkerlabs.platform.registry.business.repositories.AuthorizationCodeRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.OauthClientDetailRepository;
import com.konkerlabs.platform.registry.business.repositories.RoleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.LoginAuditService;
import com.konkerlabs.platform.registry.data.security.DeviceUserDetailsService;

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
    public LoginAuditService loginAuditService(){
        LoginAuditService loginAuditService = mock(LoginAuditService.class);

        return loginAuditService;
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

    @Bean
    public DeviceRegisterService deviceRegistryService() {
    	return Mockito.mock(DeviceRegisterService.class);
    }

    @Bean
    public OauthClientDetailRepository oauthClientDetailRepository(){
    	return Mockito.mock(OauthClientDetailRepository.class);
    }
    
    @Bean
    public AuthorizationCodeRepository authorizationCodeRepository(){
    	return Mockito.mock(AuthorizationCodeRepository.class);
    }
    
    @Bean
    public AccessTokenRepository accessTokenRepository(){
    	return Mockito.mock(AccessTokenRepository.class);
    }
    
    @Bean
    public TenantRepository tenantRepository(){
    	return Mockito.mock(TenantRepository.class);
    }  
    
    @Bean
    public RoleRepository roleRepository(){
    	return Mockito.mock(RoleRepository.class);
    }  

}
