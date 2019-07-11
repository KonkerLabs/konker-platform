package com.konkerlabs.platform.registry.business.model;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

@Data
@Builder
@Document
@CompoundIndexes({
        @CompoundIndex(name = "device_channel_idx", def = "{'deviceGuid': 1, 'channel': 1}", unique = true)
})
public class EventSchema implements URIDealer, Serializable {

    @Id
    private String id;
    @Indexed
    private String deviceGuid;
    private String channel;
    @Singular
    private Set<SchemaField> fields = new HashSet<>();

    public static final String URI_SCHEME = "eventschema";


    public URI toURI() {
        return URI.create(
                getRoutUriTemplate()
        );
    }

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return (deviceGuid + '_' + channel).toLowerCase();
    }

    @Override
    public String getGuid() {
        return id;
    }

    public Optional<SchemaField> getByPath(String path) {
        return getFields().stream().filter(schemaField -> schemaField.getPath().equals(path)).findFirst();
    }

    public void upsertTypeFor(String path, JsonParsingService.JsonPathData pathData) {
        Optional<SchemaField> field = getByPath(path);

        if (field.isPresent())
            field.get().getKnownTypes().add(pathData.getTypes().get(pathData.getTypes().size()-1));
        else
            getFields().add(SchemaField.builder().path(path).knownTypes(new HashSet<>(pathData.getTypes())).build());
    }

    @Data
    @EqualsAndHashCode(of = "path")
    @Builder
    public static class SchemaField implements Serializable {
        private String path;
        @Singular
        private Set<JsonNodeType> knownTypes;
    }
}