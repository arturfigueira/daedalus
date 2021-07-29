package com.daedalus.core.data

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Generic Number Type Specifications")
class NumberTypeSpec extends Specification {

    @Unroll
    def "#raw should be converted to #expected as #clazz"(){
        given:
        def dataType = new NumberType(clazz)

        when:
        def result = dataType.parse(raw)

        then:
        result == expected

        where:
        raw                 |   clazz           ||  expected
        -10 as short        |   Short.class     ||  -10 as short
        10 as short         |   Short.class     ||  10 as short
        "125"               |   Short.class     ||  125 as short

        -1000               |   Integer.class   ||  -1000
        250                 |   Integer.class   ||  250
        "125"               |   Integer.class   ||  125

        "125"               |   Long.class      ||  125L
        -1000L              |   Long.class      ||  -1000L
        250L                |   Long.class      ||  250

        "127"               |   Byte.class      ||  127 as byte
        -24 as byte         |   Byte.class      ||  -24 as byte
        8 as byte           |   Byte.class      ||  8 as byte

        2.5 as float        |   Float.class     ||  2.5 as float
        -25.368 as float    |   Float.class     ||  -25.368 as float
        "36.689"            |   Float.class     ||  36.689 as float

        2.5 as double       |   Double.class    ||  2.5 as double
        -25.368 as double   |   Double.class    ||  -25.368 as double
        "36.689"            |   Double.class    ||  36.689 as double
    }

    @Unroll
    def "Parsing #raw to #clazz should throw"(){
        given:
        def dataType = new NumberType(clazz)

        when:
        dataType.parse(raw)

        then:
        thrown(IncorrectTypeException)

        where:
        raw                                 |   clazz
        "ABC"                               |   Short.class
        Short.MAX_VALUE + 1                 |   Short.class
        Short.MIN_VALUE - 1                 |   Short.class
        (Short.MAX_VALUE + 1).toString()    |   Short.class
        10 as Integer                       |   Short.class
        Integer.MAX_VALUE                   |   Short.class
        true                                |   Short.class
        new Date()                          |   Short.class

        // Integer & Long Overflows, so there`s no meaning on testing max and min values
        "ABC"                               |   Integer.class
        10L                                 |   Integer.class
        true                                |   Integer.class
        new Date()                          |   Integer.class

        "ABC"                               |   Long.class
        10 as Integer                       |   Long.class
        true                                |   Long.class
        new Date()                          |   Long.class

        "ABC"                               |   Byte.class
        Byte.MAX_VALUE + 1                  |   Byte.class
        Byte.MIN_VALUE - 1                  |   Byte.class
        (Byte.MAX_VALUE + 1).toString()     |   Byte.class
        10 as Integer                       |   Byte.class
        350                                 |   Byte.class
        true                                |   Byte.class
        new Date()                          |   Byte.class

        "ABC"                               |   Float.class
        10 as Integer                       |   Float.class
        true                                |   Float.class
        new Date()                          |   Float.class

        "ABC"                               |   Double.class
        10 as Integer                       |   Double.class
        true                                |   Double.class
        new Date()                          |   Double.class
    }

    @Unroll
    def "#raw should be #expected for a #clazz"() {
        given:
        def dataType = new NumberType(clazz)

        when:
        def result = dataType.isA(raw)

        then:
        result == expected

        where:
        raw             |   clazz           || expected
        10 as short     |   Short.class     || true
        "10"            |   Short.class     || false
        11.3            |   Short.class     || false
        100 as int      |   Short.class     || false

        10 as int       |   Integer.class   || true
        "10"            |   Integer.class   || false
        11.3            |   Integer.class   || false
        100 as short    |   Integer.class   || false

        10L             |   Long.class      || true
        "10"            |   Long.class      || false
        11.3            |   Long.class      || false
        100 as short    |   Long.class      || false

        127 as byte     |   Byte.class      || true
        "10"            |   Byte.class      || false
        11.3            |   Byte.class      || false
        100 as int      |   Byte.class      || false

        127.5 as float  |   Float.class     || true
        "10"            |   Float.class     || false

        127.5 as double |   Double.class    || true
        "10"            |   Double.class    || false
    }
}
