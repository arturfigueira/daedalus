package com.daedalus.core.process;

import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.DataNode;

import java.util.*;

class Schema {
  private final Map<String, DataMapping> mappings = new HashMap<>();

  public Schema(List<DataMapping> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException("List of mapping can't be null nor empty");
    }
    final Set<DataMapping> mappingsSet = new HashSet<>(mappings);
    mappingsSet.forEach(dataMapping -> this.mappings.put(dataMapping.getName(), dataMapping));
  }

  public void convict(final DataNode dataNode) throws SchemaException {
    final List<String> errors = new LinkedList<>();
    var properties = dataNode.getProperties();

    mappings
        .keySet()
        .forEach(
            property -> {
              var data = properties.get(property);
              if (data == null) {
                errors.add("Property " + property + " could not be found at data element");
                return;
              }
              var type = mappings.get(property).getType();
              if (!type.isA(data)) {
                errors.add(
                    "Data assigned to " + property + " cannot be mapped to a " + type.getId());
              }
            });

    if (!errors.isEmpty()) {
      throw new SchemaException(errors);
    }
  }
}
