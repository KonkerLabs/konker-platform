package com.konkerlabs.platform.registry.storage.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.config.MongoPrivateStorageConfig;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingServiceImpl;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.*;

@Repository
public class PrivateStorageRepository {

    public static final String ID = "_id";
    private static Logger LOG = LoggerFactory.getLogger(PrivateStorageRepository.class);

	@Autowired
    @Qualifier("mongoPrivateStorageTemplate")
	private MongoTemplate mongoPrivateStorageTemplate;

    private JsonParsingService jsonParsingService;

	public static PrivateStorageRepository getInstance(Mongo mongo, Tenant tenant, Application application) {
		try {
			PrivateStorageRepository privateStorageRepository = new PrivateStorageRepository();
			MongoPrivateStorageConfig mongoPrivateStorageConfig = new MongoPrivateStorageConfig();
            String dbName = MessageFormat.format("{0}_{1}", tenant.getDomainName(), application.getName());
			mongoPrivateStorageConfig.setDbName(dbName);
			privateStorageRepository.mongoPrivateStorageTemplate = mongoPrivateStorageConfig.mongoTemplate(mongo);
            privateStorageRepository.jsonParsingService = new JsonParsingServiceImpl();

			return privateStorageRepository;
		} catch (Exception e) {
			LOG.error("Exception while creating TenantLogRepository instance", e);

			return null;
		}
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
		List<PrivateStorage> privateStorages = new ArrayList<>();

		DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
		DBCursor cursor = collection.find();

		try {
			while (cursor.hasNext()) {
				cursor.next();

				String content = jsonParsingService.toJsonString(cursor.curr().toMap());

				privateStorages.add(PrivateStorage.builder()
                        .collectionName(collectionName)
                        .collectionContent(content)
                        .build());
			}
		} finally {
			cursor.close();
		}

		return privateStorages;
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