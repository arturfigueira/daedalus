package com.daedalus.core.process.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.elasticsearch.action.index.IndexRequest;

public interface ElasticClient {
  CompletableFuture<Boolean> index(
      String localIdentifier, final List<IndexRequest> requests);
}
