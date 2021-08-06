package com.daedalus.core.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Document with all of its data organized in properties
 */
public class DataNode {
    @Getter
    protected final String nodeId;
    protected final Map<String, Object> properties = new HashMap<>();

    /**
     * Creates a new instance of a DataNode.
     * @param nodeId unique identifier of this node
     * @param properties map containing all the properties of this node
     */
    public DataNode(final String nodeId, Map<String, Object> properties) {
        this.nodeId = nodeId;
        this.properties.putAll(properties);
    }

    /**
     * Returns a Map containing all properties of this node.
     * @return a map view of all properties of this node
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
}
