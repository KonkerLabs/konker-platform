package com.konkerlabs.platform.registry.business.repositories.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class CassandraRegistryConfig {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String keyspace;
    private String seedHost;
    private String username;
    private String password;
    private int seedPort;

    @Bean
    public Cluster cluster() {

        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("cassandra.keyspace", "registrykeyspace");
        defaultMap.put("cassandra.hostname", "localhost");
        defaultMap.put("cassandra.port", 9042);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        try {
            Config config = ConfigFactory.load().withFallback(defaultConf);
            setKeyspace(config.getString("cassandra.keyspace"));
            setSeedHost(config.getString("cassandra.hostname"));
            setSeedPort(config.getInt("cassandra.port"));
            setUsername(config.getString("cassandra.username"));
            setPassword(config.getString("cassandra.password"));
        } catch (Exception e) {
            LOGGER.warn(String.format("Cassandra is not configured, using default cassandra config\n" +
                            "cassandra.keyspace: {1\n" +
                            "cassandra.hostname: {}\n" +
                            "cassandra.port: {}\n",
                    getKeyspace(), getSeedHost(), getSeedPort())
            );
        }

        Cluster cluster = null;

        if (StringUtils.hasText(getUsername())) {
            cluster = Cluster.builder()
                             .addContactPoint(getSeedHost())
                             .withPort(getSeedPort())
                             .withCredentials(getUsername(), getPassword())
                             .build();
        } else {
            cluster = Cluster.builder()
                            .addContactPoint(getSeedHost())
                            .withPort(getSeedPort())
                            .build();
        }

        return cluster;

    }

    @Bean
    public Session session() {

        try {

            final Cluster cluster = cluster();
            final Metadata metadata = cluster().getMetadata();

            LOGGER.info("Connected to cluster: {}\n", metadata.getClusterName());

            for (final Host host : metadata.getAllHosts()) {
                LOGGER.info("Datacenter: {}; Host: {}; Rack: {}\n", host.getDatacenter(), host.getAddress(),
                        host.getRack());
            }

            return cluster.connect(getKeyspace());

        } catch (NoHostAvailableException e) {
            LOGGER.info("NoHostAvailableException: {}", e.getMessage());

            return null;
        }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
