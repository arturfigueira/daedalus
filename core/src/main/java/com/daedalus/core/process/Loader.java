package com.daedalus.core.process;

import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.DataParser;
import com.daedalus.core.data.IncorrectTypeException;
import com.daedalus.core.process.Reshaper.ReshapedData;
import com.daedalus.core.process.client.ElasticClient;
import com.daedalus.core.stream.DataReader;
import com.daedalus.core.stream.DataSource;
import com.daedalus.core.stream.DataStore;
import com.daedalus.core.stream.DataStreamException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Loader is an object with methods to load data to an elastic search index.
 * <p>
 * It does not contains a public constructor and should be created using {@link Builder}, which
 * allows to set all options or just rely on defaults.
 * <p>
 * Data can be indexed directly to an Index or into a Type. This should be decided when creating a
 * new instance of {@link Loader} as this is an immutable configuration.
 * <p>
 * For bulk loading, loader has available a companion class {@link BulkLoader}. This companion is
 * able to read data from an external source, process it, convert it into an indexable format and
 * index it in configurable batches to an elastic search index.
 * <p>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Loader {

  private static final int DEFAULT_MAX_ELEMENTS = 500;

  protected final ElasticClient client;
  protected final DataStore backup;
  protected final Reshaper reshaper;

  /**
   * Returns a new {@link Builder} instance to help with creating a {@link Loader}. Allow setting
   * the basic Loader options.
   */
  @NoArgsConstructor
  public static class Builder {

    protected ElasticClient client;
    protected DataStore backup = null;
    protected String type = null;
    protected List<DataMapping> mappings;
    protected final DataParser.Builder dataParserBuilder = new DataParser.Builder();

    /**
     * Sets the {@link ElasticClient} client that will be used throughout the execution of the
     * loader
     *
     * @param client Required.
     * @throws IllegalArgumentException if the client is null
     */
    public Builder elasticClient(final ElasticClient client) {
      this.client =
          Optional.ofNullable(client)
              .orElseThrow(() -> new IllegalArgumentException("Client must not be null"));
      return this;
    }

    /**
     * Sets a {@link DataStore} which will handle the backup of all data, prior sending it to
     * elastic search If this is not set, no dataStore will happen during the indexing.
     * <p>
     * The dataStore will happen between the data transformation and sending it to be indexed.
     * Useful to check how the data was indexed or to redo the indexing.
     *
     * @throws IllegalArgumentException if the argument is null
     */
    public Builder dataStore(final DataStore dataStore) {
      this.backup =
          Optional.ofNullable(dataStore)
              .orElseThrow(() -> new IllegalArgumentException("dataStore must not be null"));
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
    public Builder locale(final Locale locale) {
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
    public Builder timeZone(final TimeZone timeZone) {
      if (timeZone == null) {
        throw new IllegalArgumentException("timeZone must not be null");
      }
      dataParserBuilder.timeZone(timeZone);
      return this;
    }

    /**
     * Sets the date pattern format for any date parsing that occurs. The specified pattern should
     * follow the {@link SimpleDateFormat} date and time patterns, refer to its documentation for
     * more information
     * <p>
     * The default format is {@code "yyyy-MM-dd HH:mm:ss"}.
     *
     * @throws IllegalArgumentException if the pattern is null or blank
     */
    public Builder dateFormatPattern(final String datePattern) {
      if (datePattern == null || datePattern.isBlank()) {
        throw new IllegalArgumentException("datePattern must not be null nor empty");
      }
      dataParserBuilder.dateFormat(datePattern);
      return this;
    }

    /**
     * Sets the index mapping properties structure. It is basically a list of tuples, containing the
     * property name and its data type, defining how the elastic search index or type is structured.
     * This is a required configuration, and must be provided before building a new {@link Loader}.
     * <p>
     * If no type is specified via {@link #toType(String)} this mappings will be considered as an
     * index property mapping.
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
     * @return this builder instance
     * @throws IllegalArgumentException if list of mapping is null or empty
     */
    public Builder dataMapping(final List<DataMapping> mappings) {
      if (mappings == null || mappings.isEmpty()) {
        throw new IllegalArgumentException(
            "Index Mappings must not be null and must contain at least one item");
      }
      this.mappings = mappings;
      return this;
    }

    /**
     * Choose to index data into an Elastic Search Type. The specified name should be exactly the
     * same name from the index that the data should be indexed.
     * <p>
     * Type's property mappings must specified via {@link #dataMapping(List)}
     *
     * @param type Required name of an existing Type
     * @return this builder instance
     * @throws IllegalArgumentException if the type is null or blank
     */
    public Builder toType(final String type) {
      this.type = Optional.ofNullable(type).filter(s -> !s.isBlank())
          .orElseThrow(() -> new IllegalArgumentException("Index type must not be null nor blank"));
      return this;
    }

    /**
     * Returns a new instance of {@link Loader} configured with the parameters set by this builder
     *
     * @throws IllegalArgumentException if not mappings were provided
     */
    public Loader build() {
      final var dataParser = dataParserBuilder.create();

      final var reshaper = Optional.ofNullable(type)
          .map(t -> new Reshaper(t, mappings, dataParser))
          .orElseGet(() -> Reshaper.withoutType(mappings, dataParser));

      return new Loader(client, backup, reshaper);
    }
  }

  /**
   * {@link Loader} companion class to help dealing with Bulk indexing into an elastic search index.
   * Connections and mappings will be provided by Loader. The companion can be created by {@link
   * Loader#toIndex(String)}.
   * <p>
   * {@link BulkLoader} will paginate over the data, provided by a {@code DataSource}, which is
   * specified by {@link #from(DataSource) from}, parsing it based on the loader's list of {@link
   * DataMapping} and bulk indexing them.
   * <p>
   * As the name indicates the process to index data will happen via batches, which by default is
   * setup to {@value DEFAULT_MAX_ELEMENTS} items per bulk. This can be overwritten through {@link
   * #setMaxElementsPerBulk(int) setMaxElementsPerBulk}, but be aware of the {@link ElasticClient}
   * limitations that is being used and the elastic infrastructure.
   */
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class BulkLoader {

    private final Loader loader;
    private final Bulk bulk;
    private int maxElementsPerBulk = DEFAULT_MAX_ELEMENTS;

    /**
     * Sets the max number of item per bulk request
     *
     * @throws IllegalArgumentException if the number is below or equals to zero
     */
    public void setMaxElementsPerBulk(int maxElementsPerBulk) {
      if (maxElementsPerBulk <= 0) {
        throw new IllegalArgumentException("Bulk max number of elements must be greater than zero");
      }
      this.maxElementsPerBulk = maxElementsPerBulk;
    }

    /**
     * Reads raw data defined by the {@link DataSource}, prepare and index it. If the optional
     * {@link DataStore} is present at Loader, the parsed data will also be stored after each bulk
     * request.
     *
     * @throws DataStreamException      if the raw data can't be read or parsed.
     * @throws IllegalArgumentException if data source is null
     */
    public void from(final DataSource dataSource) throws DataStreamException {
      if (dataSource == null) {
        throw new IllegalArgumentException("Data Source must not be null");
      }

      while (dataSource.hasNext()) {
        final var reader = dataSource.next();
        var criteria = DataReader.Criteria.fromBeginning(this.maxElementsPerBulk);
        var keepReading = true;
        while (keepReading) {
          try {
            final var loadedData = bulkReadAndLoad(reader, criteria);
            keepReading = !loadedData.getDocuments().isEmpty();

          } catch (SchemaException | IncorrectTypeException e) {
            throw new DataStreamException(
                "Source "
                    + reader.getSource()
                    + ", page "
                    + criteria.getPage()
                    + " could not be parse to be loaded",
                e);
          } catch (IOException e) {
            throw new DataStreamException(
                "Source "
                    + reader.getSource()
                    + ", page "
                    + criteria.getPage()
                    + " could not be be loaded into elastic",
                e);
          }

          criteria = criteria.nextPage();
        }
      }
    }

    private ReshapedData bulkReadAndLoad(
        final DataReader reader, final DataReader.Criteria criteria)
        throws DataStreamException, SchemaException, IncorrectTypeException, IOException {
      final var documents = reader.read(criteria);
      final var reshapedData = this.loader.reshaper.reshape(documents);

      if (!reshapedData.getDocuments().isEmpty()) {
        var identifier = reader.getSource() + "_" + criteria.getPage();
        this.bulk.index(identifier, reshapedData);
        Optional.ofNullable(this.loader.backup)
            .ifPresent(ds -> ds
                .store(identifier, Map.of(reshapedData.getType(), reshapedData.getDocuments())));
      }
      return reshapedData;
    }
  }

  /**
   * Creates a new {@link BulkLoader} to process bulk requests to a given index. The number of items
   * per bulk will be the default {@value DEFAULT_MAX_ELEMENTS}.
   * <p>
   * If the number of items per bulk must be different than the default, a similar method is also
   * available. Refer to {@link #toIndex(String, int)}
   *
   * @param index Require. Index name that will receive the bulk request
   */
  public BulkLoader toIndex(final String index) {
    final var bulk = new Bulk(index, this.client);
    return new BulkLoader(this, bulk);
  }

  /**
   * Creates a new {@link BulkLoader} to process bulk requests to a given index.
   *
   * @param index    Required. Index name that will receive the bulk request
   * @param bulkSize Required. Number of items per bulk. Refer to used elastic client for
   *                 limitations over this value
   */
  public BulkLoader toIndex(final String index, final int bulkSize) {
    final var bulkLoader = this.toIndex(index);
    bulkLoader.setMaxElementsPerBulk(bulkSize);
    return bulkLoader;
  }
}
