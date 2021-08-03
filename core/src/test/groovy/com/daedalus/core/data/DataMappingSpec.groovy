package com.daedalus.core.data

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DataMappingSpec extends Specification {

    @Shared DataMapping sampleMapping = new DataMapping("name", ElasticDataType.TEXT)

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
}
