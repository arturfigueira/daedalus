package com.github.daedalus.core.data;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a mapping between a property and its {@link ElasticDataType}.
 * <p>
 * For properties that the input name differs from the output name, the static method {@link
 * #withOutputName(String, String, ElasticDataType)} is available. For the cases where both input
 * and output names are equal, use the default constructor {@link #DataMapping(String,
 * ElasticDataType)}
 */
@ToString
@EqualsAndHashCode
@Getter
public class DataMapping {

  private final String name;
  private final String outputName;
  private final ElasticDataType type;

  DataMapping(String name, String outputName, ElasticDataType type) {
    this.name = Optional.ofNullable(name).filter(s -> !s.isBlank())
        .orElseThrow(() -> new IllegalArgumentException("Invalid name"));

    this.outputName = Optional.ofNullable(outputName).filter(s -> !s.isBlank())
        .orElseThrow(() -> new IllegalArgumentException("Invalid outputName Name"));

    this.type = Optional.ofNullable(type)
        .orElseThrow(() -> new IllegalArgumentException("Invalid type"));
  }

  /**
   * Constructs a new instance where the outputName equals the name
   *
   * @param name property name
   * @param type mapping data type
   * @throws IllegalArgumentException if the name is null or blank or type is null
   */
  public DataMapping(String name, ElasticDataType type) {
    this(name, name, type);
  }

  /**
   * Constructs a new instance with an outputName that differs from the name.
   *
   * @param name       property name
   * @param outputName output property name
   * @param type       mapping data type
   * @throws IllegalArgumentException if the name/outputName is null or blank, or type is null
   */
  public static DataMapping withOutputName(String name, String outputName,
      ElasticDataType type) {
    return new DataMapping(name, outputName, type);
  }
}
