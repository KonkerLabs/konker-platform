package com.konkerlabs.platform.registry.business.repositories.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import com.mongodb.DBObject;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository("solr")
public class EventRepositorySolrImpl implements EventRepository {

    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void push(Tenant tenant, Event event) throws BusinessException {
        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(event)
            .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(event.getTimestamp())
            .orElseThrow(() -> new IllegalStateException("Event timestamp cannot be null"));

        SolrTemplate template = applicationContext.getBean(SolrTemplate.class);
        template.setSolrCore(MessageFormat.format("{0}-{1}",tenant.getDomainName(),"events"));

        SolrInputDocument toBeSent = new SolrInputDocument();

        try {
            jsonParsingService.toFlatMap(event.getPayload()).forEach((field, value) -> {
                toBeSent.addField(field,value);
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException("Payload processing exception",e);
        }

        toBeSent.remove("ts");
        toBeSent.addField("ts", event.getTimestamp().toString());

        template.saveDocument(toBeSent);
        template.commit();
    }

    @Override
    public List<Event> findBy(Tenant tenant, String deviceId, Instant startInstant, Instant endInstant, Integer limit) {
        return Collections.emptyList();
    }
}
