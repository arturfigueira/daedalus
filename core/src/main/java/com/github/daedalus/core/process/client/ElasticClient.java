package com.github.daedalus.core.process.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.elasticsearch.action.index.IndexRequest;

/**
 * Represents an Elastic Search client.
 */
public interface ElasticClient {

  /**
   * Sends asynchronously a list of index requests to elastic search
   *
   * @param localIdentifier A string that identifies the origin of the data that is being indexed
   * @param requests        A list of {@link IndexRequest}
   * @return A {@link CompletableFuture} that will resolve {@code true} if the all requests were
   * fulfilled, {@code false} otherwise.
   */
  CompletableFuture<Boolean> index(
      String localIdentifier, final List<IndexRequest> requests);
}
