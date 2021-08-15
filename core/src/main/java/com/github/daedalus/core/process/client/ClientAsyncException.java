package com.github.daedalus.core.process.client;

/**
 * Unchecked exception thrown to indicate that a {@link ElasticClient} async operation failed
 */
public class ClientAsyncException extends RuntimeException {

  /**
   * Constructs an instance from the given input reason
   *
   * @param reason The error reason
   */
  ClientAsyncException(String reason) {
    super(reason);
  }

  /**
   * Constructs an instance from the given input string and
   *
   * @param reason The error reason
   * @param cause  The execution that caused the failure
   */
  ClientAsyncException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
