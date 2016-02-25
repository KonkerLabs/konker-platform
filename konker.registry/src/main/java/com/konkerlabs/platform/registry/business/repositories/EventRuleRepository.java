package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.net.URI;
import java.util.List;

public interface EventRuleRepository extends MongoRepository<EventRule,String> {
    @Query("{ 'incoming.uri' : ?0 }")
    List<EventRule> findByIncomingURI(URI uri);
    @Query("{ 'tenant.id' : ?0 }")
    List<EventRule> findAllByTenant(String tenantId);
    @Query("{ 'tenant.id' : ?0, 'id' : ?1 }")
    EventRule findByTenantIdAndRuleId(String tenantId, String name);
}