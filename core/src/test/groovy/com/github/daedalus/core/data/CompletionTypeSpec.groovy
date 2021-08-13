package com.github.daedalus.core.data

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("CompletionType Specifications")
class CompletionTypeSpec extends Specification {
    @Unroll
    def "Value #raw must be parsed into a array of elements"() {
        given:
        def type = new CompletionType();

        when:
        def parsedValue = type.parse(raw);

        then:
        parsedValue == result

        where:
        raw                                        || result
        null                                       || null
        "Beer"                                     || ["input": ["Beer"]]
        []                                         || ["input": []]
        [] as String[]                             || ["input": []]
        "Straffe Hendrik Heritage 2015 - Armagnac" || ["input": ["Straffe Hendrik Heritage 2015 - Armagnac",
                                                                 "Hendrik Heritage 2015 - Armagnac",
                                                                 "Heritage 2015 - Armagnac",
                                                                 "2015 - Armagnac", "- Armagnac", "Armagnac"]]
        ["Element A", "Element B"]                 || ["input": ["Element A", "Element B"]]
        ["Element A", "Element B"] as String[]     || ["input": ["Element A", "Element B"]]
    }

    @Unroll
    def "Value #raw will throw when parsed"() {
        given:
        def type = new CompletionType();

        when:
        type.parse(raw);

        then:
        thrown(IncorrectTypeException)

        where:
        raw << [true, 10, 168L, new Date(), [10, 65], ["ABCD", 65], ["AB", true] as Object[]]
    }

    @Unroll
    def "Value #raw as CompletionType is #expected"() {
        given:
        def type = new CompletionType();

        when:
        def result = type.isA(raw);

        then:
        result == expected

        where:
        raw                                     || expected
        ["ABC", "DFG"] as String[]              || false
        ["ABC", ""] as String[]                 || false
        [125, "ABV"]                            || false
        ["ABV", true]                           || false
        12.6                                    || false
        true                                    || false
        new Date()                              || false
        100L                                    || false
        null                                    || false
        ""                                      || true
        "Beer"                                  || true
        []                                      || true
        ["", ""]                                || true
        ["ElA", "ElB"]                          || true
        ["input": []]                           || true
        ["input": ["ABC", "DEF"]]               || true
        [["input": "ABC"], ["input": "DEF"]]    || true
        [["input": ["X", "B"]], ["input": "Z"]] || true
        ["input": ["ElA", 125]]                 || false
        [["input": "ABC"], ["input": 125]]      || false

    }
}
