package org.konker.registry.cassandraetl.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
public class EqualizeConfig {

    private String tenant;
    private String application;
    private String device;
    private String location;
    private String channel;
    private Long timestampStart;
    private Long timestampEnd;

    public EqualizeConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("tenant", null);
        defaultMap.put("application", null);
        defaultMap.put("device", null);
        defaultMap.put("location", null);
        defaultMap.put("channel", null);
        defaultMap.put("timestampStart", null);
        defaultMap.put("timestampEnd", null);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        Config config = ConfigFactory.load().withFallback(defaultConf);
        setTenant(config.getString("tenant"));
        setApplication(config.getString("application"));
        setDevice(config.getString("device"));
        setLocation(config.getString("location"));
        setChannel(config.getString("channel"));
        setTimestampStart(config.getLong("timestampStart"));
        setTimestampEnd(config.getLong("timestampEnd"));
    }
}
