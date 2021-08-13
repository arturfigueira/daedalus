package com.github.daedalus.core.process


import com.github.daedalus.core.process.client.ElasticClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Bulk Specifications")
class BulkSpec extends Specification {

    @Shared
    ElasticClient sampleClient

    def setup(){
        sampleClient = Mock(ElasticClient)
    }

    @Unroll
    def "Will throw when constructing with invalid arguments"(index, client){
        when:
        new Bulk(index, client)

        then:
        thrown(IllegalArgumentException)

        where:
        index   | client
        ""      | sampleClient
        "  "    | sampleClient
        null    | sampleClient
        null    | null
        "mock"  | null
    }

    @Unroll
    def "index wont accept invalid arguments "(identifier, data){
        given:
        def bulk = new Bulk("index", sampleClient)

        when:
        bulk.index(identifier, data)

        then:
        thrown(IllegalArgumentException)

        where:
        identifier  | data
        ""          | [["mock": 1]]
        "   "       | [["mock": 1]]
        null        | [["mock": 1]]
        "ABC"       | null
    }

    def "Nothing will be indexed if data is empty"(){
        given:
        def bulk = new Bulk("index", sampleClient)

        when:
        bulk.index("mock", [])

        then:
        0 * sampleClient.index
    }

    @Unroll
    def "Bulk will index all data as Json"(data){
        given:
        def bulk = new Bulk("sampleIndex", sampleClient)

        when:
        bulk.index("mock", data)

        then:
        1 * sampleClient.index("mock", {
            it.size() == data.size()
                    && it.findAll { it.index != "sampleIndex" }.isEmpty()
                    && it.findAll { it.contentType.shortName() != "json" }.isEmpty()
        })

        where:
        data << [
                [["name": "John", "Surname" : "Doe", "age": 28, "enabled": true]],
                [["name": "John"], ["name": "Sue"]],
                [["Country": "Brazil"], ["ExchRate": 5.0125]]
        ]
    }
}
