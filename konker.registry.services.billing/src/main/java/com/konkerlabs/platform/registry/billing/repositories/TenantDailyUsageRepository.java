package com.konkerlabs.platform.registry.billing.repositories;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TenantDailyUsageRepository extends MongoRepository<TenantDailyUsage,String> {
	
	 @Query("{ 'tenantDomain' : ?0 }")
	 List<TenantDailyUsage> findAllByTenantDomain(String tenantDomain);

}
