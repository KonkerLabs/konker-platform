package com.konkerlabs.platform.registry.data.security;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("deviceDetails")
public class DeviceUserDetailsService implements UserDetailsService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public UserDetails loadUserByUsername(String apiKey) throws UsernameNotFoundException {
        Device device = deviceRepository.findByApiKey(apiKey);

        if (device == null || !device.isActive()) {
            throw new UsernameNotFoundException("authentication.credentials.invalid");
        }

        return device;
    }

}
