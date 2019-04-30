package com.konkerlabs.platform.registry.storage.repositories;

import com.mongodb.BasicDBObject;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum FilterEnum implements Filter {

    EQ("eq") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, value);
        }
    },
    NE("ne") {
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
            return createCriteria(key, Arrays.stream(value.toString().split(",")).collect(Collectors.toList()));
        }
    },
    NIN("nin") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, Arrays.stream(value.toString().split(",")).collect(Collectors.toList()));
        }
    },
    BTN("btn") {
        @Override
        public BasicDBObject criteria(String key, Object value) {
            return createCriteria(key, Arrays.stream(value.toString().split(",")).collect(Collectors.toList()));
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
        } if (operator.equals("in")
                && value instanceof List
                && !((List) value).stream()
                    .filter(f -> !f.toString().matches(regex))
                    .findAny()
                    .isPresent()) {
            BasicDBObject firstCriteria = new BasicDBObject();
            firstCriteria.put(key, new BasicDBObject("$".concat(operator), ((List) value)
                    .stream()
                    .map(v -> new Long(v.toString()))
                    .collect(Collectors.toList())));

            BasicDBObject secondCriteria = new BasicDBObject();
            secondCriteria.put(key, new BasicDBObject("$".concat(operator),  ((List) value)
                    .stream()
                    .collect(Collectors.toList())));

            BasicDBObject or = new BasicDBObject();
            or.put("$or", Arrays.asList(firstCriteria, secondCriteria));
            return or;

        } if (operator.equals("nin")
                && value instanceof List
                && !((List) value).stream()
                .filter(f -> !f.toString().matches(regex))
                .findAny()
                .isPresent()) {
            BasicDBObject firstCriteria = new BasicDBObject();
            firstCriteria.put(key, new BasicDBObject("$".concat(operator), ((List) value)
                    .stream()
                    .map(v -> new Long(v.toString()))
                    .collect(Collectors.toList())));

            BasicDBObject secondCriteria = new BasicDBObject();
            secondCriteria.put(key, new BasicDBObject("$".concat(operator),  ((List) value)
                    .stream()
                    .collect(Collectors.toList())));

            BasicDBObject and = new BasicDBObject();
            and.put("$and", Arrays.asList(firstCriteria, secondCriteria));
            return and;

        } else if (operator.equals("btn")
                && value instanceof List
                && !((List) value).stream()
                .filter(f -> !f.toString().matches(regex))
                .findAny()
                .isPresent()) {
            BasicDBObject firstBetween = new BasicDBObject();
            firstBetween.put(
                    key,
                    new BasicDBObject("$gte", ((List) value).get(0)).append("$lt", ((List) value).get(1)));

            BasicDBObject secondBetween = new BasicDBObject();
            secondBetween.put(
                    key,
                    new BasicDBObject("$gte", new Long(((List) value).get(0).toString())).append("$lt", new Long(((List) value).get(1).toString())));

            BasicDBObject or = new BasicDBObject();
            or.put("$or", Arrays.asList(firstBetween, secondBetween));
            return or;
        } else if (operator.equals("btn")) {
            BasicDBObject between = new BasicDBObject();
            between.put(
                    key,
                    new BasicDBObject("$gte", ((List) value).get(0)).append("$lt", ((List) value).get(1)));

            return between;
        } else {
            BasicDBObject criteria = new BasicDBObject();
            criteria.put(key, new BasicDBObject("$".concat(operator), value));
            return criteria;
        }
    }
}