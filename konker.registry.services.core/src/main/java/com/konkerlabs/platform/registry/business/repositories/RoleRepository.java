package com.konkerlabs.platform.registry.business.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.konkerlabs.platform.registry.business.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
	
	@Query("{ 'name' : ?0 }")
	Role findByName(String name);

}
