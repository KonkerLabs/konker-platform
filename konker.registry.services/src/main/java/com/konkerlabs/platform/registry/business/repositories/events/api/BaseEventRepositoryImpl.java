package com.konkerlabs.platform.registry.business.repositories.events.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.time.Instant;
import java.util.List;

public abstract class BaseEventRepositoryImpl implements EventRepository {

    protected enum Type {
        INCOMING("incoming", EVENTS_INCOMING_COLLECTION_NAME),
        OUTGOING("outgoing", EVENTS_OUTGOING_COLLECTION_NAME);

        private String actorFieldName;
        private String collectionName;

        public String getActorFieldName() {
            return actorFieldName;
        }

        public String getCollectionName() {
            return collectionName;
        }

        Type(String actorFieldName, String collectionName) {
            this.actorFieldName = actorFieldName;
            this.collectionName = collectionName;
        }
    }

    @Override
    public void removeBy(Tenant tenant, String deviceGuid) throws BusinessException {
        try {
            doRemoveBy(tenant, deviceGuid, Type.INCOMING);
            doRemoveBy(tenant, deviceGuid, Type.OUTGOING);
        } catch (Exception e){
            throw new BusinessException(e.getMessage(), e);
        }
    }

    @Override
    public List<Event> findIncomingBy(Tenant tenant,
                                      String deviceGuid,
                                      String channel,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant, deviceGuid, channel, startInstant, endInstant, ascending, limit,
                Type.INCOMING, false);
    }

    @Override
    public List<Event> findOutgoingBy(Tenant tenant,
                                      String deviceGuid,
                                      String channel,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindBy(tenant, deviceGuid, channel, startInstant, endInstant, ascending, limit,
                Type.OUTGOING, false);
    }


    @Override
    public Event saveIncoming(Tenant tenant, Event event) throws BusinessException {
        return doSave(tenant,event, Type.INCOMING);
    }

    @Override
    public Event saveOutgoing(Tenant tenant, Event event) throws BusinessException {
        return doSave(tenant,event, Type.OUTGOING);
    }

    protected abstract Event doSave(Tenant tenant, Event event, Type incoming) throws BusinessException;

    protected abstract void doRemoveBy(Tenant tenant, String deviceGuid, Type incoming) throws Exception;

    protected abstract List<Event> doFindBy(Tenant tenant, String deviceGuid, String channel,
                                            Instant startInstant, Instant endInstant, boolean ascending,
                                            Integer limit, Type incoming, boolean b) throws BusinessException;

}


