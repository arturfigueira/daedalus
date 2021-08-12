package com.daedalus.core.process;

import com.daedalus.core.data.DataParser;
import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.Document;
import com.daedalus.core.data.IncorrectTypeException;

import com.daedalus.core.stream.DataSource;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * An object responsible to parse {@link Document} into a map of properties / values, based on an
 * list of {@link DataMapping}.
 * <p>
 * Its main usage is while reading an unformatted data from a {@link DataSource}, parsing it into an
 * indexable data structure, expected by the defined type/index.
 * <p>
 * If no index is provided, {@link Reshaper} will proceed as if the documents are directly under the
 * index, instead of type. For these cases, where no type is require, use the utility static method
 * {@link #withoutType(List, DataParser)} to create instances of {@link Reshaper}
 */
class Reshaper {

  private final Map<String, DataMapping> mappings = new HashMap<>();
  private final DataParser dataParser;
  private final String indexType;

  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  @Getter
  static class ReshapedData {

    private final String type;
    private final List<Map<String, Object>> documents;

    static ReshapedData withoutType(final List<Map<String, Object>> documents) {
      return new ReshapedData("", documents);
    }
  }

  public static Reshaper withoutType(@NonNull final List<DataMapping> mappings,
      @NonNull final DataParser dataParser) {
    return new Reshaper("", mappings, dataParser);
  }

  public Reshaper(@NonNull final String indexType, @NonNull final List<DataMapping> mappings,
      @NonNull final DataParser dataParser) {
    this.indexType = indexType;
    this.dataParser = dataParser;
    final Set<DataMapping> mappingsSet = new HashSet<>(mappings);
    mappingsSet.forEach(dataMapping -> this.mappings.put(dataMapping.getName(), dataMapping));
  }

  public ReshapedData reshape(final List<Document> documents)
      throws SchemaException, IncorrectTypeException {
    if (documents == null) {
      throw new IllegalArgumentException("Nodes to be reshaped can't be null");
    }

    final ArrayList<Map<String, Object>> outputNodes = new ArrayList<>();
    for (var node : documents) {
      final var reshapedProps = this.propertiesReshape(node.getProperties());
      outputNodes.add(reshapedProps);
    }

    return new ReshapedData(indexType, outputNodes);
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

      var mapping = entry.getValue();
      final var parsedValue = dataParser.parse(value, mapping.getType());

      reshapedMap.put(mapping.getOutputName(), parsedValue);
    }

    return reshapedMap;
  }
}
