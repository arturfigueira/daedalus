package com.daedalus.core.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DataNode {
    @Getter
    protected final String nodeId;
    protected final Map<String, Object> properties = new HashMap<>();

    public DataNode(final String nodeId, Map<String, Object> properties) {
        this.nodeId = nodeId;
        this.properties.putAll(properties);
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
}
