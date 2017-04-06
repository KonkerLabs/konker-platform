package com.konkerlabs.platform.registry.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;

import lombok.Data;

@Data
public class EventsFilter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String deviceGuid;
    private String channel;

    public EventsFilter(String query) throws BadRequestResponseException {

        if (!StringUtils.hasText(query)) {
            return;
        }

        String filters[] = query.split("\\+");

        for (String filter : filters) {
            parseToken(filter);
        }

    }

    private void parseToken(String filter) throws BadRequestResponseException {

        String tokens[] = filter.split(":");

        if (tokens.length != 2) {
            throw new BadRequestResponseException("Invalid filter: " + filter);
        }

        if (tokens[0].equalsIgnoreCase("device")) {
            this.deviceGuid = tokens[1];
        } else if (tokens[0].equalsIgnoreCase("channel")) {
            this.channel = tokens[1];
        } else {
            throw new BadRequestResponseException("Not supported filter: " + tokens[0]);
        }

    }

}
