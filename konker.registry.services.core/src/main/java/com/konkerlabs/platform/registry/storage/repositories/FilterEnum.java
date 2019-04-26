package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

import java.util.Arrays;
import java.util.regex.Pattern;

public enum FilterEnum implements Filter {

    EQ("eq") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    NEQ("neq") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    GT("gt") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    GTE("gte") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    LT("lt") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    LTE("lte") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    IN("in") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    NIN("nin") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    BTN("btn") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    LIKE("like") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            BasicDBObject criteria = new BasicDBObject();
            criteria.put(key, Pattern.compile(value.toString()));
            return criteria;
        }
    },
    DEFAULT("") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            BasicDBObject equal = new BasicDBObject();
            equal.put(key, value);
            return equal;
        }
    };

    private String operator;
    private String regex = "\\d+";

    FilterEnum(String operator) {
        this.operator = operator;
    }

    protected BasicDBObject createCriteria(String key, Object value) {
        if (value instanceof String && ((String) value).matches(regex)) {
            BasicDBObject firstCriteria = new BasicDBObject();
            firstCriteria.put(key, new BasicDBObject("$".concat(operator), Long.parseLong(value.toString())));

            BasicDBObject secondCriteria = new BasicDBObject();
            secondCriteria.put(key, new BasicDBObject("$".concat(operator), value.toString()));

            BasicDBObject or = new BasicDBObject();
            or.put("$or", Arrays.asList(firstCriteria, secondCriteria));
            return or;
        } else {
            BasicDBObject criteria = new BasicDBObject();
            criteria.put(key, new BasicDBObject("$".concat(operator), value));
            return criteria;
        }
    }
}