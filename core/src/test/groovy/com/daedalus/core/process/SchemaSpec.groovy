package com.daedalus.core.process

import com.daedalus.core.data.DataMapping
import com.daedalus.core.data.DataNode
import com.daedalus.core.data.ElasticDataType
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Schema Specifications")
class SchemaSpec extends Specification {

    @Unroll
    def "Should not be able to construct with empty mappings"(List<DataMapping> mappings){
        when:
        new Schema(mappings)

        then:
        thrown(IllegalArgumentException)

        where:
        mappings << [null, []]
    }

    def "Should convict when a data is missing a schema properties"(){
        given:
        def data = new DataNode("mock", [name: "John Doe"])
        def schema = new Schema(
                [new DataMapping("name", ElasticDataType.TEXT),
                 new DataMapping("age", ElasticDataType.INTEGER)])

        when:
        schema.convict(data)

        then:
        thrown(SchemaException)
    }

    @Unroll
    def "Should convict when data is null"(){
        given:
        def data = new DataNode("mock", [name: null])
        def schema = new Schema([new DataMapping("name", datatype)])

        when:
        schema.convict(data)

        then:
        thrown(SchemaException)

        where:
        datatype << [ElasticDataType.BOOLEAN,
                     ElasticDataType.INTEGER,
                     ElasticDataType.FLOAT,
                     ElasticDataType.IP,
                     ElasticDataType.COMPLETION,
                     ElasticDataType.TEXT]
    }

    @Unroll
    def "Should convict due to #val not being a #datatype"(val, datatype){
        given:
        def data = new DataNode("mock", [value: val])
        def schema = new Schema([new DataMapping("value", datatype)])

        when:
        schema.convict(data)

        then:
        thrown(SchemaException)

        where:
        val         | datatype
        "Foo"       | ElasticDataType.BOOLEAN
        true        | ElasticDataType.INTEGER
        1.5f        | ElasticDataType.INTEGER
        "10"        | ElasticDataType.INTEGER
        1           | ElasticDataType.TEXT
        2.5         | ElasticDataType.DATE
        true        | ElasticDataType.DATE
        "25/10/1998"| ElasticDataType.DATE
        100         | ElasticDataType.BYTE
        new Date()  | ElasticDataType.COMPLETION
        100         | ElasticDataType.COMPLETION
        "192.1"     | ElasticDataType.IP
        "290.1.1.1" | ElasticDataType.IP
    }

    @Unroll
    def "Should not convict when a data matches datatype definition"(val, datatype){
        given:
        def data = new DataNode("mock", [value: val])
        def schema = new Schema([new DataMapping("value", datatype)])

        when:
        schema.convict(data)

        then:
        noExceptionThrown()

        where:
        val                 | datatype
        true                | ElasticDataType.BOOLEAN
        false               | ElasticDataType.BOOLEAN
        1                   | ElasticDataType.INTEGER
        200 as short        | ElasticDataType.SHORT
        2.5f                | ElasticDataType.FLOAT
        100L                | ElasticDataType.LONG
        127 as byte         | ElasticDataType.BYTE
        2.3 as double       | ElasticDataType.DOUBLE
        0b00100001 as byte  | ElasticDataType.BYTE
        "192.168.1.1"       | ElasticDataType.IP
        "John Doe"          | ElasticDataType.COMPLETION
        "John Doe"          | ElasticDataType.TEXT
        "John Doe"          | ElasticDataType.KEYWORD
    }
}
