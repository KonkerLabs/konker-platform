package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.config.EmailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service("tenantUserDetails")
public class TenantUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantUserDetailsService.class);
    private static final int MIN_DELAY_TIME = 100;
    private static final int MAX_DELAY_TIME = 250;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailConfig emailConfig;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Delay time introduced to prevent user enumeration attack
        Random random = new Random();
        int delayTime = random.nextInt(MAX_DELAY_TIME - MIN_DELAY_TIME) + MIN_DELAY_TIME;
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User user = userRepository.findOne(Optional.of(email).orElse("").trim().toLowerCase());
        if(user == null){
        	invalidCredentials(email);

        } else if (Optional.ofNullable(user).isPresent() &&
        			emailConfig.isEnabled() &&
        			!user.isActive()) {
        	invalidCredentials(email);
        }
        
        return user;
    }

	private void invalidCredentials(String email) {
		User noUser = User.builder().email(email).tenant(
		        Tenant.builder().name("NOT_FOUND").domainName("NOT_FOUND").build()
		).build();
		LOGGER.debug("User not found",
		         noUser.toURI(),
		         noUser.getTenant().getLogLevel());
		 throw new UsernameNotFoundException("authentication.credentials.invalid");
	}
}
