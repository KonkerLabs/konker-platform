package org.konker.registry.cassandraetl.repositories;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.config.CassandraRegistryConfig;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryCassandraImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Data
@Primary
@Repository("cassandraEventsEtl")
public class EventRepositoryCassandraEtlImpl extends EventRepositoryCassandraImpl {

    @Autowired
    private CassandraRegistryConfig config;

    @Autowired
    private Cluster cluster;

    @Autowired
    private Session session;

    private Random rnd = new Random(System.nanoTime());

    private Map<String, PreparedStatement> insertIncomingMap = new HashMap<>();

    private Map<String, PreparedStatement> insertOutgoingMap = new HashMap<>();


    public void saveEvent(Tenant tenant, Application application, Event event, String table) {
        PreparedStatement ps = getInsertIncomingPreparedStatement(table);

        BoundStatement statement = ps.bind(tenant.getDomainName(),
                application.getName(),
                event.getEpochTime(),
                event.getIncoming().getChannel(),
                event.getIncoming().getDeviceGuid(),
                event.getIncoming().getDeviceId(),
                event.getIncoming().getLocationGuid(),
                Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getElev() : null,
                Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getHdop() : null,
                Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLat() : null,
                Optional.ofNullable(event.getGeolocation()).isPresent() ? event.getGeolocation().getLon() : null,
                event.getIngestedTimestamp().toEpochMilli() * 1000000, // nanoseconds
                event.getPayload());

        session.execute(statement);
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
            query.append("location_guid, ");
            query.append("geo_elev, ");
            query.append("geo_hdop, ");
            query.append("geo_lat, ");
            query.append("geo_lon, ");
            query.append("ingested_timestamp, ");
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
            query.append("?");
            query.append(")");

            ps = session.prepare(query.toString());
            insertIncomingMap.put(table, ps);

        }

        return ps;

    }

}
