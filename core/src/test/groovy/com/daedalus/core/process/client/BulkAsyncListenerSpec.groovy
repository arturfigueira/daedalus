package com.daedalus.core.process.client

import org.apache.http.HttpHost
import org.apache.http.RequestLine
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.http.HttpResponse
import org.elasticsearch.index.shard.ShardId
import spock.lang.Shared
import spock.lang.Specification

class BulkAsyncListenerSpec extends Specification {

    @Shared ResultsQueue bulkResults
    @Shared RequestLine sampleReqLine
    @Shared HttpHost httpHost
    @Shared HttpResponse sampleHttpRes

    def setup(){
        bulkResults = ResultsQueue.unbounded();
        sampleReqLine = Mock(RequestLine)
        sampleHttpRes = Mock(HttpResponse)
        httpHost = new HttpHost("localhost", 8080)
    }

    def "after success bulk the request will be added to results"(){
        given:
        def request = new BulkRequest()
        request.add(new IndexRequest("testIndex"))

        def executionId = 1001
        def localIdentifier = "mockedTest"


        def shardId = new ShardId("testIndex", UUID.randomUUID().toString(), 100)
        def responseItems = [
                new BulkItemResponse(1, DocWriteRequest.OpType.INDEX,
                        new IndexResponse(shardId, "INDEX", "1", 1L, 1L, 1L, true)),
                new BulkItemResponse(2, DocWriteRequest.OpType.INDEX,
                        new IndexResponse(shardId, "INDEX", "2", 1L, 1L, 1L, true))
        ] as BulkItemResponse[]

        def response = new BulkResponse(responseItems,1000L)

        def expectedItems = responseItems.collect {
            new ResultsQueue.Item.ItemBuilder()
                .id(it.getId())
                .action(it.getOpType().toString())
                .build()
        }

        def expected = new ResultsQueue.Result.ResultBuilder()
                .index("testIndex")
                .requestId(executionId)
                .executionTime(1000L)
                .localIdentifier(localIdentifier)
                .resultItems(expectedItems)
                .build()

        def listener = new BulkAsyncListener(localIdentifier, bulkResults)

        when:
        listener.afterBulk(executionId, request, response)

        then:
        bulkResults.queue.size() == 1
        bulkResults.queue.contains(expected)
    }

    def "failed responses will be included within the bulk results"(){
        given:
        def request = new BulkRequest()
        request.add(new IndexRequest("testIndex"))

        def executionId = 1001
        def localIdentifier = "mockedTest"


        def failure = new Exception("Forced Error")

        def shardId = new ShardId("testIndex", UUID.randomUUID().toString(), 100)
        def responseItems = [
                new BulkItemResponse(1, DocWriteRequest.OpType.INDEX,
                        new IndexResponse(shardId, "INDEX", "1", 1L, 1L, 1L, true)),
                new BulkItemResponse(2, DocWriteRequest.OpType.INDEX,
                        new BulkItemResponse.Failure("testIndex", "INDEX", null, failure))
        ] as BulkItemResponse[]

        def response = new BulkResponse(responseItems,1000L)

        def expectedItems = responseItems.collect {
            def builder=new ResultsQueue.Item.ItemBuilder()
                    .id(it.getId())
                    .action(it.getOpType().toString())

            if(it.failed) {
                builder.error(new ResultsQueue.Error("INDEX",
                        "java.lang.Exception: Forced Error"))
            }

            return builder.build()
        }

        def listener = new BulkAsyncListener(localIdentifier, bulkResults)

        when:
        listener.afterBulk(executionId, request, response)

        then:
        bulkResults.queue[0].getResultItems() == expectedItems
    }

    def "after a failure bulk the request will be added to results"(){
        given:
        def request = new BulkRequest()
        request.add(new IndexRequest("testIndex"))

        def executionId = 1001
        def localIdentifier = "mockedTest"

        def error = new Exception()

        def expected = new ResultsQueue.Result.ResultBuilder()
                .index("testIndex")
                .requestId(executionId)
                .executionTime(0)
                .localIdentifier(localIdentifier)
                .failure(error)
                .build()

        def listener = new BulkAsyncListener(localIdentifier, bulkResults)

        when:
        listener.afterBulk(executionId, request, error)

        then:
        bulkResults.queue.size() == 1
        bulkResults.queue.contains(expected)
    }


}
