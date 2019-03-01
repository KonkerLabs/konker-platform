package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
	
	@Query("{ 'tenant.id' : ?0 }")
	List<User> findAllByTenantId(String tenantId);

	@Query("{ 'tenant.id' : ?0, '_id' : ?1}")
	User findAllByTenantIdAndEmail(String tenantId, String email);
	
	@Query(value = "{ 'registrationDate' : { '$gte' : ?0, '$lte' : ?1 }}", count = true)
	Long countRegistrationsBetweenDate(Instant start, Instant end);
	
	@Query("{ '_id' : ?0}")
	User findByEmail(String email);

    @Query("{ 'tenant.id' : ?0, 'application' : { $exists: false }, 'location' : { $exists: false } }")
    List<User> findAllAdminUsers(String tenantId);

	@Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'location' : { $exists: false } }")
	List<User> findAllByTenantIdApplicationName(String tenantId, String applicationName);

    @Query("{ 'tenant.id' : ?0, 'application.name' : ?1, 'location.id' : ?2 }")
    List<User> findAllByTenantIdApplicationNameLocationId(String tenantId, String applicationName, String locationId);

}
