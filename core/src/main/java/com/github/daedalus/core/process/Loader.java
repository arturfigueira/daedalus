package com.github.daedalus.core.process;

import com.github.daedalus.core.elastic.DataMapping;
import com.github.daedalus.core.elastic.IncorrectTypeException;
import com.github.daedalus.core.process.client.ElasticClient;
import com.github.daedalus.core.stream.DataReader;
import com.github.daedalus.core.stream.DataSource;
import com.github.daedalus.core.stream.DataStore;
import com.github.daedalus.core.stream.DataStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * {@link Loader} is an object with methods to load data to an elastic search index.
 * <p>
 * It does not contains a public constructor and should be created using {@link LoaderBuilder},
 * which allows to set all options or just rely on defaults.
 * <p>
 * For bulk loading, loader has available a companion class {@link BulkLoader}. This companion is
 * able to read data from an external source, process it, convert it a indexable format and load it
 * in configurable batches to an elastic search index.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Loader {

  protected static final int MAX_BULK_ELEMENTS = 500;

  protected final ElasticClient client;
  protected final DataStore backup;
  protected final Reshaper reshaper;

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
   * setup to {@value MAX_BULK_ELEMENTS} items per bulk. This can be overwritten through {@link
   * #setMaxElementsPerBulk(int) setMaxElementsPerBulk}, but be aware of the {@link ElasticClient}
   * limitations that is being used and the elastic infrastructure.
   */
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class BulkLoader {

    private final Loader loader;
    private final Bulk bulk;
    private int maxElementsPerBulk = MAX_BULK_ELEMENTS;

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
     * Reads raw data defined by the {@link DataSource} and prepare and index it. If the optional
     * {@link DataStore} is present, at Loader, the parsed data will be stored after each bulk
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
            final var loadedNodes = bulkReadAndLoad(reader, criteria);
            keepReading = !loadedNodes.isEmpty();

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

    protected List<Map<String, Object>> bulkReadAndLoad(
        final DataReader reader, final DataReader.Criteria criteria)
        throws DataStreamException, SchemaException, IncorrectTypeException, IOException {
      final var dataNodes = reader.read(criteria);
      final var reshapedNodes = this.loader.reshaper.reshape(dataNodes);

      if (!reshapedNodes.isEmpty()) {
        var identifier = reader.getSource() + "_" + criteria.getPage();
        this.bulk.index(identifier, reshapedNodes);
        Optional.ofNullable(this.loader.backup)
            .ifPresent(ds -> ds.store(identifier, reshapedNodes));
      }

      return reshapedNodes;
    }
  }

  /**
   * Creates a new {@link BulkLoader} to process bulk requests to a given index. The number of items
   * per bulk will be the default {@value MAX_BULK_ELEMENTS}.
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
