package com.github.daedalus.core.elastic.data

import com.github.daedalus.core.elastic.IncorrectTypeException
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

class DateTypeSpec extends Specification {

    def "When not configured will parse a string with default settings"(){
        given:
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(TimeZone.getDefault())

        def dateType = new DateType()

        def inputDate = "2021-04-28 16:00:00"
        def expected = dateFormat.parse(inputDate)

        when:
        def result = dateType.parse(inputDate)

        then:
        result == expected
    }

    def "Will parse strings with provided date pattern format"(){
        given:
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(TimeZone.getDefault())

        def dateType = new DateType()
        dateType.setFormat("dd/MM/yyyy HH:mm:ss")

        def expected = dateFormat.parse("2021-04-28 16:00:00")

        when:
        def result = dateType.parse("28/04/2021 16:00:00")

        then:
        result == expected
    }

    def "Will parse strings with the provided Locale"(){
        given:
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(TimeZone.getDefault())

        def dateType = new DateType()
        dateType.setFormat("MMM d, yyyy")
        dateType.setLocale(Locale.ENGLISH)

        def expected = dateFormat.parse("2010-11-01 00:00:00")

        when:
        def result = dateType.parse("Nov 1, 2010")

        then:
        result == expected
    }

    def "Will parse strings to the provided TimeZone"(){
        given:
        def timeZone = TimeZone.getTimeZone("America/Los_Angeles")

        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(timeZone)

        def dateType = new DateType()
        dateType.setFormat("dd/MM/yyyy");
        dateType.setTimeZone(timeZone)

        def expected = dateFormat.parse("2010-11-01 00:00:00")

        when:
        def result = dateType.parse("01/11/2010")

        then:
        result == expected
    }

    def "Will parse long to correspondent epoch date"(){
        given:
        def sampleDate = new Date()
        def dateType = new DateType()

        when:
        def result = dateType.parse(sampleDate.getTime())

        then:
        result == sampleDate
    }

    def "Will parse a date to an equals object"(){
        given:
        def sampleDate = new Date()
        def dateType = new DateType()

        when:
        def result = dateType.parse(sampleDate)

        then:
        result == sampleDate
    }

    def "Will parse null to null"(){
        given:
        def dateType = new DateType()

        when:
        def result = dateType.parse(null)

        then:
        result == null
    }

    @Unroll
    def "Will thrown when trying to parse #input to Date"(input){
        given:
        def dateType = new DateType()

        when:
        dateType.parse(input)

        then:
        thrown(IncorrectTypeException)

        where:
        input << ["  ", 500 as int, true, "15/25/2021 18:00:00" ]
    }
}
