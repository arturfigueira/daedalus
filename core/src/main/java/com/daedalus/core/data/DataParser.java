package com.daedalus.core.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import lombok.NoArgsConstructor;

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

  public boolean isA(final Object object, final ElasticDataType dataType) {
    if (dataType == null) throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    return this.dataTypes.get(dataType.name()).isA(object);
  }

  public Object parse(final Object input, final ElasticDataType outputType)
      throws IncorrectTypeException {
    if (outputType == null) throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    return this.parse(input, outputType.name());
  }

  public Object parse(final Object input, final String typeName) throws IncorrectTypeException {
    if (typeName == null || typeName.isBlank()) {
      throw new IllegalArgumentException(TYPE_SHOULD_BE_PROVIDED);
    }
    return this.dataTypes.get(typeName).parse(input);
  }

  @NoArgsConstructor
  public static class Builder {
    private Locale locale = Locale.getDefault();
    private TimeZone timeZone = TimeZone.getDefault();
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public Builder timeZone(TimeZone timeZone) {
      this.timeZone =
          Optional.ofNullable(timeZone)
              .orElseThrow(() -> new IllegalArgumentException("TimeZone can`t be null"));
      return this;
    }

    public Builder locale(Locale locale) {
      this.locale =
          Optional.ofNullable(locale)
              .orElseThrow(() -> new IllegalArgumentException("Locale can`t be null"));
      return this;
    }

    public Builder dateFormat(String dateFormat) {
      this.dateFormat =
          Optional.ofNullable(dateFormat)
              .filter(s -> !s.isBlank())
              .orElseThrow(() -> new IllegalArgumentException("Locale can`t be null"));
      return this;
    }

    public DataParser create() {
      return new DataParser(this.locale, this.timeZone, this.dateFormat);
    }
  }
}
