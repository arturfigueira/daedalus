package com.daedalus.core.stream;

import com.daedalus.core.data.Document;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * An object that may be used to read data from external data sources, such as filesystem, databases
 * or APIs.
 * <p>
 * This enforces that the object should be able to read data in a pageable matter, through the use
 * of a {@link Criteria} which is available as a companion class of this interface.
 * <p>
 * DataReader defines {@link #getSource()} and {@link #read(Criteria)} to read data.
 */
public interface DataReader {

  /**
   * An object that contains pagination criteria to navigate through a data source. Useful to read
   * large amounts of data, preventing the system to be overloaded with data at once.
   */
  @Getter
  @ToString
  class Criteria {

    protected final int page;
    protected final int size;

    /**
     * Creates a new criteria
     *
     * @param page which page it will be read.
     * @param size amount of data per page.
     */
    public Criteria(int page, int size) {
      if (page < 0 || size <= 0) {
        throw new IllegalArgumentException("Invalid criteria. page: " + page + ", size: " + size);
      }
      this.page = page;
      this.size = size;
    }

    /**
     * Returns the current position based on current page and size.
     */
    public int startAt() {
      return page * size;
    }

    /**
     * Returns the last position based on current page and size.
     */
    public int until() {
      return this.startAt() + (size - 1);
    }

    /**
     * Creates a new {@link Criteria} instance for the next page
     */
    public Criteria nextPage() {
      return new Criteria(this.page + 1, this.size);
    }

    /**
     * Returns a new instance of a {@link Criteria} starting at page Zero.
     *
     * @param size how many elements per page
     */
    public static Criteria fromBeginning(int size) {
      return new Criteria(0, size);
    }
  }

  /**
   * Returns the source identification. This might be the file name in which the data is located,
   * the id / or table name or even the rest api url that was used to access the resource.
   *
   * @throws DataStreamException if anything goes wrong when retrieving the information
   */
  String getSource() throws DataStreamException;

  /**
   * Reads a chunk of data, based on the given readerCriteria, return it as a list of {@code
   * DataNode}}
   *
   * @param readerCriteria the reading criteria
   * @return The list of data nodes that fulfill the reader criteria
   * @throws DataStreamException if it was not able to read the data
   */
  List<Document> read(final Criteria readerCriteria) throws DataStreamException;
}
