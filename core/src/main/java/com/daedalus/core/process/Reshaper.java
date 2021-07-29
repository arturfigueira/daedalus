package com.daedalus.core.process;

import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.DataNode;
import com.daedalus.core.data.IncorrectTypeException;

import java.util.*;

class Reshaper {

  private final Map<String, DataMapping> mappings = new HashMap<>();

  public Reshaper(List<DataMapping> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException("List of mapping can't be null nor empty");
    }
    final Set<DataMapping> mappingsSet = new HashSet<>(mappings);
    mappingsSet.forEach(dataMapping -> this.mappings.put(dataMapping.getName(), dataMapping));
  }

  public List<Map<String, Object>> reshape(final List<DataNode> inputNodes)
      throws SchemaException, IncorrectTypeException {
    if (inputNodes == null) {
      throw new IllegalArgumentException("Nodes to be reshaped can't be null");
    }

    final ArrayList<Map<String, Object>> outputNodes = new ArrayList<>();
    for (var node : inputNodes) {
      final var reshapedProps = this.propertiesReshape(node.getProperties());
      outputNodes.add(reshapedProps);
    }

    return outputNodes;
  }

  private Map<String, Object> propertiesReshape(final Map<String, Object> rawProperties)
      throws SchemaException, IncorrectTypeException {
    final Map<String, Object> reshapedMap = new HashMap<>();
    for (var entry : mappings.entrySet()) {
      final var propertyKey = entry.getKey();
      var value = Optional
          .ofNullable(rawProperties.get(propertyKey))
          .orElseThrow(
                  () -> new SchemaException("A value for " + propertyKey + " could not be found")
          );

      final var dataType = entry.getValue().getType();
      reshapedMap.put(propertyKey, dataType.parse(value));
    }

    return reshapedMap;
  }
}
