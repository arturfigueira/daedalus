package com.github.daedalus.core.data

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("IPType Specifications")
class IpTypeSpec extends Specification {
    @Unroll
    def "#raw must be parsed to a text #result"(){
        given:
        def textType = new IpType();

        when:
        def parsedValue = textType.parse(raw);

        then:
        parsedValue == result

        where:
        raw                     ||  result
        null                    ||  null
        "127.0.0.1"             ||  "127.0.0.1"
        "2001:DB8:AD:F:0:0:0:1" ||  "2001:DB8:AD:F:0:0:0:1"
    }

    @Unroll
    def "#raw must thrown when parsed"(){
        given:
        def textType = new IpType();

        when:
        textType.parse(raw);

        then:
        thrown(IncorrectTypeException)

        where:
        raw << [1, true, "ABCDE", "127.0.", "256.1.0.0", "127.0.0.0.1", "127.0.1"]
    }

    @Unroll
    def "#raw as an IP is #expectedResult"(){
        given:
        def textType = new IpType();

        when:
        def result = textType.isA(raw)

        then:
        result == expectedResult

        where:
        raw                                         ||  expectedResult
        "127.0.0.1"                                 ||  true
        "255.255.255.0"                             ||  true
        "192.168.1.1"                               ||  true
        "239.255.255.255"                           ||  true
        "0.0.0.1"                                   ||  true
        "10.0.0.0"                                  ||  true

        "127-0-0-1"                                 ||  false
        "127001"                                    ||  false
        "127.001"                                   ||  false
        "127.0.0"                                   ||  false
        "256.255.255.0"                             ||  false
        "192.168.1.1.0"                             ||  false
        "A.255.255.255"                             ||  false
        "SAMPLE"                                    ||  false
        10.0                                        ||  false
        new Date()                                  ||  false

        "2001:db8:85a3:3fa::7344"                   ||  true
        "2001:0db8:85a3:03fa:0000:0000:0000:7344"   ||  true
        "::1"                                       ||  true
        "0000:0000:0000:0000:0000:FFFF:C0A8:0101"   ||  true
        "::ffff:c0a8:101"                           ||  true
        "::ffff:192.168.1.1"                        ||  true
    }
}
