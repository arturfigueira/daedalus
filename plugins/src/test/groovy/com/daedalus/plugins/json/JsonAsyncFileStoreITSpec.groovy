package com.daedalus.plugins.json

import com.google.gson.Gson
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Json Async File Writer Spec")
class JsonAsyncFileStoreITSpec extends Specification {

    @Shared File tempDir

    def setup(){
        tempDir = File.createTempDir()
    }

    def cleanup(){
        tempDir.listFiles().each {it.delete()}
        tempDir.delete()
    }

    @Unroll
    def "Should thrown when constructing with invalid arguments"(path, timeout){
        when:
        new JsonAsyncFileStore(path, timeout)

        then:
        thrown(IllegalArgumentException)

        where:
        path    |   timeout
        ""      |   5000
        "    "  |   5000
        null    |   5000
        "path"  |   -1
    }

    def "When a filename is not provided the file should stored with random name"(){
        given:
        def asyncFileStore = new JsonAsyncFileStore(tempDir.getAbsolutePath(), 10000)
        def data1 = ["name": "John Doe"]

        when:
        def promise = asyncFileStore.store([data1])

        then:
        promise.get().isStored()
        tempDir.listFiles().size() == 1
        tempDir.listFiles()[0].name.endsWith(".json")
    }

    def "Store file should contain the given data as a JSON"(){
        given:
        def asyncFileStore = new JsonAsyncFileStore(tempDir.getAbsolutePath(), 10000)
        def user1 = ["name": "John Doe", "age": 25, "isResident": true]
        def user2 = ["name": "Joanna Doe", "age": 32, "isResident": false]
        def data = [user1, user2]

        def expected = new Gson().toJson(data)

        when:
        def promise = asyncFileStore.store(data)

        then:
        promise.get().isStored()
        tempDir.listFiles()[0].text == expected
    }

    def "Write process should not be able to continue after the timeout"(){
        given:
        def asyncFileStore = new JsonAsyncFileStore(tempDir.getAbsolutePath(), 1)
        def user1 = ["name": "John Doe", "age": 25, "isResident": true]

        def arr = [];
        1000000.times {arr.add(user1)}

        when:
        def asyncStore = asyncFileStore.store(arr)
        def isSaved = asyncStore.get()

        then:
        !isSaved.isStored()
    }
}
