package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Role;

public interface RoleService {
    enum Validations {
        ROLE_NOT_EXIST("service.role.validation.not.exist");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
    
    ServiceResponse<Role> findByName(String name);

}
