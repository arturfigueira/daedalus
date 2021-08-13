package com.github.daedalus.core.stream

import spock.lang.Specification
import spock.lang.Unroll

class DataReaderSpec extends Specification {

    @Unroll
    def "Criteria should throw when constructed with incorrect arguments"(page, size){
        when:
        new DataReader.Criteria(page, size)

        then:
        thrown(IllegalArgumentException)

        where:
        page | size
        -1   | 10
        10   | 0
        10   | -1
    }

    def "Criteria should allow starting from page zero"(){
        given:
        def criteria = new DataReader.Criteria(0, 100)

        when:
        def currentPage = criteria.getPage()

        then:
        currentPage == 0
    }

    @Unroll
    def "When at page #page with size #size, start at should be #expected"(){
        given:
        def criteria = new DataReader.Criteria(page, size)

        when:
        def startAt = criteria.startAt()

        then:
        expected == startAt

        where:
        page | size | expected
        0    | 100  | 0
        1    | 100  | 100
        5    | 25   | 125
    }

    @Unroll
    def "When at page #page with size #size, until should be #expected"(){
        given:
        def criteria = new DataReader.Criteria(page, size)

        when:
        def until = criteria.until()

        then:
        expected == until

        where:
        page | size | expected
        0    | 100  | 99
        1    | 100  | 199
        5    | 25   | 149
    }
}
