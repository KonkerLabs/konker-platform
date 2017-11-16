package com.konkerlabs.platform.registry.business.repositories.events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.Event.EventGeolocation;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.config.CassandraRegistryConfig;
import com.konkerlabs.platform.registry.business.repositories.events.api.BaseEventRepositoryImpl;

@Repository("cassandraEvents")
public class EventRepositoryCassandraImpl extends BaseEventRepositoryImpl implements DisposableBean {

    private static final String INCOMING_EVENTS = "incoming_events";
    private static final String INCOMING_EVENTS_CHANNEL = "incoming_events_channel";
    private static final String INCOMING_EVENTS_DEVICE_GUID = "incoming_events_device_guid";
    private static final String INCOMING_EVENTS_DEVICE_GUID_CHANNEL = "incoming_events_device_guid_channel";
    private static final String INCOMING_EVENTS_DELETED = "incoming_events_deleted";

    private static final String OUTGOING_EVENTS = "outgoing_events";
    private static final String OUTGOING_EVENTS_CHANNEL = "outgoing_events_channel";
    private static final String OUTGOING_EVENTS_DEVICE_GUID = "outgoing_events_device_guid";
    private static final String OUTGOING_EVENTS_DEVICE_GUID_CHANNEL = "outgoing_events_device_guid_channel";
    private static final String OUTGOING_EVENTS_DELETED = "outgoing_events_deleted";

    @Autowired
    private CassandraRegistryConfig config;

    @Autowired
    private Cluster cluster;

    @Autowired
    private Session session;

    private Random rnd = new Random(System.nanoTime());

    private Map<String, PreparedStatement> insertIncomingMap = new HashMap<>();

    private Map<String, PreparedStatement> insertOutgoingMap = new HashMap<>();

    private Map<String, PreparedStatement> selectStatementCache = new HashMap<>();

    @Override
    protected Event doSave(Tenant tenant, Application application, Event event, Type type) {

        event.setEpochTime(event.getTimestamp().toEpochMilli() * 1000000 + rnd.nextInt(1000000));

        if (type == Type.INCOMING) {
            saveEvent(tenant, application, event, type, INCOMING_EVENTS, true);
            saveEvent(tenant, application, event, type, INCOMING_EVENTS_DEVICE_GUID, false);
            saveEvent(tenant, application, event, type, INCOMING_EVENTS_DEVICE_GUID_CHANNEL, false);
            saveEvent(tenant, application, event, type, INCOMING_EVENTS_CHANNEL, false);
        } else if (type == Type.OUTGOING) {
            saveEvent(tenant, application, event, type, OUTGOING_EVENTS, true);
            saveEvent(tenant, application, event, type, OUTGOING_EVENTS_DEVICE_GUID, false);
            saveEvent(tenant, application, event, type, OUTGOING_EVENTS_DEVICE_GUID_CHANNEL, false);
            saveEvent(tenant, application, event, type, OUTGOING_EVENTS_CHANNEL, false);
        }

        return event;

    }

    private void saveEvent(Tenant tenant, Application application, Event event, Type type, String table, boolean synchronous) {

        if (type == Type.INCOMING) {

            PreparedStatement ps = getInsertIncomingPreparedStatement(table);

            BoundStatement statement = ps.bind(tenant.getDomainName(),
                                               application.getName(),
                                               event.getEpochTime(),
                                               event.getIncoming().getChannel(),
                                               event.getIncoming().getDeviceGuid(),
                                               event.getIncoming().getDeviceId(),
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getElev() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getHdop() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLat() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLon() : null,
                                               event.getPayload());
            if (synchronous) {
                session.execute(statement);
            } else {
                session.executeAsync(statement);
            }
        } else if (type == Type.OUTGOING) {

            PreparedStatement ps = getInsertOutgoingPreparedStatement(table);

            BoundStatement statement = ps.bind(tenant.getDomainName(),
                                               application.getName(),
                                               event.getEpochTime(),
                                               event.getOutgoing().getChannel(),
                                               event.getOutgoing().getDeviceGuid(),
                                               event.getOutgoing().getDeviceId(),
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getElev() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getHdop() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLat() : null,
                                               Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLon() : null,
                                               event.getIncoming().getChannel(),
                                               event.getIncoming().getDeviceGuid(),
                                               event.getIncoming().getDeviceId(),
                                               event.getPayload());
            if (synchronous) {
                session.execute(statement);
            } else {
                session.executeAsync(statement);
            }
        }

    }

