package com.daedalus.core.process

import com.daedalus.core.data.DataMapping
import com.daedalus.core.data.DataNode
import com.daedalus.core.data.ElasticDataType
import com.daedalus.core.process.client.ElasticClient
import com.daedalus.core.stream.DataReader
import com.daedalus.core.stream.DataSource
import com.daedalus.core.stream.DataStore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class LoaderSpec extends Specification {

    @Shared def validMappings = [new DataMapping("name", ElasticDataType.TEXT)]
    @Shared def validClient = Mock(ElasticClient)
    @Shared def validDataStore = Mock(DataStore)
    @Shared def validDateFormat = "dd-mm-yyyy"

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
        def loader = new Loader.Builder()
                .elasticClient(validClient)
                .mapDataWith(validMappings)
                .build()

        when:
        loader.toIndex(index, sizePerBulk)

        then:
        thrown(IllegalArgumentException)

        where:
        index << ["", "    ", null, "sample", "sample"]
        sizePerBulk << [10, 10, 10, 0, -1]

    }

    @Unroll
    def "loader builder should throws with invalid arguments"(client,
                                                              dataStore,
                                                              locale,
                                                              timeZone,
                                                              dataFormat,
                                                              mappings){
        when:
        new Loader.Builder()
                .elasticClient(client)
                .backupTo(dataStore)
                .locale(locale)
                .timeZone(timeZone)
                .dateFormatPattern(dataFormat)
                .mapDataWith(mappings)
                .build()

        then:
        thrown(IllegalArgumentException)

        where:
        client << [null, validClient, validClient, validClient, validClient, validClient, validClient, validClient ]
        dataStore << [validDataStore, null, validDataStore, validDataStore, validDataStore, validDataStore, validDataStore, validDataStore]
        locale << [Locale.getDefault(), Locale.getDefault(), null, Locale.getDefault(), Locale.getDefault(), Locale.getDefault(), Locale.getDefault(), Locale.getDefault()]
        timeZone << [TimeZone.getDefault(), TimeZone.getDefault(), TimeZone.getDefault(), null, TimeZone.getDefault(), TimeZone.getDefault(), TimeZone.getDefault(), TimeZone.getDefault() ]
        dataFormat << [validDateFormat, validDateFormat, validDateFormat, validDateFormat, null, " ", validDateFormat, validDateFormat ]
        mappings << [validMappings, validMappings, validMappings, validMappings, validMappings, validMappings, [], null ]
    }


    def "when a dataStore is provided a backup to it should occurs"(){
        given:
        def dataStore = Mock(DataStore)
        def loader = new Loader.Builder()
                .elasticClient(validClient)
                .mapDataWith(validMappings)
                .backupTo(dataStore)
                .build()

        def dataReader = Mock(DataReader)
        def dataSource = Mock(DataSource)
        dataSource.hasNext() >>> [true, false]
        dataSource.next() >> dataReader

        dataReader.getSource() >> "mockReader"
        dataReader.read(_) >>> [[new DataNode("1", ["name": "Lord of The Rings"])], []]

        when:
        loader.toIndex("books").from(dataSource)

        then:
        1 * dataStore.store('mockReader_0',[['name':'Lord of The Rings']])
    }

    def "loader will split in batches depending on the max elements per bulk request"(){
        given:
        def elasticClient = Mock(ElasticClient)
        def loader = new Loader.Builder()
                .elasticClient(elasticClient)
                .mapDataWith(validMappings)
                .build()

        def dataReader = Mock(DataReader)
        def dataSource = Mock(DataSource)
        dataSource.hasNext() >>> [true, false]
        dataSource.next() >> dataReader

        dataReader.getSource() >> "mockReader"
        dataReader.read(_) >>> [ [new DataNode("1", ["name": "Lord of The Rings"])],
                                 [new DataNode("2", ["name": "Gates of Fire"])],
                                 []]

        when:
        loader.toIndex("books", 1).from(dataSource)

        then:
        2 * elasticClient.index(_,_)
    }
}
