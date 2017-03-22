package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.converters.InstantReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.InstantWriteConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIWriteConverter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.config.ClusterBuilderConfigurer;
import org.springframework.cassandra.config.java.AbstractClusterConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.mapping.SimpleUserTypeResolver;

import java.util.*;


@Configuration
public class CassandraConfig
        extends AbstractClusterConfiguration
        implements BeanClassLoaderAware {


    protected String clusterName;
    protected String keyspace;
    protected String seedHost;
    protected int seedPort;
    protected ClassLoader beanClassLoader;
    @Autowired
    protected EventStorageConfig eventStorageConfig;
    private static final Logger LOG = LoggerFactory.getLogger(CassandraConfig.class);


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


    @Bean
    public CassandraAdminOperations cassandraTemplate() throws Exception {
        try{
            if(Optional.ofNullable(this.session()).isPresent() &&
                    Optional.ofNullable(this.cassandraConverter()).isPresent()){
                return new CassandraAdminTemplate(this.session().getObject(), this.cassandraConverter());
            } else {
                return null;
            }
        } catch (Exception e){
            LOG.error("Error construct Cassandra Template", e);
        }

        return null;


    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    public String[] getEntityBasePackages() {
        return new String[]{this.getClass().getPackage().getName()};
    }


    public CassandraOperations template() {
        CassandraTemplate cassandraTemplate = null;
        try {
            cassandraTemplate =
                    new CassandraTemplate(
                            session().getObject(),
                            this.cassandraConverter()
                    );
        } catch (ClassNotFoundException e) {
            LOG.error("Error in cassandraTemplate instance", e);
        }
        return cassandraTemplate;
    }


    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

    @Override
    protected ClusterBuilderConfigurer getClusterBuilderConfigurer() {
        return super.getClusterBuilderConfigurer();
    }

    public SchemaAction getSchemaAction() {
        return SchemaAction.NONE;
    }

    public static final List<Converter<?, ?>> converters = Arrays.asList(
            new Converter[]{
                    new InstantReadConverter(),
                    new InstantWriteConverter(),
                    new URIReadConverter(),
                    new URIWriteConverter()
            }
    );


    @Bean
    public CassandraSessionFactoryBean session() throws ClassNotFoundException {
        CassandraSessionFactoryBean session = null;
        try {
            if (eventStorageConfig.getEventRepositoryBean()
                    .equals(EventStorageConfig.EventStorageConfigType.CASSANDRA.bean()) &&
                    this.cluster() != null && this.cluster().getObject() != null) {
                session = new CassandraSessionFactoryBean();
                session.setCluster(this.cluster().getObject());
                session.setConverter(this.cassandraConverter());
                session.setKeyspaceName(this.getKeyspaceName());
                session.setSchemaAction(this.getSchemaAction());
                session.setStartupScripts(this.getStartupScripts());
                session.setShutdownScripts(this.getShutdownScripts());
            } else {
                LOG.debug("Cassandra is not configured as event storage...");
            }
        } catch (Exception e) {
            LOG.error("Fail trying to create the cassandra session client...", e);
        }

        return session;
    }

    @Bean
    public CassandraConverter cassandraConverter() throws ClassNotFoundException {
        try {
            if (eventStorageConfig.getEventRepositoryBean()
                    .equals(EventStorageConfig.EventStorageConfigType.CASSANDRA.bean())){
                MappingCassandraConverter mappingCassandraConverter =
                        new MappingCassandraConverter(this.cassandraMapping());
                mappingCassandraConverter.setCustomConversions(this.customConversions());
                return mappingCassandraConverter;
            }
        } catch (Exception e){
            LOG.error("Fail trying to create the cassandra converters...", e);
        }

        return null;
    }

    @Bean
    public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {
        try {
            BasicCassandraMappingContext mappingContext = new BasicCassandraMappingContext();
            mappingContext.setBeanClassLoader(this.beanClassLoader);
            mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan(this.getEntityBasePackages()));
            CustomConversions customConversions = this.customConversions();
            mappingContext.setCustomConversions(customConversions);
            mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
            mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(this.cluster().getObject(), this.getKeyspaceName()));
            return mappingContext;
        } catch (Exception e){
            LOG.error("Fail trying to create the cassandra mapping...", e);
        }
        return null;
    }

    @Override
    protected List<String> getStartupScripts() {
        return super.getStartupScripts();
    }
}
