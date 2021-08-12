package com.daedalus.core.process;

import com.daedalus.core.process.Reshaper.ReshapedData;
import com.daedalus.core.process.client.ElasticClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Helper class to prepare and send bulk request to an elastic search index.
 */
class Bulk {

  protected final String index;
  protected final ElasticClient client;

  Bulk(String index, final ElasticClient client) {
    this.index =
        Optional.ofNullable(index)
            .filter(s -> !s.trim().isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("index should not be null nor empty"));

    this.client =
        Optional.ofNullable(client)
            .orElseThrow(() -> new IllegalArgumentException("Elastic client should not be null"));
  }

  public void index(String localIdentifier, final ReshapedData reshapedData)
      throws IOException {

    var identifier =
        Optional.ofNullable(localIdentifier)
            .filter(s -> !s.trim().isBlank())
            .orElseThrow(
                () -> new IllegalArgumentException("identifier should not be null nor empty"));

    if (reshapedData == null) {
      throw new IllegalArgumentException("Reshaped Data should not be null");
    }

    if (reshapedData.getDocuments().isEmpty()) {
      return;
    }

    var documents = reshapedData.getDocuments();
    var type = reshapedData.getType();
    final var requests =
        type.isBlank() ? toIndexRequests(documents) : toTypeRequests(type, documents);

    client.index(identifier, requests);
  }

  private List<IndexRequest> toTypeRequests(final String type,
      final List<Map<String, Object>> documents)
      throws IOException {
    final var requests = new ArrayList<IndexRequest>();
    for (var document : documents) {
      var typeJson = this.convertToJson(type, document);
      requests.add(new IndexRequest(this.index).source(typeJson));
    }

    return requests;
  }

  private List<IndexRequest> toIndexRequests(final List<Map<String, Object>> documents)
      throws IOException {
    final var requests = new ArrayList<IndexRequest>();
    for (var document : documents) {
      var documentJson = this.convertToJson(document);
      requests.add(new IndexRequest(this.index).source(documentJson));
    }

    return requests;
  }

  private XContentBuilder convertToJson(String type, final Map<String, Object> document)
      throws IOException {
    final var jsonBuilder = XContentFactory.jsonBuilder();
    jsonBuilder.startObject();
    {
      jsonBuilder.startObject(type);
      {
        for (var entry : document.entrySet()) {
          jsonBuilder.field(entry.getKey(), entry.getValue());
        }
      }
      jsonBuilder.endObject();
    }
    jsonBuilder.endObject();
    return jsonBuilder;
  }

  private XContentBuilder convertToJson(Map<String, Object> document) throws IOException {
    var documentJson = XContentFactory.jsonBuilder().startObject();
    for (var entry : document.entrySet()) {
      documentJson.field(entry.getKey(), entry.getValue());
    }
    documentJson.endObject();
    return documentJson;
  }
}
