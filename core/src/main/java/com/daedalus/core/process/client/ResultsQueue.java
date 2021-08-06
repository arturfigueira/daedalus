package com.daedalus.core.process.client;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * A queue that might be used to maintain a history of all requests that were asynchronously sent to
 * an elastic search index. This object is thread safe.
 * <p>
 * There are two ways to create a new instance of this queue. An unbounded queue can be created by
 * the static method {@link #unbounded()}. A bounded queue can be created by its default
 * constructor.
 * <p>
 * Be aware that the unbounded queue can grow indefinitely, resulting in overflows. Also, unbounded
 * queues won`t timeout for any operations that modifies its contents. Bounded queues are required
 * to setup a timeout for any operations that modifies its contents.
 */
public class ResultsQueue {

  protected final Long timeout;
  protected final BlockingQueue<Result> queue;

  /**
   * Constructs a new instance, limiting the number of elements that this queue will hold.
   * Operations, that modifies its content, will abort if its not completed within the specified
   * timeout.
   *
   * @param capacity maximum number of elements that this queue will hold
   * @param timeout  maximum time that this queue will wait until any insertion/removal is
   *                 completed. Time is calculated in milliseconds.
   * @throws IllegalArgumentException if any of the values are below or equal to zero
   */
  ResultsQueue(final int capacity, final long timeout) {
    if (capacity <= 0 || timeout <= 0) {
      throw new IllegalArgumentException("Max in-memory and timeout should be greater than zero");
    }

    this.queue = new LinkedBlockingQueue<>(capacity);
    this.timeout = timeout;
  }

  private ResultsQueue() {
    this.queue = new LinkedBlockingQueue<>();
    this.timeout = 0L;
  }

  /**
   * Creates a new instance of a {@link ResultsQueue} without capacity limitations and timeout An
   * unbounded queue can grow indefinitely, an will be restricted by the resources available to the
   * application. Use it with caution, as it can crash the application, consuming all available
   * resources
   */
  static ResultsQueue unbounded() {
    return new ResultsQueue();
  }

  /**
   * Object that represents the result of an elastic search`s request
   */
  @Getter
  @EqualsAndHashCode
  @ToString
  @Builder(access = AccessLevel.PROTECTED)
  public static class Result {

    private final String localIdentifier;
    private final long requestId;
    private final String index;
    private final long executionTime;

    @Singular
    private final List<Item> resultItems;
    private final Throwable failure;
  }


  /**
   * Objects that represents the items that were part of an elastic search`s request
   */
  @Getter
  @ToString
  @EqualsAndHashCode
  @Builder(access = AccessLevel.PROTECTED)
  public static class Item {

    private final String id;
    private final String action;
    private final Error error;
  }

  /**
   * Object that represents a request error, describing its reason and type
   */
  @Getter
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Error {

    private final String type;
    private final String reason;
  }

  /**
   * Insert the specified item into this queue, returning {@code true} upon successful inclusion,
   * {@code false} otherwise.
   * <p>
   * If this queue is configured with a capacity, the insertion will wait until space is available
   * or until the specified timeout is exceeded.
   *
   * @param result non-null {@link Result} item
   * @return {@code true} if it was added successfully, {@code false} otherwise
   * @throws NullPointerException if the element is null
   */
  boolean add(final @NonNull Result result) {
    var added = false;
    if (this.timeout == 0) {
      queueIt(result);
      added = true;
    } else {
      added = offerToQueue(result, this.timeout);
    }
    return added;
  }

  @SneakyThrows
  private void queueIt(Result result) {
    this.queue.put(result);
  }

  @SneakyThrows
  private boolean offerToQueue(Result result, Long tm) {
    return this.queue.offer(result, tm, TimeUnit.MILLISECONDS);
  }

  /**
   * Verify if the queue is empty or not
   *
   * @return {@code true} if the queue is empty}, {@code false} otherwise}.
   */
  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  /**
   * Retrieves and removes the head of this queue.
   * <p>
   * If this queue was setup with a timeout it will wait until the specified time for an element to
   * be available.
   * <p>
   * If not timeout was setup, the method will return right away, without blocking.
   * <p>
   * If no element is available {@code null} will be returned.
   *
   * @return the head of this queue or null if no element is available
   */
  @SneakyThrows
  public Result drain() {
    return (this.timeout > 0)
        ? this.queue.poll(this.timeout, TimeUnit.MILLISECONDS)
        : this.queue.poll();
  }
}
