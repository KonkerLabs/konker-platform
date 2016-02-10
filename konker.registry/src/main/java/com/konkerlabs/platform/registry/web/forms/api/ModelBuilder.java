package com.konkerlabs.platform.registry.web.forms.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;

public interface ModelBuilder<M,F> {

    M toModel() throws BusinessException;
    F fillFrom(M model);
}
