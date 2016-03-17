package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface SmsDestinationService {

    ServiceResponse<List<SmsDestination>> findAll(Tenant tenant);

    ServiceResponse<SmsDestination> register(Tenant tenant, SmsDestination destination);

    ServiceResponse<SmsDestination> update(Tenant tenant, String guid, SmsDestination destination);

    ServiceResponse<SmsDestination> getByGUID(Tenant tenant, String guid);

}