    private PreparedStatement getInsertOutgoingPreparedStatement(String table) {

        PreparedStatement ps = insertOutgoingMap.get(table);

        if (ps == null) {

            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO ");
            query.append(config.getKeyspace());
            query.append(".");
            query.append(table);
            query.append(" (");
            query.append("tenant_domain, ");
            query.append("application_name, ");
            query.append("timestamp, ");
            query.append("channel, ");
            query.append("device_guid, ");
            query.append("device_id, ");
            
            query.append("geo_elev, ");
            query.append("geo_hdop, ");
            query.append("geo_lat, ");
            query.append("geo_lon, ");

            query.append("incoming_channel, ");
            query.append("incoming_device_guid, ");
            query.append("incoming_device_id, ");

            query.append("payload");
            query.append(") VALUES (");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?");
            query.append(")");

            ps = session.prepare(query.toString());
            insertOutgoingMap.put(table, ps);

        }

        return ps;

    }

    private PreparedStatement getInsertIncomingPreparedStatement(String table) {

        PreparedStatement ps = insertIncomingMap.get(table);

        if (ps == null) {

            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO ");
            query.append(config.getKeyspace());
            query.append(".");
            query.append(table);
            query.append(" (");
            query.append("tenant_domain, ");
            query.append("application_name, ");
            query.append("timestamp, ");
            query.append("channel, ");
            query.append("device_guid, ");
            query.append("device_id, ");
            query.append("geo_elev, ");
            query.append("geo_hdop, ");
            query.append("geo_lat, ");
            query.append("geo_lon, ");
            query.append("payload");
            query.append(") VALUES (");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?, ");
            query.append("?");
            query.append(")");

            ps = session.prepare(query.toString());
            insertIncomingMap.put(table, ps);

        }

        return ps;

    }

    @Override
    protected List<Event> doFindBy(Tenant tenant,
                                   Application application,
                                   String deviceGuid,
                                   String channel,
                                   Instant startInstant,
                                   Instant endInstant,
                                   boolean ascending,
                                   Integer limit,
                                   Type type,
                                   boolean isDeleted) throws BusinessException {

        String table = null;

        List<Object> filters = new ArrayList<>();

        String query = getQuery(tenant,
                                application,
                                deviceGuid,
                                channel,
                                startInstant,
                                endInstant,
                                ascending,
                                limit,
                                type,
                                table,
                                filters);

        PreparedStatement ps = selectStatementCache.get(query);
        if (ps == null) {
            ps = session.prepare(query);
            selectStatementCache.put(query, ps);
        }
        BoundStatement statement = ps.bind(filters.toArray(new Object[filters.size()]));

        final ResultSet rs = session.execute(statement);

        List<Event> events = new LinkedList<>();

        while (!rs.isExhausted()) {
            final Row row = rs.one();

            EventActor outgoingActor = null;
            EventActor incomingActor = null;

            if (type == Type.INCOMING) {

                incomingActor = EventActor.builder()
                                          .tenantDomain(row.getString("tenant_domain"))
                                          .applicationName(row.getString("application_name"))
                                          .deviceGuid(row.getString("device_guid"))
                                          .deviceId(row.getString("device_id"))
                                          .channel(row.getString("channel"))
                                          .build();

            } else if (type == Type.OUTGOING) {

                outgoingActor = EventActor.builder()
                        .tenantDomain(row.getString("tenant_domain"))
                        .applicationName(row.getString("application_name"))
                        .deviceGuid(row.getString("device_guid"))
                        .deviceId(row.getString("device_id"))
                        .channel(row.getString("channel"))
                        .build();

                incomingActor = EventActor.builder()
                        .tenantDomain(row.getString("tenant_domain"))
                        .applicationName(row.getString("application_name"))
                        .deviceGuid(row.getString("incoming_device_guid"))
                        .deviceId(row.getString("incoming_device_id"))
                        .channel(row.getString("incoming_channel"))
                        .build();

            }

            Event event = Event.builder()
                               .epochTime(row.getLong("timestamp"))
                               .timestamp(Instant.ofEpochMilli(row.getLong("timestamp") / 1000000))
                               .incoming(incomingActor)
                               .outgoing(outgoingActor)
                               .geolocation(EventGeolocation.builder()
                            		   .lat(row.getDouble("geo_lat"))
                            		   .lon(row.getDouble("geo_lon"))
                            		   .hdop(row.getLong("geo_hdop"))
                            		   .elev(row.getDouble("geo_elev"))
                            		   .build())
                               .payload(row.getString("payload"))
                               .build();

            events.add(event);
        }

        return events;

    }

