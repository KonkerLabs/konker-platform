package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    @Query("{ 'tenant.id' : ?0 }")
    List<Device> findAllByTenant(String tenantId);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceId' : ?2 }")
    @Cacheable(value = "deviceCache", keyGenerator = "customKeyGenerator")
    Device findByTenantIdAndApplicationAndDeviceId(String tenantId, String applicationName, String deviceId);
    @Query("{ 'tenant.id' : ?0, 'guid' : ?1 }")
    @Cacheable(value = "deviceCache", keyGenerator = "tenantIdDeviceGuidKeyGenerator")
    Device findByTenantAndGuid(String tenantId, String deviceGuid);
    @Query("{ 'apiKey' : ?0 }")
    @Cacheable(value = "deviceCache", keyGenerator = "apiKeyCustomKeyGenerator")
    Device findByApiKey(String apiKey);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1 }")
	List<Device> findAllByTenantIdAndApplicationName(String tenantId, String applicationName);
    @Query(value = "{ 'tenant.id' : ?0, 'application.name' : ?1 }", count = true)
    Long countAllByTenantIdAndApplicationName(String tenantId, String applicationName);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'guid' : ?2 }")
    Device findByTenantAndApplicationAndGuid(String tenantId, String applicationName, String guid);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'location.id' : ?2 }")
    List<Device> findAllByTenantIdAndApplicationNameAndLocationName(String tenantId, String applicationName, String locationId);
    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'deviceModel.id' : ?2 }")
	List<Device> findAllByTenantIdApplicationNameAndDeviceModel(String tenantId, String applicationName, String deviceModelId);

    @Caching(put = {
            @CachePut(value = "deviceCache", keyGenerator = "customKeyGenerator"),
            @CachePut(value = "deviceCache", keyGenerator = "apiKeyCustomKeyGenerator"),
            @CachePut(value = "deviceCache", keyGenerator = "tenantIdDeviceGuidKeyGenerator")
    })
    @Override
    <S extends Device> S save(S s);

    @Caching(evict = {
            @CacheEvict(value = "deviceCache", keyGenerator = "apiKeyRemovedKeyGenerator"),
            @CacheEvict(value = "deviceCache", keyGenerator = "tenantDeviceGuidRemovedKeyGenerator"),
            @CacheEvict(value = "deviceCache", keyGenerator = "tenantApplicationDeviceIdRemovedCustomKeyGenerator"),
            @CacheEvict(value = "eventSchemaCache", keyGenerator = "deviceGuidRemovedKeyGenerator")
    })
    @Override
    void delete(Device device);
}