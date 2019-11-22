package com.konkerlabs.platform.registry.business.repositories.events.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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
    public void removeBy(Tenant tenant, Application application, String deviceGuid) throws BusinessException {
        try {
            doRemoveByCommon(tenant, application, deviceGuid, Type.INCOMING);
            doRemoveByCommon(tenant, application, deviceGuid, Type.OUTGOING);
        } catch (Exception e){
            throw new BusinessException(e.getMessage(), e);
        }
    }
    
    @Override
    public void removeIncomingBy(Tenant tenant, Application application, String deviceGuid, List<Event> events) throws BusinessException {
    	try {
            doRemoveByCommon(tenant, application, deviceGuid, events, Type.INCOMING);
        } catch (Exception e){
            throw new BusinessException(e.getMessage(), e);
        }
    }
    
    @Override
    public void removeOutgoingBy(Tenant tenant, Application application, String deviceGuid, List<Event> events) throws BusinessException {
    	try {
            doRemoveByCommon(tenant, application, deviceGuid, events, Type.OUTGOING);
        } catch (Exception e){
            throw new BusinessException(e.getMessage(), e);
        }
    }

    @Override
    public void copy(Tenant tenant, Device originDevice, Device destDevice) throws BusinessException {

        Instant startInstant = ZonedDateTime.now().minusYears(100L).toInstant();
        Instant endInstant = ZonedDateTime.now().plusYears(100L).toInstant();

        // incoming
        List<Event> incomingEvents = findIncomingBy(tenant, originDevice.getApplication(), originDevice.getGuid(), null, null, startInstant, endInstant, false, null);
        for (Event event : incomingEvents) {
            event.getIncoming().setApplicationName(destDevice.getApplication().getName());
            event.getIncoming().setDeviceGuid(destDevice.getGuid());
            event.getIncoming().setDeviceId(destDevice.getDeviceId());

            saveIncoming(tenant, destDevice.getApplication(), event);
        }

        // outgoing
        List<Event> outgoingEvents = findOutgoingBy(tenant, originDevice.getApplication(), originDevice.getGuid(), null, null, startInstant, endInstant, false, null);
        for (Event event : outgoingEvents) {
            event.getOutgoing().setApplicationName(destDevice.getApplication().getName());
            event.getOutgoing().setDeviceGuid(destDevice.getGuid());
            event.getOutgoing().setDeviceId(destDevice.getDeviceId());

            saveOutgoing(tenant, destDevice.getApplication(), event);
        }
    }

    @Override
    public List<Event> findIncomingBy(Tenant tenant,
                                      Application application,
                                      String deviceGuid,
                                      String locationGuid,
                                      String channel,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindByCommon(tenant, application, deviceGuid, locationGuid, channel, startInstant, endInstant, ascending,
                limit, Type.INCOMING, false);
    }

    @Override
    public List<Event> findOutgoingBy(Tenant tenant,
                                      Application application,
                                      String deviceGuid,
                                      String locationGuid,
                                      String channel,
                                      Instant startInstant,
                                      Instant endInstant,
                                      boolean ascending,
                                      Integer limit) throws BusinessException {
        return doFindByCommon(tenant, application, deviceGuid, locationGuid, channel, startInstant, endInstant, ascending,
                limit, Type.OUTGOING, false);
    }


    @Override
    public Event saveIncoming(Tenant tenant, Application application, Event event) throws BusinessException {
        return doSaveCommon(tenant, application, event, Type.INCOMING);
    }

    @Override
    public Event saveOutgoing(Tenant tenant, Application application, Event event) throws BusinessException {
        return doSaveCommon(tenant, application, event, Type.OUTGOING);
    }

    protected abstract Event doSave(Tenant tenant, Application application, Event event, Type incoming) throws BusinessException;

    protected abstract void doRemoveBy(Tenant tenant, Application application, String deviceGuid, Type incoming) throws Exception;
    
    protected abstract void doRemoveBy(Tenant tenant, Application application, String deviceGuid, List<Event> events, Type incoming) throws Exception;

    protected abstract List<Event> doFindBy(Tenant tenant, Application application, String deviceGuid, String locationGuid, String channel,
                                            Instant startInstant, Instant endInstant, boolean ascending,
                                            Integer limit, Type incoming, boolean b) throws BusinessException;

    private Event doSaveCommon(Tenant tenant, Application application, Event event, Type incoming) throws BusinessException {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));
                Optional.ofNullable(event)
                .orElseThrow(() -> new BusinessException(CommonValidations.RECORD_NULL.getCode()));
        Optional.ofNullable(event.getIncoming())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_INCOMING_NULL.getCode()));
        Optional.ofNullable(event.getIncoming().getDeviceGuid()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Validations.INCOMING_DEVICE_GUID_NULL.getCode()));
        Optional.ofNullable(event.getIncoming().getChannel()).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new BusinessException(Validations.EVENT_INCOMING_CHANNEL_NULL.getCode()));

        return doSave(tenant, application, event, incoming);

    }

    private void doRemoveByCommon(Tenant tenant, Application application, String deviceGuid, Type type) throws Exception {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));

        doRemoveBy(tenant, application, deviceGuid, type);

    }
    
    private void doRemoveByCommon(Tenant tenant, Application application, String deviceGuid, List<Event> events, Type type) throws Exception {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(deviceGuid).filter(s -> !s.isEmpty())
        		.orElseThrow(() -> new IllegalArgumentException("Device ID cannot be null or empty"));
       
        doRemoveBy(tenant, application, deviceGuid, events, type);

    }

    private List<Event> doFindByCommon(Tenant tenant, Application application, String deviceGuid, String locationGuid, String channel,
                                       Instant startInstant, Instant endInstant, boolean ascending,
                                       Integer limit, Type incoming, boolean isDeleted) throws BusinessException {

        Optional.ofNullable(tenant)
                .filter(tenant1 -> Optional.ofNullable(tenant1.getDomainName()).filter(s -> !s.isEmpty()).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));

        if (!Optional.ofNullable(startInstant).isPresent() &&
                !Optional.ofNullable(limit).isPresent())
            throw new IllegalArgumentException("Limit cannot be null when start instant isn't provided");

        return doFindBy(tenant, application, deviceGuid,
                locationGuid, channel, startInstant, endInstant,
                ascending, limit, incoming, isDeleted);

    }


}


