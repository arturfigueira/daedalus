package com.github.daedalus.core.process.client;

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

/**
 * Client that wraps an instance of the official Elastic Search {@link RestHighLevelClient} and
 * allows to execute request operations asynchronously, taking care responses and errors.
 * <p>
 * It contains provides a result queue which holds all responses that elastic search sent back,
 * allowing a post processing, of the results, on a non-blocking fashion.
 * <p>
 * Async operations can be performed by a custom {@link Executor}, which can be provided to the
 * wrapper. This is useful when thread-pooling is controlled by an external entity, such as an
 * enterprise application server.
 * <p>
 * Besides that, async operations is configured to timeout after a specified amount of time, but
 * this behavior can be disabled. Beware that if not disabled all async operations might continue
 * running indefinitely.
 * <p>
 * Results queued at this client is limited to a specified number of elements and can be configured
 * per user necessity.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class HighLevelClientWrapper implements ElasticClient {

  private final RestHighLevelClient client;
  private final ResultsQueue resultsQueue;
  private final long timeout;
  private final Executor executor;

  /**
   * {@link HighLevelClientWrapper} static builder
   */
  public static class Wrap {

    protected RestHighLevelClient client;
    protected int capacity = 500;
    protected long timeout = 10L;
    private Executor executor;

    /**
     * Construct a new Wrap builder instance, wrapping the provided elastic rest client. By default,
     * all wrap builders starts with a default capacity and a timeout in milliseconds
     *
     * @param client {@link RestHighLevelClient} to be wrapped
     * @throws IllegalArgumentException if the client is null
     */
    public Wrap(final RestHighLevelClient client) {
      this.client =
          Optional.ofNullable(client)
              .orElseThrow(() -> new IllegalArgumentException("Rest Client must not be null"));
    }

    /**
     * Configure the maximum number of results that this client can hold. A zero value will allow an
     * infinite number of results to be queued, use it with caution as it might consume resource
     * indefinitely.
     *
     * @param capacity number of results that can be queued at this client
     * @return this builder instance
     * @throws IllegalArgumentException if the capacity is below zero
     */
    public Wrap capacity(int capacity) {
      if (capacity < 0) {
        throw new IllegalArgumentException("Capacity must not be below zero");
      }
      this.capacity = capacity;
      return this;
    }

    /**
     * Configure the maximum time that this client will wait for an async operation to complete.
     * Zero means that timeout is disable and the client will wait indefinitely for any async
     * operation to complete.
     *
     * @param timeout time in milliseconds
     * @return this builder instance
     * @throws IllegalArgumentException if specified timeout is is below or equals zero
     */
    public Wrap timeout(long timeout) {
      if (timeout < 0) {
        throw new IllegalArgumentException("Timeout must not be below zero");
      }
      this.timeout = timeout;
      return this;
    }

    /**
     * Configure a custom {@link Executor} for all async tasks that this client spawns
     *
     * @param executor custom async task executor
     * @return this builder instance
     * @throws IllegalArgumentException if specified executor is null
     */
    public Wrap executor(final Executor executor) {
      this.executor =
          Optional.ofNullable(executor)
              .orElseThrow(() -> new IllegalArgumentException("Executor must not be null"));

      return this;
    }

    /**
     * Creates a new instance of an {@link HighLevelClientWrapper} configured with this builder
     * properties
     *
     * @return configured wrapper instance
     */
    public HighLevelClientWrapper build() {
      final var bulkResults =
          (capacity > 0) ? new ResultsQueue(capacity, this.timeout) : ResultsQueue.unbounded();
      return new HighLevelClientWrapper(this.client, bulkResults, this.timeout, executor);
    }
  }

  /**
   * Returns the current results queue
   *
   * @return
   */
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
            "daedalus_" + this.getClass().getSimpleName())
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
