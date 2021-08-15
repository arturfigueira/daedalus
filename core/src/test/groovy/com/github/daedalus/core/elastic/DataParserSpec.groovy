package com.github.daedalus.core.elastic

import com.github.daedalus.core.elastic.DataParser
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DataParserSpec extends Specification {

    @Shared DataParser dataParser

    def setup(){
        dataParser = new DataParser.Builder().create()
    }

    def "isA should throws when no data type is provided"(){
        when:
        dataParser.isA("SampleData", null);

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "parse should throws when no data type is provided"(datatype){
        when:
        dataParser.parse("SampleData", datatype);

        then:
        thrown(IllegalArgumentException)

        where:
        datatype << [null, "   "]
    }

    @Unroll
    def "Builder should not throw when invalid arguments is provided"(locale, timezone, format){
        given:
        def builder = new DataParser.Builder()

        when:
        builder.locale(locale).timeZone(timezone).dateFormat(format)

        then:
        thrown(IllegalArgumentException)

        where:
        locale          | timezone                                      | format
        null            | TimeZone.getTimeZone("America/Los_Angeles")   | "mm-dd-yyyy"
        Locale.ENGLISH  | null                                          | "mm-dd-yyyy"
        Locale.ENGLISH  | TimeZone.getTimeZone("America/Los_Angeles")   | null
        Locale.ENGLISH  | TimeZone.getTimeZone("America/Los_Angeles")   | "     "
    }
}
