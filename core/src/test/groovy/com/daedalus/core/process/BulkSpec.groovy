package com.daedalus.core.process

import com.daedalus.core.process.client.ElasticClient
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
        ""          | Reshaper.ReshapedData.withoutType([["mock": 1]])
        "   "       | Reshaper.ReshapedData.withoutType([["mock": 1]])
        null        | Reshaper.ReshapedData.withoutType([["mock": 1]])
        "ABC"       | null
    }

    def "Nothing will be indexed if data is empty"(){
        given:
        def bulk = new Bulk("index", sampleClient)
        def emptyReshapedData = new Reshaper.ReshapedData("", [])

        when:
        bulk.index("mock", emptyReshapedData)

        then:
        0 * sampleClient.index
    }

    @Unroll
    def "Bulk will index all data as Json"(data, expected){
        given:
        def bulk = new Bulk("sampleIndex", sampleClient)
        def rehapedFata = Reshaper.ReshapedData.withoutType(data)

        when:
        bulk.index("mock", rehapedFata)

        then:
        1 * sampleClient.index("mock", {
            it.collect { it.source().utf8ToString() }.join(",") == expected
            && it.findAll { it.index != "sampleIndex" }.isEmpty()
            && it.findAll { it.contentType.shortName() != "json" }.isEmpty()
        })

        where:
        data << [
                [["name": "John", "Surname" : "Doe", "age": 28, "enabled": true]],
                [["name": "John"], ["name": "Sue"]],
                [["country": "Brazil", "exchRate": 5.0125], ["country": "UK", "exchRate": 1.025]]
        ]

        expected << [
                '{"name":"John","Surname":"Doe","age":28,"enabled":true}',
                '{"name":"John"},{"name":"Sue"}',
                '{"country":"Brazil","exchRate":5.0125},{"country":"UK","exchRate":1.025}'
        ]
    }

    @Unroll
    def "Bulk will index to a type all data as Json"(type,data, expected){
        given:
        def bulk = new Bulk("sampleIndex", sampleClient)
        def rehapedFata = new Reshaper.ReshapedData(type, data)

        when:
        bulk.index("mock", rehapedFata)

        then:
        1 * sampleClient.index("mock", {
            it.collect { it.source().utf8ToString() }.join(",") == expected
            && it.findAll { it.index != "sampleIndex" }.isEmpty()
            && it.findAll { it.contentType.shortName() != "json" }.isEmpty()
        })

        where:
        data << [
                [["name": "John", "Surname" : "Doe", "age": 28, "enabled": true]],
                [["name": "John"], ["name": "Sue"]],
                [["country": "Brazil", "exchRate": 5.0125], ["country": "UK", "exchRate": 1.025]]
        ]

        type << ["account", "user", "currency"]

        expected << [
                '{"account":{"name":"John","Surname":"Doe","age":28,"enabled":true}}',
                '{"user":{"name":"John"}},{"user":{"name":"Sue"}}',
                '{"currency":{"country":"Brazil","exchRate":5.0125}},{"currency":{"country":"UK","exchRate":1.025}}'
        ]
    }
}
