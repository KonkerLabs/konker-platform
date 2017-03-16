package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {


    private String clusterName;
    private String keyspace;
    private String seedHost;
    private int seedPort;

    public CassandraConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("cassandra.clustername", "konker");
        defaultMap.put("cassandra.keyspace", "koknerevents");
        defaultMap.put("cassandra.hostname", "localhost");
        defaultMap.put("cassandra.port", 9042);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        Config config = ConfigFactory.load().withFallback(defaultConf);
        setClusterName(config.getString("cassandra.clustername"));
        setKeyspace(config.getString("cassandra.keyspace"));
        setSeedHost(config.getString("cassandra.hostname"));
        setSeedPort(config.getInt("cassandra.port"));
    }

    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @Override
    protected String getClusterName() {
        return clusterName;
    }

    @Override
    protected int getPort() {
        return seedPort;
    }

    @Override
    protected String getContactPoints() {
        return seedHost;
    }


}
