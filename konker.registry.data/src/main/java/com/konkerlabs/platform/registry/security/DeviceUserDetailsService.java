package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("deviceDetails")
public class DeviceUserDetailsService implements UserDetailsService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public UserDetails loadUserByUsername(String apiKey) throws UsernameNotFoundException {
        return Optional
                .ofNullable(deviceRepository.findByApiKey(apiKey))
                .orElseThrow(() -> new UsernameNotFoundException("authentication.credentials.invalid"));
    }
}
