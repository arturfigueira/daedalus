package com.daedalus.core.process;

import com.daedalus.core.data.DataMapping;
import com.daedalus.core.data.DataParser;
import com.daedalus.core.data.IncorrectTypeException;
import com.daedalus.core.process.client.ElasticClient;
import com.daedalus.core.stream.DataReader;
import com.daedalus.core.stream.DataSource;
import com.daedalus.core.stream.DataStore;
import com.daedalus.core.stream.DataStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Loader {

  protected final ElasticClient client;
  protected final DataStore backup;
  protected final Reshaper reshaper;

  @NoArgsConstructor
  public static class Builder {

    protected ElasticClient client;
    protected DataStore backup = null;
    protected List<DataMapping> mappings;
    protected final DataParser.Builder dataParserBuilder = new DataParser.Builder();

    public Builder elasticClient(final ElasticClient client) {
      this.client =
          Optional.ofNullable(client)
              .orElseThrow(() -> new IllegalArgumentException("Client must not be null"));
      return this;
    }

    public Builder backupTo(final DataStore backup) {
      this.backup =
          Optional.ofNullable(backup)
              .orElseThrow(() -> new IllegalArgumentException("backup must not be null"));
      return this;
    }

    public Builder locale(final Locale locale) {
      if (locale == null) {
        throw new IllegalArgumentException("locale must not be null");
      }
      dataParserBuilder.locale(locale);
      return this;
    }

    public Builder timeZone(final TimeZone timeZone) {
      if (timeZone == null) {
        throw new IllegalArgumentException("timeZone must not be null");
      }
      dataParserBuilder.timeZone(timeZone);
      return this;
    }

    public Builder dateFormatPattern(final String datePattern) {
      if (datePattern == null || datePattern.isBlank()) {
        throw new IllegalArgumentException("datePattern must not be null nor empty");
      }
      dataParserBuilder.dateFormat(datePattern);
      return this;
    }

    public Builder mapDataWith(final List<DataMapping> mappings) {
      if (mappings == null || mappings.isEmpty()) {
        throw new IllegalArgumentException(
            "Mappings must is required and must contain at least one item");
      }
      this.mappings = mappings;
      return this;
    }

    public Loader build() {
      final var reshaper = new Reshaper(mappings, dataParserBuilder.create());
      return new Loader(client, backup, reshaper);
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class BulkLoader {

    private final Loader loader;
    private final Bulk bulk;
    private int maxElementsPerBulk = 500;

    public void setMaxElementsPerBulk(int maxElementsPerBulk) {
      if (maxElementsPerBulk <= 0) {
        throw new IllegalArgumentException("Bulk max number of elements must be greater than zero");
      }
      this.maxElementsPerBulk = maxElementsPerBulk;
    }

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

  public BulkLoader toIndex(final String index) {
    final var bulk = new Bulk(index, this.client);
    return new BulkLoader(this, bulk);
  }

  public BulkLoader toIndex(final String index, final int bulkSize) {
    final var bulkLoader = this.toIndex(index);
    bulkLoader.setMaxElementsPerBulk(bulkSize);
    return bulkLoader;
  }
}
