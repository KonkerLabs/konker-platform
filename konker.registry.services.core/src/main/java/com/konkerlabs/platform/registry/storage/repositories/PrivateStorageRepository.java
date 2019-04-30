package com.konkerlabs.platform.registry.storage.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingServiceImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PrivateStorageRepository {

    public static final String ID = "_id";
    private static Logger LOG = LoggerFactory.getLogger(PrivateStorageRepository.class);

	@Autowired
    @Qualifier("mongoPrivateStorageTemplate")
	private MongoTemplate mongoPrivateStorageTemplate;

    private JsonParsingService jsonParsingService;

    public PrivateStorageRepository() {
        jsonParsingService = new JsonParsingServiceImpl();
    }

	public void save(String collectionName, Map<String, Object> collectionContent) {
		checkCollection(collectionName);

        final BasicDBObject[] document = {new BasicDBObject()};

		collectionContent.entrySet().stream()
                .forEach(entry -> {
                        document[0] = document[0].append(entry.getKey(), entry.getValue());
                });
        document[0].removeField("_class");

		DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
		collection.insert(document[0]);
	}

	public PrivateStorage update(String collectionName,  Map<String, Object> content) throws JsonProcessingException {
        DBObject queryById = new BasicDBObject().append(ID, content.get(ID));

        DBCollection collectionFor = mongoPrivateStorageTemplate.getCollection(collectionName);
        DBObject dbObject = collectionFor.findOne(queryById);

        if (!Optional.ofNullable(dbObject).isPresent()) {
            return null;
        }

        content.remove("_id");
        dbObject.putAll(content);

	    DBObject query = new BasicDBObject().append(ID, dbObject.get(ID));

        DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
        collection.update(query, dbObject);

        return PrivateStorage.builder()
                .collectionName(collectionName)
                .collectionContent(jsonParsingService.toJsonString(dbObject.toMap()))
                .build();
    }

    public void remove(String collectionName, String id) {
        DBObject query = new BasicDBObject();
        query.put(ID, id);

        DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
        collection.remove(query);
	}

	public List<PrivateStorage> findAll(String collectionName) throws JsonProcessingException {
		List<PrivateStorage> privatesStorage = new ArrayList<>();

		DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
		DBCursor cursor = collection.find();

        toPrivateStorageList(collectionName, privatesStorage, cursor);

        return privatesStorage;
	}

	public PrivateStorage findById(String collectionName, String id) throws JsonProcessingException {
        DBObject query = new BasicDBObject();
        query.put(ID, id);

        DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
        DBObject object = collection.findOne(query);

        if (!Optional.ofNullable(object).isPresent()) {
            return null;
        }

        return PrivateStorage.builder()
                .collectionName(collectionName)
                .collectionContent(jsonParsingService.toJsonString(object.toMap()))
                .build();
    }

    public List<PrivateStorage> findByQuery(String collectionName,
                                            Map<String, String> queryParam,
                                            String sort,
                                            int pageNumber,
                                            int pageSize) throws JsonProcessingException {
        List<BasicDBObject> criterias = queryParam.entrySet()
                .stream()
                .map(item -> {
                    String[] params = item.getValue().split(":");
                    String value = params[0];
                    FilterEnum filter = FilterEnum.DEFAULT;

                    if (params.length == 2) {
                        String operator = params[0];
                        value = params[1];
                        filter = FilterEnum.valueOf(operator.toUpperCase());
                    }

                    return filter.criteria(item.getKey(), value);
                })
                .collect(Collectors.toList());

        List<PrivateStorage> privatesStorage = new ArrayList<>();
        DBObject query = new BasicDBObject();

        if (!criterias.isEmpty()) {
            List<BasicDBObject> andCriteria = new ArrayList<>();
            andCriteria.addAll(criterias);
            query.put("$and", andCriteria);
        }


        DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
        DBCursor cursor = collection.find(query);

        if (sort != null) {
            String[] sortParams = sort.split(":");
            BasicDBObject sortObject = SortEnum.valueOf(sortParams[0].toUpperCase()).sort(sortParams[1]);
            cursor.sort(sortObject);
        }

        toPrivateStorageList(collectionName, privatesStorage, cursor, pageNumber, pageSize);

        return privatesStorage;
    }

    private void toPrivateStorageList(String collectionName,
                                      List<PrivateStorage> privatesStorage,
                                      DBCursor cursor,
                                      int pageNumber,
                                      int pageSize) throws JsonProcessingException {
        try {
            cursor.skip(pageNumber);
            cursor.limit(pageSize);
            while (cursor.hasNext()) {
                cursor.next();

                String content = jsonParsingService.toJsonString(cursor.curr().toMap());

                privatesStorage.add(PrivateStorage.builder()
                        .collectionName(collectionName)
                        .collectionContent(content)
                        .build());
            }
        } finally {
            cursor.close();
        }
    }

    private void toPrivateStorageList(String collectionName, List<PrivateStorage> privatesStorage, DBCursor cursor) throws JsonProcessingException {
        try {
            while (cursor.hasNext()) {
                cursor.next();

                String content = jsonParsingService.toJsonString(cursor.curr().toMap());

                privatesStorage.add(PrivateStorage.builder()
                        .collectionName(collectionName)
                        .collectionContent(content)
                        .build());
            }
        } finally {
            cursor.close();
        }
    }

    public Set<String> listCollections() {
        Set<String> collectionNames = mongoPrivateStorageTemplate.getCollectionNames();
        collectionNames.remove("system.users");
        collectionNames.remove("system.indexes");
        return collectionNames;
    }

	private void checkCollection(String collectionName) {
		if (!mongoPrivateStorageTemplate.collectionExists(collectionName)) {
			CollectionOptions options = new CollectionOptions(1073741824, null, false);
			mongoPrivateStorageTemplate.createCollection(collectionName, options);
		}
	}
}