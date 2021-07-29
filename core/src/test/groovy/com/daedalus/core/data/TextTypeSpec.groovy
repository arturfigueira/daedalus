package com.daedalus.core.data

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("TextTypeSpec Test case")
class TextTypeSpec extends Specification {
    @Unroll
    def "Value #raw must be parsed to a text #result"(){
        given:
        def textType = new TextType();

        when:
        def parsedValue = textType.parse(raw);

        then:
        parsedValue == result

        where:
        raw                     ||  result
        null                    ||  null
        "app"                   ||  "app"
        1                       ||  "1"
        100L                    ||  "100"
        true                    ||  "true"
        false                   ||  "false"
        123 as byte             ||  "123"
        1.36 as double          ||  "1.36"
        0.25 as float           ||  "0.25"
        28 as short             ||  "28"
    }
}
