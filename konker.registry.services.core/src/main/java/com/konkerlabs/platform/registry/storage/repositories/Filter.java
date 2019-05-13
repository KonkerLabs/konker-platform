package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

public interface Filter {

    BasicDBObject criteria(String key, Object value);

}
