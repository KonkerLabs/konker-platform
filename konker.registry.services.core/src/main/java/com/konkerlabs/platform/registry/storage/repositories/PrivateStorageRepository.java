package com.konkerlabs.platform.registry.storage.repositories;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.config.MongoPrivateStorageConfig;
import com.konkerlabs.platform.registry.storage.model.PrivateStorage;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PrivateStorageRepository {

	private static Logger LOG = LoggerFactory.getLogger(PrivateStorageRepository.class);

	@Autowired
	private MongoTemplate mongoPrivateStorageTemplate;

	public static PrivateStorageRepository getInstance(Mongo mongo, String dbName) {
		try {
			PrivateStorageRepository privateStorageRepository = new PrivateStorageRepository();
			MongoPrivateStorageConfig mongoTenantConfig = new MongoPrivateStorageConfig();
			mongoTenantConfig.setDbName(dbName);
			privateStorageRepository.mongoPrivateStorageTemplate = mongoTenantConfig.mongoTemplate(mongo);

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

	public List<PrivateStorage> findAll(Tenant tenant, String collectionName) {
		List<PrivateStorage> logs = new ArrayList<>();

		checkCollection(collectionName);

		DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
		DBCursor cursor = collection.find();

		try {
			while (cursor.hasNext()) {
				cursor.next();

				String message = (String) cursor.curr().get("message");
				String level = (String) cursor.curr().get("level");

				logs.add(PrivateStorage.builder()
                        .collectionName(level)
                        .keyName(level)
                        .collectionContent(level)
                        .build());
			}
		} finally {
			cursor.close();
		}

		return logs;
	}

	public DBObject findByKey(String collectionName, String key) {
        DBCollection collection = mongoPrivateStorageTemplate.getCollection(collectionName);
        DBObject object = collection.findOne(key);

        return object;
    }

	private void checkCollection(String collectionName) {
		if (!mongoPrivateStorageTemplate.collectionExists(collectionName)) {
			CollectionOptions options = new CollectionOptions(1073741824, null, false);
			mongoPrivateStorageTemplate.createCollection(collectionName, options);
		}
	}

}