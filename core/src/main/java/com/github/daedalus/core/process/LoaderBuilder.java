package com.github.daedalus.core.process;

import com.github.daedalus.core.data.DataMapping;
import com.github.daedalus.core.data.DataParser;
import com.github.daedalus.core.process.client.ElasticClient;
import com.github.daedalus.core.stream.DataStore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import lombok.NoArgsConstructor;

/**
 * Returns a new {@link LoaderBuilder} instance to facilitate construction of new  {@link Loader}.
 * {@link Loader} has no public constructor, so this is the preferred way to create it.
 */
@NoArgsConstructor
public class LoaderBuilder {

  protected ElasticClient client;
  protected DataStore backup = null;
  protected List<DataMapping> mappings;
  protected final DataParser.Builder dataParserBuilder = new DataParser.Builder();

  /**
   * Sets the {@link ElasticClient} client that will be used throughout the execution of the loader
   *
   * @param client Required.
   * @throws IllegalArgumentException if the client is null
   */
  public LoaderBuilder elasticClient(final ElasticClient client) {
    this.client =
        Optional.ofNullable(client)
            .orElseThrow(() -> new IllegalArgumentException("Client must not be null"));
    return this;
  }

  /**
   * Sets a {@link DataStore} which will handle the backup of all data, prior sending it to elastic
   * search If this is not set, no backup will happen during the indexing.
   * <p>
   * The backup will happen between the data transformation and sending it to be indexed. Useful to
   * check how the data was indexed or to redo the indexing.
   *
   * @throws IllegalArgumentException if the argument is null
   */
  public LoaderBuilder backupTo(final DataStore backup) {
    this.backup =
        Optional.ofNullable(backup)
            .orElseThrow(() -> new IllegalArgumentException("backup must not be null"));
    return this;
  }

  /**
   * Sets the {@link Locale} for any transformation that occurs and must be according to a locale.
   * This includes Date parsing which is widely influenced by the locale.
   * <p>
   * The default value will be calculated based on the JVM {@code Locale.getDefault()}
   *
   * @throws IllegalArgumentException if the argument is null
   */
  public LoaderBuilder locale(final Locale locale) {
    if (locale == null) {
      throw new IllegalArgumentException("locale must not be null");
    }
    dataParserBuilder.locale(locale);
    return this;
  }

  /**
   * Sets the {@link TimeZone} for any transformation that occurs and must be according to a
   * timezone. This is important for Date parsing which can result in undesired results when using
   * incorrect timezones
   * <p>
   * The default value will be calculated based on the JVM {@code TimeZone.getDefault()}}
   *
   * @throws IllegalArgumentException if the argument is null
   */
  public LoaderBuilder timeZone(final TimeZone timeZone) {
    if (timeZone == null) {
      throw new IllegalArgumentException("timeZone must not be null");
    }
    dataParserBuilder.timeZone(timeZone);
    return this;
  }

  /**
   * Sets the date pattern format for any date parsing that occurs. The specified pattern should
   * follow the {@link SimpleDateFormat} date and time patterns, refer to its documentation for more
   * information
   * <p>
   * The default format is {@code "yyyy-MM-dd HH:mm:ss"}.
   *
   * @throws IllegalArgumentException if the pattern is null or blank
   */
  public LoaderBuilder dateFormatPattern(final String datePattern) {
    if (datePattern == null || datePattern.isBlank()) {
      throw new IllegalArgumentException("datePattern must not be null nor empty");
    }
    dataParserBuilder.dateFormat(datePattern);
    return this;
  }

  /**
   * Sets the index mapping properties structure. It is basically a list of tuples, containing the
   * property name and its data type, defining how the elastic search index is structured This is a
   * required configuration that must be provided before building a new {@link Loader}
   * <p>
   * Using the index mappings below as reference:
   * <pre>{@code
   *        {
   *          "mappings" : {
   *            "properties" : {
   *              "title":{
   *                "type": "completion"
   *              },
   *              "style":{
   *                "type: "keyword"
   *              }
   *           }
   *        }
   *      }
   *   }
   * </pre>
   * <p>
   * A list of {@link DataMapping} should be provided as follows:
   * <pre>{@code
   *        var mappings = new ArrayList<DataMapping>();
   *        mappings.add(new DataMapping("title", ElasticDataType.COMPLETION));
   *        mappings.add(new DataMapping("style", ElasticDataType.KEYWORD));
   *    }
   * </pre>
   *
   * @throws IllegalArgumentException if list of mapping is null or empty
   */
  public LoaderBuilder mappings(final List<DataMapping> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException(
          "Mappings must is required and must contain at least one item");
    }
    this.mappings = mappings;
    return this;
  }

  /**
   * Returns a new instance of {@link Loader} configured with the parameters set by this builder
   *
   * @throws IllegalArgumentException if not mappings were provided
   */
  public Loader build() {
    final var reshaper = new Reshaper(mappings, dataParserBuilder.create());
    return new Loader(client, backup, reshaper);
  }
}
