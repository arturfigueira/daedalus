package com.daedalus.core.process;

import com.daedalus.core.data.DataParser;
import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.Document;

import java.util.*;

/**
 * Schema is an abstraction of an elastic search index properties`s mapping. This contains methods
 * to validate if a {@link Document} is following the index data structure.
 */
class Schema {

  private final Map<String, DataMapping> mappings = new HashMap<>();
  private final DataParser dataParser;

  public Schema(final List<DataMapping> mappings, final DataParser dataParser) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException("List of mapping can't be null nor empty");
    }
    final Set<DataMapping> mappingsSet = new HashSet<>(mappings);
    mappingsSet.forEach(dataMapping -> this.mappings.put(dataMapping.getName(), dataMapping));

    this.dataParser =
        Optional.ofNullable(dataParser)
            .orElseThrow(() -> new IllegalArgumentException("Data parser cant be null"));
  }

  /**
   * Validates if given {@link Document} is structured in conformity of this schema's {@link
   * Schema#mappings}. All properties defined at this schema's mappings is required to be presented
   * at the document. The datatype is validated as well.
   * <p>
   * Properties, found at the document, that are not mapped in the current schema, will be ignored
   * and won't generate any exceptions.
   * <p>
   * If the document is in conformity nothing will happens, but a {@link SchemaException} will be
   * thrown otherwise, listing all non conformity's of the document.
   *
   * @param document Document to be validated
   * @throws SchemaException if there is an non conformity with this document
   */
  public void convict(final Document document) throws SchemaException {
    final List<String> errors = new LinkedList<>();
    var properties = document.getProperties();

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
              if (!dataParser.isA(data, type)) {
                errors.add(
                    "Data assigned to "
                        + property
                        + " cannot be mapped to a "
                        + type.name().toLowerCase());
              }
            });

    if (!errors.isEmpty()) {
      throw new SchemaException(errors);
    }
  }
}
