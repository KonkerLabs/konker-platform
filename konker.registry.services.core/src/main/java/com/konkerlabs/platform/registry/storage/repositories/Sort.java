package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

public interface Sort {

    BasicDBObject sort(String field);

}
