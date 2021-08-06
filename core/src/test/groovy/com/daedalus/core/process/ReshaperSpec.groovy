package com.daedalus.core.process

import com.daedalus.core.data.DataMapping
import com.daedalus.core.data.Document
import com.daedalus.core.data.DataParser
import com.daedalus.core.data.ElasticDataType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Reshaper Specifications")
class ReshaperSpec extends Specification {

    @Shared DataParser

    def setup() {
        dataParser = new DataParser.Builder().create()
    }

    @Unroll
    def "Will throw when constructing with invalid arguments"(mappings, parser){
        when:
        new Reshaper(mappings, parser)

        then:
        thrown(IllegalArgumentException)

        where:
        mappings << [[] as List<DataMapping>, null, [new DataMapping("name", ElasticDataType.TEXT)]]
        parser << [dataParser, dataParser, null]
    }

    def "Will throw when reshaping a null"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = new Reshaper([mapping], dataParser)

        when:
        reshaper.reshape(null)

        then:
        thrown(IllegalArgumentException)

    }

    def "Should thrown when a mapped property is not present at the input document"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = new Reshaper([mapping], dataParser)

        def document = new Document("mock", ["age": 32])

        when:
        reshaper.reshape([document])

        then:
        thrown(SchemaException)
    }

    def "Should reshape a document based on given mappings"(){
        given:
        def isValidMapping = new DataMapping("isValid", ElasticDataType.BOOLEAN)
        def ageMapping = new DataMapping("age", ElasticDataType.SHORT)
        def reshaper = new Reshaper([isValidMapping, ageMapping], dataParser)

        def document = new Document("mock", ["age": "32", "isValid": "0"])

        when:
        def reshapedData = reshaper.reshape([document])

        then:
        reshapedData.size() == 1
        reshapedData.get(0).get("age") == 32 as short
        reshapedData.get(0).get("isValid") == false
    }

    def "Should return an empty list when reshaping an empty input"(){
        given:
        def isValidMapping = new DataMapping("isValid", ElasticDataType.BOOLEAN)
        def reshaper = new Reshaper([isValidMapping], dataParser)

        when:
        def reshapedData = reshaper.reshape([])

        then:
        reshapedData == []
    }
}
