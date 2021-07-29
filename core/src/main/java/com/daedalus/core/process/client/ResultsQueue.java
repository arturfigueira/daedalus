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

public class ResultsQueue {

  protected final Long timeout;
  protected final BlockingQueue<Result> queue;

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

  static ResultsQueue unbounded() {
    return new ResultsQueue();
  }

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


  @Getter
  @ToString
  @EqualsAndHashCode
  @Builder(access = AccessLevel.PROTECTED)
  public static class Item {
    private final String id;
    private final String action;
    private final Error error;
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Error {
    private final String type;
    private final String reason;
  }

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

  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  @SneakyThrows
  public Result drain() {
    return (this.timeout > 0)
        ? this.queue.poll(this.timeout, TimeUnit.MILLISECONDS)
        : this.queue.take();
  }
}
