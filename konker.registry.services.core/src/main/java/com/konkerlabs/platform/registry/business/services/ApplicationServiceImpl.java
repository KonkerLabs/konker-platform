package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository repository;

    @Override
    public ServiceResponse<Application> save(Application application) {
        return null;
    }

    @Override
    public ServiceResponse<List<Application>> findAll(Tenant tenant) {
        return null;
    }

    @Override
    public ServiceResponse<Application> findById(Tenant tenant, String applicationId) {
        return null;
    }

    @Override
    public ServiceResponse<Application> remove(Tenant tenant, Application application) {
        return null;
    }
}
