package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

public enum SortEnum implements Sort {

    ASC("asc") {
        @Override
        public BasicDBObject sort(String field) {
            return new BasicDBObject(field, 1);
        }
    },
    DESC("desc") {
        @Override
        public BasicDBObject sort(String field) {
            return new BasicDBObject(field, -1);
        }
    };

    private String order;

    SortEnum(String order) {
        this.order = order;
    }

}