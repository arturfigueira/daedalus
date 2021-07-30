package com.daedalus.core.process.client;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class HighLevelClientWrapper implements ElasticClient {
  private final RestHighLevelClient client;
  private final ResultsQueue resultsQueue;
  private final long timeout;
  private final Executor executor;

  public static class Wrap {
    protected RestHighLevelClient client;
    protected int capacity = 0;
    protected long timeout = 10L;
    private Executor executor;

    public Wrap(final RestHighLevelClient client) {
      this.client =
          Optional.ofNullable(client)
              .orElseThrow(() -> new IllegalArgumentException("Rest Client must not be null"));
    }

    public Wrap capacity(int capacity) {
      if (capacity <= 0) {
        throw new IllegalArgumentException("BulkResults should not be null");
      }
      this.capacity = capacity;
      return this;
    }

    public Wrap timeout(long timeout) {
      if (timeout <= 0) {
        throw new IllegalArgumentException("Timeout must be greater than zero");
      }
      this.timeout = timeout;
      return this;
    }

    public Wrap executor(final Executor executor) {
      this.executor =
          Optional.ofNullable(executor)
              .orElseThrow(() -> new IllegalArgumentException("Executor must not be null"));

      return this;
    }

    public HighLevelClientWrapper build() {
      final var bulkResults =
          (capacity > 0) ? new ResultsQueue(capacity, this.timeout) : ResultsQueue.unbounded();
      return new HighLevelClientWrapper(this.client, bulkResults, this.timeout, executor);
    }
  }

  public ResultsQueue results() {
    return this.resultsQueue;
  }

  public CompletableFuture<Boolean> index(
      String localIdentifier, final List<IndexRequest> requests) {
    final var bulkProcessor =
        BulkProcessor.builder(
                (request, bulkListener) ->
                    client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                new BulkAsyncListener(localIdentifier, resultsQueue),
                "daedalus_"+this.getClass().getSimpleName())
            .build();

    requests.forEach(bulkProcessor::add);

    return (executor == null)
        ? CompletableFuture.supplyAsync(processorSupplier(bulkProcessor))
        : CompletableFuture.supplyAsync(processorSupplier(bulkProcessor), executor);
  }

  private Supplier<Boolean> processorSupplier(BulkProcessor bulkProcessor) {
    return () -> {
      try {
        return bulkProcessor.awaitClose(this.timeout, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new CompletionException(e);
      }
    };
  }
}
