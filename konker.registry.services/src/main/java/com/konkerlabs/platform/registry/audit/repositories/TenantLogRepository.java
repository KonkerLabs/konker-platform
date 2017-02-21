package com.konkerlabs.platform.registry.audit.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.config.MongoAuditConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository
public class TenantLogRepository {

	private static Logger LOG = LoggerFactory.getLogger(TenantLogRepository.class);

	@Autowired
	private MongoTemplate mongoAuditTemplate;

	public static final int MAX_DOCUMENTS = 1000;

	public static TenantLogRepository getInstance() {

		try {
			TenantLogRepository tenantLogRepository = new TenantLogRepository();
			tenantLogRepository.mongoAuditTemplate = new MongoAuditConfig().mongoTemplate();

			return tenantLogRepository;
		} catch (Exception e) {
			LOG.error("Exception while creating TenantLogRepository instance", e);

			return null;
		}
		
	}
	
	public List<TenantLog> findAll(Tenant tenant) {

		List<TenantLog> logs = new ArrayList<>(MAX_DOCUMENTS);

		String collectionName = getCollectionName(tenant);

		checkCollection(collectionName);

		DBCollection collection = mongoAuditTemplate.getCollection(collectionName);
		DBCursor cursor = collection.find();

		try {
			while (cursor.hasNext()) {

				cursor.next();

				String message = (String) cursor.curr().get("message");
				String level = (String) cursor.curr().get("level");
				Long time = (Long) cursor.curr().get("time");

				logs.add(TenantLog.builder().level(level).message(message).time(new Date(time)).build());

			}
		} finally {
			cursor.close();
		}

		return logs;

	}

	public void insert(String domainName, long timestampMillis, String level, String message) {

		String collectionName = domainName;

		checkCollection(collectionName);

		DBObject object = new BasicDBObject();
		object.put("time", timestampMillis);
		object.put("level", level);
		object.put("message", message);
		object.removeField("_class");

		DBCollection collection = mongoAuditTemplate.getCollection(collectionName);
		collection.insert(object);

	}

	public void insert(String domainName, Date time, String level, String message) {
		insert(domainName, time.getTime(), level, message);
	}

	private String getCollectionName(Tenant tenant) {
		return tenant.getDomainName();
	}

	private void checkCollection(String collectionName) {

		if (!mongoAuditTemplate.collectionExists(collectionName)) {
			CollectionOptions options = new CollectionOptions(512000, MAX_DOCUMENTS, true);
			mongoAuditTemplate.createCollection(collectionName, options);
		}

	}

}