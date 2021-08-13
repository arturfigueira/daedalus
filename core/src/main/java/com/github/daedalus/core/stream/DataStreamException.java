package com.github.daedalus.core.stream;

/**
 * Checked exception thrown when a data can not be read from an external source.
 */
public class DataStreamException extends Exception {

  /**
   * Construct an instance with given reason and the original error cause.
   *
   * @param message String with an description of the error
   * @param cause   The original error cause of the stream failure
   */
  public DataStreamException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct an instance with original error cause.
   *
   * @param cause The original error cause of the stream failure
   */
  public DataStreamException(Throwable cause) {
    super(cause);
  }
}
