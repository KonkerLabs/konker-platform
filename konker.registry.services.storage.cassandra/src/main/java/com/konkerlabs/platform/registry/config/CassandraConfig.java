package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
public class CassandraConfig {

    protected String clusterName;
    protected String keyspace;
    protected String seedHost;
    protected int seedPort;

    protected EventStorageConfig eventStorageConfig = new EventStorageConfig();
    private static final Logger LOG = LoggerFactory.getLogger(CassandraConfig.class);

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getSeedHost() {
        return seedHost;
    }

    public void setSeedHost(String seedHost) {
        this.seedHost = seedHost;
    }

    public int getSeedPort() {
        return seedPort;
    }

    public void setSeedPort(int seedPort) {
        this.seedPort = seedPort;
    }

    public CassandraConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("cassandra.clustername", "konker");
        defaultMap.put("cassandra.keyspace", "registrykeyspace");
        defaultMap.put("cassandra.hostname", "localhost");
        defaultMap.put("cassandra.port", 9042);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        try {
            Config config = ConfigFactory.load().withFallback(defaultConf);
            setClusterName(config.getString("cassandra.clustername"));
            setKeyspace(config.getString("cassandra.keyspace"));
            setSeedHost(config.getString("cassandra.hostname"));
            setSeedPort(config.getInt("cassandra.port"));
        } catch (Exception e) {
            LOG.warn(String.format("Cassandra is not configured, using default cassandra config\n" +
                            "cassandra.clustername: {0}\n" +
                            "cassandra.keyspace: {1}\n" +
                            "cassandra.hostname: {2}\n" +
                            "cassandra.port: {3}\n",
                    getClusterName(), getKeyspace(), getSeedHost(), getSeedPort())
            );
        }

    }


    protected String getKeyspaceName() {
        return keyspace;
    }

}
