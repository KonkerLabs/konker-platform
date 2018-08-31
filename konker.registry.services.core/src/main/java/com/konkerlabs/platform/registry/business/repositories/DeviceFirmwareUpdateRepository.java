package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceFwUpdate;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DeviceFirmwareUpdateRepository extends MongoRepository<DeviceFwUpdate, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'device.id' : ?2, 'status' : ?3 }")
    DeviceFwUpdate findUnique(String tenantId, String applicationName, String deviceGuid, FirmwareUpdateStatus status);

}