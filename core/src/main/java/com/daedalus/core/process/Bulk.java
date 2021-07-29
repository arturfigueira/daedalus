package com.daedalus.core.process;

import com.daedalus.core.process.client.ElasticClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentFactory;

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

  public void index(String localIdentifier, final List<Map<String, Object>> data)
      throws IOException {

    var identifier =
        Optional.ofNullable(localIdentifier)
            .filter(s -> !s.trim().isBlank())
            .orElseThrow(
                () -> new IllegalArgumentException("identifier should not be null nor empty"));

    if(data == null){
      throw new IllegalArgumentException("data should not be null");
    }

    if(data.isEmpty()){
      return;
    }

    final var requests = new ArrayList<IndexRequest>();
    for (var dataNode : data) {
      var jsonNode = XContentFactory.jsonBuilder().startObject();
      for (var entry : dataNode.entrySet()) {
        jsonNode.field(entry.getKey(), entry.getValue());
      }
      jsonNode.endObject();

      requests.add(new IndexRequest(this.index).source(jsonNode));
    }

    client.index(identifier, requests);
  }
}
