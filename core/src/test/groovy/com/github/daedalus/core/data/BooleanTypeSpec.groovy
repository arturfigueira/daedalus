package com.github.daedalus.core.data

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("BooleanType Test case")
class BooleanTypeSpec extends Specification {

    @Unroll
    def "Value #raw must be parsed to boolean #result"(){
        given:
        def booleanType = new BooleanType();

        when:
        def parsedValue = booleanType.parse(raw);

        then:
        parsedValue == result

        where:
        raw     ||  result
        null    ||  null
        true    ||  true
        false   ||  false
        0       ||  false
        1       ||  true
        "0"     ||  false
        "1"     ||  true
        "true"  ||  true
        "false" ||  false
        "True"  ||  true
        "False" ||  false
    }

    @Unroll
    def "Value #raw will throw when parsed"(){
        given:
        def booleanType = new BooleanType();

        when:
        booleanType.parse(raw);

        then:
        thrown(IncorrectTypeException)

        where:
        raw << ["random", 1.258, "36", -5, 10, "falze", 0.1, "1.2", "0.5", -1 ]
    }

    @Unroll
    def "Should be #result when asked if #raw is a boolean"(){
        given:
        def booleanType = new BooleanType();

        when:
        def isA = booleanType.isA(raw);

        then:
        isA == result

        where:
        raw     ||  result
        true    ||  true
        false   ||  true
        "false" ||  false
        "true"  ||  false
        0       ||  false
        1       ||  false
    }
}
