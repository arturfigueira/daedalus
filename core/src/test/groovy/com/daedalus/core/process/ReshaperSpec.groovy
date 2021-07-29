package com.daedalus.core.process

import com.daedalus.core.data.DataMapping
import com.daedalus.core.data.DataNode
import com.daedalus.core.data.ElasticDataType
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Reshaper Specifications")
class ReshaperSpec extends Specification {

    @Unroll
    def "Will throw when constructing with invalid arguments"(mappings){
        when:
        new Reshaper(mappings)

        then:
        thrown(IllegalArgumentException)

        where:
        mappings << [[] as List<DataMapping>, null]
    }

    def "Will throw when reshaping a null"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = new Reshaper([mapping])

        when:
        reshaper.reshape(null)

        then:
        thrown(IllegalArgumentException)

    }

    def "Should thrown when a mapped property is not present at the data input"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = new Reshaper([mapping])

        def dataNode = new DataNode("mock", ["age" : 32])

        when:
        reshaper.reshape([dataNode])

        then:
        thrown(SchemaException)
    }

    def "Should reshape a data node based on given mappings"(){
        given:
        def isValidMapping = new DataMapping("isValid", ElasticDataType.BOOLEAN)
        def ageMapping = new DataMapping("age", ElasticDataType.SHORT)
        def reshaper = new Reshaper([isValidMapping, ageMapping])

        def dataNode = new DataNode("mock", ["age" : "32", "isValid" : "0"])

        when:
        def reshapedData = reshaper.reshape([dataNode])

        then:
        reshapedData.size() == 1
        reshapedData.get(0).get("age") == 32 as short
        reshapedData.get(0).get("isValid") == false
    }

    def "Should return an empty list when reshaping an empty input"(){
        given:
        def isValidMapping = new DataMapping("isValid", ElasticDataType.BOOLEAN)
        def reshaper = new Reshaper([isValidMapping])

        when:
        def reshapedData = reshaper.reshape([])

        then:
        reshapedData == []
    }
}
