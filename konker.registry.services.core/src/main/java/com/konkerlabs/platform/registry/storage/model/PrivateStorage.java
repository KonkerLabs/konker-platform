package com.konkerlabs.platform.registry.storage.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateStorage {

    private String collectionName;
    private String collectionContent;

}
