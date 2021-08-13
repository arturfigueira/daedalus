package com.github.daedalus.core.stream;

/**
 * An object that represents an external source of data. It provides methods, such as {@link
 * #next()} and {@link #hasNext()}, to iterate over the data tha this data source has access to.
 * <p>
 * This was designed to allow reading large amounts of data in batches, preventing an overflow of
 * memory.
 */
public interface DataSource {

  /**
   * Returns the next {@link DataReader}
   *
   * @return a new the next available {@link DataReader}
   * @throws DataStreamException If any error occurs while retrieving the next reader
   */
  DataReader next() throws DataStreamException;

  /**
   * Indicates whether there is still more data to be read
   *
   * @return {@code true} if there is still more data to be read, {@code false} otherwise.
   * @throws DataStreamException If any error occurs while checking for the next reader.
   */
  boolean hasNext() throws DataStreamException;
}
