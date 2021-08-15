package com.github.daedalus.core.elastic

import com.github.daedalus.core.elastic.DataMapping
import com.github.daedalus.core.elastic.ElasticDataType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DataMappingSpec extends Specification {

    @Shared DataMapping sampleMapping = new DataMapping("name", ElasticDataType.TEXT)

    @Unroll
    def "Constructor should throw with invalid arguments"(name, outputName, type){
        when:
        new DataMapping(name, outputName, type)

        then:
        thrown(IllegalArgumentException)

        where:
        name    | outputName    | type
        ""      | "title"       | ElasticDataType.TEXT
        "title" | ""            | ElasticDataType.TEXT
        null    | "title"       | ElasticDataType.TEXT
        "title" | null          | ElasticDataType.TEXT
        "  "    | "title"       | ElasticDataType.TEXT
        "title" | "    "        | ElasticDataType.TEXT
    }

    @Unroll
    def "Comparing with #value the equality should be #expected"(){
        when:
        def result = sampleMapping.equals(value)

        then:
        result == expected

        where:
        value                                                 | expected
        sampleMapping                                         | true
        new DataMapping("name", ElasticDataType.TEXT)         | true
        null                                                  | false
        "sample"                                              | false
        new DataMapping("id", ElasticDataType.TEXT)           | false
        new DataMapping("name", ElasticDataType.BOOLEAN)      | false
        new DataMapping("id", ElasticDataType.SHORT)          | false
    }

    def "Default constructor should create outputName equals to name"(){
        when:
        def mapping = new DataMapping("title", ElasticDataType.SHORT)

        then:
        mapping.getName() == mapping.getOutputName()
    }

    def "Static constructor should create a mapping with chose outputName"(){
        when:
        def mapping = DataMapping.withOutputName("title", "customTitle", ElasticDataType.INTEGER)

        then:
        mapping.getOutputName() == "customTitle"
        mapping.getName() == "title"
    }
}
