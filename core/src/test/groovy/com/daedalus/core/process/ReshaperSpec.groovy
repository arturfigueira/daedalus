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
    def "Will throw when constructing with invalid arguments"(type,mappings, parser){
        when:
        new Reshaper(type, mappings, parser)

        then:
        thrown(NullPointerException)

        where:
        type << [null, "mock", "mock"]
        mappings << [[new DataMapping("name", ElasticDataType.TEXT)], null, [new DataMapping("name", ElasticDataType.TEXT)]]
        parser << [dataParser, dataParser, null]
    }

    def "Will throw when reshaping a null"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = Reshaper.withoutType([mapping], dataParser)

        when:
        reshaper.reshape(null)

        then:
        thrown(IllegalArgumentException)

    }

    def "Should thrown when a mapped property is not present at the input document"(){
        given:
        def mapping = new DataMapping("name", ElasticDataType.TEXT)
        def reshaper = Reshaper.withoutType([mapping], dataParser)

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
        def reshaper = Reshaper.withoutType([isValidMapping, ageMapping], dataParser)

        def document = new Document("mock", ["age": "32", "isValid": "0"])

        when:
        def reshapedData = reshaper.reshape([document])

        then:
        reshapedData.getDocuments().size() == 1
        reshapedData.getDocuments().get(0).get("age") == 32 as short
        reshapedData.getDocuments().get(0).get("isValid") == false
    }

    def "Should add provided type to the reshaped data"(){
        given:
        def ageMapping = new DataMapping("age", ElasticDataType.SHORT)
        def indexType = "contact"
        def reshaper = new Reshaper(indexType, [ageMapping], dataParser)

        def document = new Document("1001", ["age": "32"])

        when:
        def reshapedData = reshaper.reshape([document])

        then:
        reshapedData.getType() == indexType
    }

    def "Should add an empty type when indexing directly to the index"(){
        given:
        def ageMapping = new DataMapping("age", ElasticDataType.SHORT)
        def reshaper = Reshaper.withoutType([ageMapping], dataParser)

        def document = new Document("1001", ["age": "32"])

        when:
        def reshapedData = reshaper.reshape([document])

        then:
        reshapedData.getType().isEmpty()
    }

    def "Should return an empty list when reshaping an empty input"(){
        given:
        def isValidMapping = new DataMapping("isValid", ElasticDataType.BOOLEAN)
        def reshaper = new Reshaper("user", [isValidMapping], dataParser)

        when:
        def reshapedData = reshaper.reshape([])

        then:
        reshapedData.getDocuments() == []
    }

    def "ReshapeData without type construct with empty type"(){
        when:
        def reshapedData = Reshaper.ReshapedData.withoutType([])

        then:
        reshapedData.getType().isEmpty()
    }

    @Unroll
    def "ReshapeData constructs with provided data"(type, documents){
        when:
        def reshapedData = new Reshaper.ReshapedData(type, documents)

        then:
        reshapedData.getType() == type
        reshapedData.getDocuments() == documents

        where:
        type        | documents
        "contact"   | []
        "users"     | [["name": "John", "age": 35]]
    }
}
