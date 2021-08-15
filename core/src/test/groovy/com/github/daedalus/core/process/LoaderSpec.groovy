package com.github.daedalus.core.process


import com.github.daedalus.core.stream.DataReader
import com.github.daedalus.core.stream.DataSource
import com.github.daedalus.core.elastic.DataMapping
import com.github.daedalus.core.elastic.Document
import com.github.daedalus.core.elastic.ElasticDataType
import com.github.daedalus.core.process.client.ElasticClient
import com.github.daedalus.core.stream.DataStore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Loader Specifications")
class LoaderSpec extends Specification {

    @Shared def validMappings = [new DataMapping("name", ElasticDataType.TEXT)]
    @Shared def validClient = Mock(ElasticClient)

    @Unroll
    def "bulk loader wont accept max elements equals or below zero"(val){
        given:
        def bulkLoader = new Loader.BulkLoader(null, null);

        when:
        bulkLoader.setMaxElementsPerBulk(val)

        then:
        thrown(IllegalArgumentException)

        where:
        val << [0, -1, -100]
    }

    def "bulk loader should throws when loading null datasource"(){
        given:
        def bulkLoader = new Loader.BulkLoader(null, null);

        when:
        bulkLoader.from(null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "should throws when bulk loading with invalid args"(index, sizePerBulk){
        given:
        def loader = new LoaderBuilder()
                .elasticClient(validClient)
                .mappings(validMappings)
                .build()

        when:
        loader.toIndex(index, sizePerBulk)

        then:
        thrown(IllegalArgumentException)

        where:
        index << ["", "    ", null, "sample", "sample"]
        sizePerBulk << [10, 10, 10, 0, -1]

    }




    def "when a dataStore is provided a backup to it should occurs"(){
        given:
        def dataStore = Mock(DataStore)
        def loader = new LoaderBuilder()
                .elasticClient(validClient)
                .mappings(validMappings)
                .backupTo(dataStore)
                .build()

        def dataReader = Mock(DataReader)
        def dataSource = Mock(DataSource)
        dataSource.hasNext() >>> [true, false]
        dataSource.next() >> dataReader

        dataReader.getSource() >> "mockReader"
        dataReader.read(_) >>> [[new Document("1", ["name": "Lord of The Rings"])], []]

        when:
        loader.toIndex("books").from(dataSource)

        then:
        1 * dataStore.store('mockReader_0',[['name':'Lord of The Rings']])
    }

    def "loader will split in batches depending on the max elements per bulk request"(){
        given:
        def elasticClient = Mock(ElasticClient)
        def loader = new LoaderBuilder()
                .elasticClient(elasticClient)
                .mappings(validMappings)
                .build()

        def dataReader = Mock(DataReader)
        def dataSource = Mock(DataSource)
        dataSource.hasNext() >>> [true, false]
        dataSource.next() >> dataReader

        dataReader.getSource() >> "mockReader"
        dataReader.read(_) >>> [[new Document("1", ["name": "Lord of The Rings"])],
                                [new Document("2", ["name": "Gates of Fire"])],
                                []]

        when:
        loader.toIndex("books", 1).from(dataSource)

        then:
        2 * elasticClient.index(_,_)
    }
}