    private String getQuery(Tenant tenant, Application application, String deviceGuid, String channel,
            Instant startInstant, Instant endInstant, boolean ascending, Integer limit, Type type,
            String table, List<Object> filters) {

        StringBuilder query = new StringBuilder();

        if (type == Type.INCOMING) {
            query.append("SELECT tenant_domain, application_name, timestamp, channel, device_guid, device_id, geo_elev, geo_hdop, geo_lat, geo_lon, payload FROM ");

            if (deviceGuid != null && channel != null) {
                table = INCOMING_EVENTS_DEVICE_GUID_CHANNEL;
            } else if (deviceGuid != null && channel == null) {
                table = INCOMING_EVENTS_DEVICE_GUID;
            } else if (deviceGuid == null && channel != null) {
                table = INCOMING_EVENTS_CHANNEL;
            } else if (deviceGuid == null && channel == null) {
                table = INCOMING_EVENTS;
            }
        } else if (type == Type.OUTGOING) {
            query.append("SELECT tenant_domain, application_name, timestamp, channel, device_guid, device_id, geo_elev, geo_hdop, geo_lat, geo_lon, incoming_channel, incoming_device_guid, incoming_device_id, payload FROM ");

            if (deviceGuid != null && channel != null) {
                table = OUTGOING_EVENTS_DEVICE_GUID_CHANNEL;
            } else if (deviceGuid != null && channel == null) {
                table = OUTGOING_EVENTS_DEVICE_GUID;
            } else if (deviceGuid == null && channel != null) {
                table = OUTGOING_EVENTS_CHANNEL;
            } else if (deviceGuid == null && channel == null) {
                table = OUTGOING_EVENTS;
            }
        }

        query.append(config.getKeyspace());
        query.append(".");
        query.append(table);
        query.append(" WHERE ");

        query.append(" tenant_domain = ?");
        filters.add(tenant.getDomainName());

        query.append(" AND application_name = ?");
        filters.add(application.getName());

        if (deviceGuid != null) {
            query.append(" AND device_guid = ?");
            filters.add(deviceGuid);
        }

        if (channel != null) {
            query.append(" AND channel = ?");
            filters.add(channel);
        }

        if (startInstant != null) {
            query.append(" AND timestamp > ?");
            filters.add(startInstant.toEpochMilli() * 1000000);
        }

        if (endInstant != null) {
            query.append(" AND timestamp <= ?");
            filters.add(endInstant.toEpochMilli() * 1000000);
        }

        if (ascending) {
            query.append(" ORDER BY timestamp ASC");
        } else {
            query.append(" ORDER BY timestamp DESC");
        }

        if (limit != null) {
            query.append(" LIMIT ");
            query.append(limit);
        }

        return query.toString();

    }

    @Override
    protected void doRemoveBy(Tenant tenant, Application application, String deviceGuid, Type type) throws Exception {

        List<Event> keys = doFindBy(tenant, application, deviceGuid, null, null, null, false, null, type, false);

        Set<String> channels = new HashSet<>();

        for (Event key: keys) {
            if (type == Type.INCOMING) {
                channels.add(key.getIncoming().getChannel());
                removeFromTableByKey(key, type);
                saveEvent(tenant, application, key, type, INCOMING_EVENTS_DELETED, false);
            } else if (type == Type.OUTGOING) {
                channels.add(key.getOutgoing().getChannel());
                removeFromTableByKey(key, type);
                saveEvent(tenant, application, key, type, OUTGOING_EVENTS_DELETED, false);
            }
        }

        removeFromGuidTable(tenant, application, deviceGuid, type);
        removeFromGuidChannelTable(tenant, application, deviceGuid, type, channels);

    }

