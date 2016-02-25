package com.konkerlabs.platform.registry.web.forms.api;

import java.util.function.Supplier;

public interface ModelBuilder<M,F,S> {

    M toModel();
    F fillFrom(M model);
    void setAdditionalSupplier(Supplier<S> sSupplier);
}
