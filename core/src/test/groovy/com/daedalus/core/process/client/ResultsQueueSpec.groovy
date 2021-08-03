package com.daedalus.core.process.client

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

@Title("ResultsQueue Specifications")
class ResultsQueueSpec extends Specification {

    @Unroll
    def "Will throw when constructing with invalid arguments" (capacity, timeout){
        when:
        new ResultsQueue(capacity, timeout)

        then:
        thrown(IllegalArgumentException)

        where:
        capacity    | timeout
        0           | 0
        1           | 0
        1           | -1
        0           | 1
        -1          | 1
    }

    def "Adding a result should increase the size of the results"(){
        given:
        def bulkResults = ResultsQueue.unbounded()

        def result = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .localIdentifier("test")
                .requestId(1)
                .executionTime(100L).build();

        when:
        def isAdded = bulkResults.add(result)

        then:
        isAdded
        !bulkResults.isEmpty()
    }

    def "Adding to a full bounded BulkResults will timeout and not add"(){
        given:
        def bulkResults = new ResultsQueue(1, 10)

        def result = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .localIdentifier("test")
                .requestId(1)
                .executionTime(100L)
                .build()

        when:
        def withinLimitAdd = bulkResults.add(result)
        def overLimitAdd = bulkResults.add(result)

        then:
        withinLimitAdd
        !overLimitAdd
    }

    def "Draining results will diminish the stored results"(){
        given:
        def bulkResults = new ResultsQueue(1, 10)

        def result = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .localIdentifier("test")
                .requestId(1)
                .executionTime(100L)
                .build()

        bulkResults.add(result)

        when:
        def drainedResult = bulkResults.drain()

        then:
        drainedResult != null
        bulkResults.isEmpty()
    }

    def "Draining wont block indefinitely when is empty"(){
        given:
        def bulkResults = new ResultsQueue(1, 10)

        when:
        def drainedResult = bulkResults.drain()

        then:
        drainedResult == null
    }

    def "Should throws when adding null to the queue"(){
        given:
        def bulkResults = ResultsQueue.unbounded()

        when:
        bulkResults.add(null)

        then:
        thrown(NullPointerException)
    }

    def "Add will occur if room is made available before timeout happens"(){
        given:
        def bulkResults = new ResultsQueue(1, 250)

        def result = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .localIdentifier("test")
                .requestId(1)
                .executionTime(100L)
                .build()

        bulkResults.add(result)
        def completableFuture = CompletableFuture.supplyAsync(() -> {
            sleep(100)
            return bulkResults.drain()
        })
        when:
        def isAdded = bulkResults.add(result)
        completableFuture.get()

        then:
        isAdded
        completableFuture.isDone()
    }

    def "Drain should work as FIFO"(){
        given:
        def bulkResults = ResultsQueue.unbounded()

        def resultA = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .requestId(1)
                .build()

        def resultB = new ResultsQueue.Result.ResultBuilder()
                .index("mock")
                .requestId(2)
                .build()

        bulkResults.add(resultA)
        bulkResults.add(resultB)

        when:
        def drainedResult = bulkResults.drain()

        then:
        drainedResult == resultA
    }
}
