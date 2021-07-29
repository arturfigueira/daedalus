package com.daedalus.core.process.client;

import com.daedalus.core.process.client.ResultsQueue.Item;
import com.daedalus.core.process.client.ResultsQueue.Result;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

class BulkAsyncListener implements BulkProcessor.Listener {

  private final String localIdentifier;
  private final ResultsQueue resultsQueue;

  public BulkAsyncListener(@NonNull String localIdentifier, @NonNull ResultsQueue resultsQueue) {
    this.localIdentifier = localIdentifier;
    this.resultsQueue = resultsQueue;
  }

  @Override
  public void beforeBulk(long executionId, BulkRequest request) {
    return;
  }

  @Override
  public void afterBulk(
      @NotNull long executionId, @NotNull BulkRequest request, @NotNull BulkResponse response) {
    final var result =
        Result.builder()
            .localIdentifier(this.localIdentifier)
            .requestId(executionId)
            .index(request.getIndices().iterator().next())
            .executionTime(response.getTook().getMillis())
            .resultItems(
                Arrays.stream(response.getItems())
                    .map(this::mapToItem)
                    .collect(Collectors.toList()))
            .build();

    addToGlobalResults(result);
  }

  private void addToGlobalResults(Result result) {
    if (!this.resultsQueue.add(result)) {
      throw new ClientAsyncException("The " + result + " could not be added into bulk results");
    }
  }

  private Item mapToItem(final BulkItemResponse bulkItemResponse) {
    var itemBuilder = Item.builder()
            .action(bulkItemResponse.getOpType().toString());

    Optional.ofNullable(bulkItemResponse.getResponse())
        .ifPresent(o -> itemBuilder.id(((DocWriteResponse)o).getId()));

    if (bulkItemResponse.isFailed()) {
      final var failure = bulkItemResponse.getFailure();
      itemBuilder.error(new ResultsQueue.Error(failure.getType(), failure.getMessage()));
    }

    return itemBuilder.build();
  }

  @Override
  public void afterBulk(long executionId, final BulkRequest request, final Throwable failure) {
    final var result =
        Result.builder()
            .localIdentifier(this.localIdentifier)
            .requestId(executionId)
            .index(request.getIndices().iterator().next())
            .failure(failure)
            .build();

    addToGlobalResults(result);
  }
}
