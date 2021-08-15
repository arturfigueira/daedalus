package com.github.daedalus.core.elastic;

import com.github.daedalus.core.elastic.data.DataType;
import com.github.daedalus.core.elastic.data.Formatable;
import com.github.daedalus.core.elastic.data.Localizable;
import com.github.daedalus.core.elastic.data.ZoneTimeable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import lombok.NoArgsConstructor;

/**
 * {@code DataParser} is a class for parsing objects in a format suitable for Elastic Search.
 * <p>
 * It provides methods to identify and parse objects into an elastic search data type structure.
 * Parsing to datatype that requires localization, timezones, and string pattern is also supported.
 * <p>
 * Available data types, available to the parse, are dictated by the items enumerated at {@link
 * ElasticDataType} class.
 * <p>
 * A static {@link Builder} is available to facilitate the creation of new instances of this
 * parser.
 */
public class DataParser {

  private static final String TYPE_SHOULD_BE_PROVIDED = "A data type should be provided";
  private final HashMap<String, DataType<?>> dataTypes = new HashMap<>();

  DataParser(final Locale locale, final TimeZone timeZone, final String dateFormat) {
    for (var datatype : ElasticDataType.values()) {
      final var type = datatype.produce();
      if (type instanceof Localizable) {
        ((Localizable) type).setLocale(locale);
      }
      if (type instanceof Formatable) {
        ((Formatable) type).setFormat(dateFormat);
      }
      if (type instanceof ZoneTimeable) {
        ((ZoneTimeable) type).setTimeZone(timeZone);
      }
      dataTypes.put(datatype.name(), type);
    }
  }

  /**
   * Verifies if given object can be considered to be of the specified {@link ElasticDataType}
   *
   * @param object   a instance of object to be validated against the dataType
   * @param dataType target ElasticDataType
   * @return {@code true} if given object can be considered to be of specified datatype, {@code
   * false} otherwise
   * @throws IllegalArgumentException if specified data type is null
   */
  public boolean isA(final Object object, final ElasticDataType dataType) {
    if (dataType == null) {
      throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    }
    return this.dataTypes.get(dataType.name()).isA(object);
  }

  /**
   * Parses the given object to the specified {@link ElasticDataType}.
   *
   * @param input      object to be parsed
   * @param outputType desired elastic data type in which input should be parsed
   * @return An object parsed according to the specified {@link ElasticDataType}.
   * @throws IncorrectTypeException   If the parsing can't be performed.
   * @throws IllegalArgumentException if data type is null
   */
  public Object parse(final Object input, final ElasticDataType outputType)
      throws IncorrectTypeException {
    if (outputType == null) {
      throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    }
    return this.parse(input, outputType.name());
  }

  /**
   * Parses the given object to the specified {@link ElasticDataType}.
   *
   * @param input    object to be parsed
   * @param typeName unique name of the desired elastic data type in which input should be parsed
   * @return An object parsed according to the specified {@link ElasticDataType}.
   * @throws IncorrectTypeException   If the parsing can't be performed.
   * @throws IllegalArgumentException if data type is null
   * @throws NullPointerException     if no data type can be found by the given typeName
   */
  public Object parse(final Object input, final String typeName) throws IncorrectTypeException {
    if (typeName == null || typeName.isBlank()) {
      throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    }
    return this.dataTypes.get(typeName).parse(input);
  }

  /**
   * {@link DataParser} companion builder object.
   * <p>
   * By default the builder will create new instances of a {@link DataParser} with the following
   * properties:
   * <ul>
   *   <li>locale: default locale for this instance of the Java Virtual Machine.</li>
   *   <li> timeZone: default timezone for this instance of the Java Virtual Machine.</li>
   *   <li>dateFormat: {@code "yyyy-MM-dd HH:mm:ss"}</li>
   * </ul>
   * <p>
   * Those values can be overridden via the available methods of this builder object.
   */
  @NoArgsConstructor
  public static class Builder {

    private Locale locale = Locale.getDefault();
    private TimeZone timeZone = TimeZone.getDefault();
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * Specifies a new timeZone
     *
     * @param timeZone the timeZone whose parsing should use
     * @return this builder instance
     * @throws IllegalArgumentException if timezone is null
     */
    public Builder timeZone(TimeZone timeZone) {
      this.timeZone =
          Optional.ofNullable(timeZone)
              .orElseThrow(() -> new IllegalArgumentException("TimeZone can`t be null"));
      return this;
    }

    /**
     * Specifies a new Locale
     *
     * @param locale the locale whose parsing should use
     * @return this builder instance
     * @throws IllegalArgumentException if locale is null
     */
    public Builder locale(Locale locale) {
      this.locale =
          Optional.ofNullable(locale)
              .orElseThrow(() -> new IllegalArgumentException("Locale can`t be null"));
      return this;
    }

    /**
     * Specifies a new date format pattern
     *
     * @param dateFormat the pattern describing the date and time format
     * @return this builder instance
     * @throws IllegalArgumentException if dateFormat is null or blank
     */
    public Builder dateFormat(String dateFormat) {
      this.dateFormat =
          Optional.ofNullable(dateFormat)
              .filter(s -> !s.isBlank())
              .orElseThrow(() -> new IllegalArgumentException("Locale can`t be null"));
      return this;
    }

    /**
     * Creates a new instance of {@link DataParser} configured with the specified builder`s
     * properties
     *
     * @return a new {@link DataParser} instance
     */
    public DataParser create() {
      return new DataParser(this.locale, this.timeZone, this.dateFormat);
    }
  }
}
