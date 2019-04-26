package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

public interface Filter {

    public BasicDBObject criteria(String key, Object value);

}
