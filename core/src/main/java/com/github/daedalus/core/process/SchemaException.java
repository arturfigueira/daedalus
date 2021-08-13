package com.github.daedalus.core.process;

import java.util.List;

/**
 * Checked exception thrown to indicate when a Data node does not follow an elastic search index
 * mappings
 */
public class SchemaException extends Exception {

  /**
   * Construct an instance with the reason of this schema exception
   *
   * @param message A string explaining the non-conformity
   */
  public SchemaException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with a list of all non-conformities
   *
   * @param messages List containing descriptions of all non-conformities
   */
  public SchemaException(List<String> messages) {
    super(String.join(", ", messages));
  }
}
