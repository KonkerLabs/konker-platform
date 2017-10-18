package com.konkerlabs.platform.registry.business.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.User;

public interface UserRepository extends MongoRepository<User, String> {
	
	@Query("{ 'tenant.id' : ?0 }")
	List<User> findAllByTenantId(String tenantId);

	@Query("{ 'tenant.id' : ?0, '_id' : ?1}")
	User findAllByTenantIdAndEmail(String tenantId, String email);
	
	@Query(value = "{ 'registrationDate' : { '$gte' : ?0, '$lte' : ?1 }}", count = true)
	Long countBetweenDate(Instant start, Instant end);
}
