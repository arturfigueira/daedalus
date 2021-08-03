package com.daedalus.core.process;

import com.daedalus.core.data.DataParser;
import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.DataNode;
import com.daedalus.core.data.IncorrectTypeException;

import com.daedalus.core.stream.DataSource;
import java.util.*;

/**
 * An object responsible to parse {@link DataNode}  into a map of properties / values, based on an
 * elastic search list of {@link DataMapping}.
 * <p>
 * Its main usage is while reading an unformatted data from a {@link DataSource} , parsing it into
 * an indexable data structure.
 */
class Reshaper {

  private final Map<String, DataMapping> mappings = new HashMap<>();
  private final DataParser dataParser;

  public Reshaper(List<DataMapping> mappings, final DataParser dataParser) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException("List of mapping can't be null nor empty");
    }
    final Set<DataMapping> mappingsSet = new HashSet<>(mappings);
    mappingsSet.forEach(dataMapping -> this.mappings.put(dataMapping.getName(), dataMapping));

    this.dataParser =
        Optional.ofNullable(dataParser)
            .orElseThrow(() -> new IllegalArgumentException("A data parser should be provide"));
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
      final var fieldKey = entry.getKey();
      var value =
          Optional.ofNullable(rawProperties.get(fieldKey))
              .orElseThrow(
                  () -> new SchemaException("A value for " + fieldKey + " could not be found"));

      final var parsedValue = dataParser.parse(value, entry.getValue().getType());
      reshapedMap.put(fieldKey, parsedValue);
    }

    return reshapedMap;
  }
}
