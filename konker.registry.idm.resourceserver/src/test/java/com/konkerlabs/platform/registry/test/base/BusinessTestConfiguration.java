package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@ComponentScan(basePackages = {
	"com.konkerlabs.platform.registry.idm",
    "com.konkerlabs.platform.registry.business",
    "com.konkerlabs.platform.utilities",
	"com.konkerlabs.platform.registry.audit.repositories",
	"com.konkerlabs.platform.registry.billing.repositories"
}, lazyInit = true)
public class BusinessTestConfiguration {

}