    private void removeFromGuidChannelTable(Tenant tenant, Application application, String deviceGuid, Type type, Set<String> channels) {

        for (String channel: channels) {

            String tenantDomain = tenant.getDomainName();
            String applicationName = application.getName();

            // INCOMING_EVENTS_DEVICE_GUID

            StringBuilder query = new StringBuilder();
            List<Object> filters = new ArrayList<>();

            query.append("DELETE FROM ");
            query.append(config.getKeyspace());
            query.append(".");
            if (type == Type.INCOMING) {
                query.append(INCOMING_EVENTS_DEVICE_GUID_CHANNEL);
            } else if (type == Type.OUTGOING) {
                query.append(OUTGOING_EVENTS_DEVICE_GUID_CHANNEL);
            }
            query.append(" WHERE ");

            query.append(" tenant_domain = ?");
            filters.add(tenantDomain);

            query.append(" AND application_name = ?");
            filters.add(applicationName);

            query.append(" AND device_guid = ?");
            filters.add(deviceGuid);

            query.append(" AND channel = ?");
            filters.add(channel);

            session.execute(query.toString(), filters.toArray(new Object[filters.size()]));

        }

    }

    private void removeFromGuidTable(Tenant tenant, Application application, String deviceGuid, Type type) {

        String tenantDomain = tenant.getDomainName();
        String applicationName = application.getName();

        // INCOMING_EVENTS_DEVICE_GUID

        StringBuilder query = new StringBuilder();
        List<Object> filters = new ArrayList<>();

        query.append("DELETE FROM ");
        query.append(config.getKeyspace());
        query.append(".");
        if (type == Type.INCOMING) {
            query.append(INCOMING_EVENTS_DEVICE_GUID);
        } else if (type == Type.OUTGOING) {
            query.append(OUTGOING_EVENTS_DEVICE_GUID);
        }
        query.append(" WHERE ");

        query.append(" tenant_domain = ?");
        filters.add(tenantDomain);

        query.append(" AND application_name = ?");
        filters.add(applicationName);

        query.append(" AND device_guid = ?");
        filters.add(deviceGuid);

        session.execute(query.toString(), filters.toArray(new Object[filters.size()]));

    }

    private void removeFromTableByKey(Event key, Type type) {

        String tenantDomain = key.getIncoming().getTenantDomain();
        String applicationName = key.getIncoming().getApplicationName();
        String channel = key.getIncoming().getChannel();
        Long epochTs = key.getEpochTime();

        // remove from tables: INCOMING_EVENTS or OUTGOING_EVENTS

        StringBuilder query = new StringBuilder();
        List<Object> filters = new ArrayList<>();

        query.append("DELETE FROM ");
        query.append(config.getKeyspace());
        query.append(".");
        if (type == Type.INCOMING) {
            query.append(INCOMING_EVENTS);
        } else if (type == Type.OUTGOING) {
            query.append(OUTGOING_EVENTS);
        }
        query.append(" WHERE ");

        query.append(" tenant_domain = ?");
        filters.add(tenantDomain);

        query.append(" AND application_name = ?");
        filters.add(applicationName);

        query.append(" AND timestamp = ?");
        filters.add(epochTs);

        session.executeAsync(query.toString(), filters.toArray(new Object[filters.size()]));

        // remove from tables: INCOMING_EVENTS_CHANNEL or OUTGOING_EVENTS_CHANNEL

        query = new StringBuilder();
        filters = new ArrayList<>();

        query.append("DELETE FROM ");
        query.append(config.getKeyspace());
        query.append(".");
        if (type == Type.INCOMING) {
            query.append(INCOMING_EVENTS_CHANNEL);
        } else if (type == Type.OUTGOING) {
            query.append(OUTGOING_EVENTS_CHANNEL);
        }
        query.append(" WHERE ");

        query.append(" tenant_domain = ?");
        filters.add(tenantDomain);

        query.append(" AND application_name = ?");
        filters.add(applicationName);

        query.append(" AND channel = ?");
        filters.add(channel);

        query.append(" AND timestamp = ?");
        filters.add(epochTs);

        session.executeAsync(query.toString(), filters.toArray(new Object[filters.size()]));

    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void destroy() throws Exception {
        if (cluster != null) {
            cluster.close();
        }
    }

}


