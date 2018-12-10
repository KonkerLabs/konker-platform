package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.Device;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DeviceSearchRepository  {

    @Autowired
    private MongoTemplate mongoTemplate;

	public List<Device> search(String tenantId, String applicationName, String locationId, String tag) {

        Query query = new Query();

        List<Criteria> criterias = new ArrayList<>();
        criterias.add(Criteria.where("application.name").is(applicationName));

        if (StringUtils.isNotBlank(tag)) {
            criterias.add(Criteria.where("tags").in(tag));
        }

        if (StringUtils.isNotBlank(locationId)) {
            criterias.add(Criteria.where("location.id").is(locationId));
        }

        query.addCriteria(Criteria
                .where("tenant.id").is(tenantId)
                .andOperator(criterias.toArray(new Criteria[criterias.size()]))
        );

        List<Device> devices = mongoTemplate.find(query, Device.class);
        return devices;

    }

}