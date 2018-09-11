package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceFwUpdate;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DeviceFirmwareUpdateRepository extends MongoRepository<DeviceFwUpdate, String> {

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceGuid' : ?2, 'version' : ?3 }")
    DeviceFwUpdate findUnique(String tenantId, String applicationName, String deviceGuid, String version);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceGuid' : ?2, 'status' : ?3 }")
    DeviceFwUpdate findUnique(String tenantId, String applicationName, String deviceGuid, FirmwareUpdateStatus status);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceFirmware.id' : ?2 }")
    List<DeviceFwUpdate> findByDeviceFirmware(String tenantId, String applicationName, String deviceFirmwareId);

}