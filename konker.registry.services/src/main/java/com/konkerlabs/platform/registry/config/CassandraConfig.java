package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.converters.InstantReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.InstantWriteConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIWriteConverter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.*;


public class CassandraConfig extends AbstractCassandraConfiguration {


    private String clusterName;
    private String keyspace;
    private String seedHost;
    private int seedPort;

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

    @Override
    public CassandraAdminOperations cassandraTemplate() throws Exception {

        return super.cassandraTemplate();
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

    public static final List<Converter<?,?>> converters = Arrays.asList(
            new Converter[] {
                    new InstantReadConverter(),
                    new InstantWriteConverter(),
                    new URIReadConverter(),
                    new URIWriteConverter()
            }
    );

    @Override
    @Bean(name = "cassandraSession")
    public CassandraSessionFactoryBean session() throws ClassNotFoundException {
        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(this.cluster().getObject());
        session.setConverter(this.cassandraConverter());
        session.setKeyspaceName(this.getKeyspaceName());
        session.setSchemaAction(this.getSchemaAction());
        session.setStartupScripts(this.getStartupScripts());
        session.setShutdownScripts(this.getShutdownScripts());
        return session;
    }

    @Override
    @Bean(name = "cassandraConverter")
    public CassandraConverter cassandraConverter() throws ClassNotFoundException {
        MappingCassandraConverter mappingCassandraConverter =
                new MappingCassandraConverter(this.cassandraMapping());
        mappingCassandraConverter.setCustomConversions(this.customConversions());
        return mappingCassandraConverter;
    }
}
