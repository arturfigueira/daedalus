package com.github.daedalus.core.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Document with its data organized in properties
 */
public class Document {
    @Getter
    protected final String nodeId;
    protected final Map<String, Object> properties = new HashMap<>();

    /**
     * Creates a new instance of a Document.
     * @param nodeId unique identifier of this node
     * @param properties map containing all the properties of this document
     */
    public Document(final String nodeId, Map<String, Object> properties) {
        this.nodeId = nodeId;
        this.properties.putAll(properties);
    }

    /**
     * Returns a Map containing all properties of this document.
     * @return a map view of all properties of this document
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
}
