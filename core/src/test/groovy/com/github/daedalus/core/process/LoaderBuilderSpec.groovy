package com.github.daedalus.core.process


import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("Bulk Loader Specifications")
class LoaderBuilderSpec extends Specification {

    def "loader builder should throws when providing null dataStore"(){
        when:
        new LoaderBuilder().backupTo(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "loader builder should throws when providing null client"(){
        when:
        new LoaderBuilder().elasticClient(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "loader builder should throws when providing null locale"(){
        when:
        new LoaderBuilder().locale(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "loader builder should throws when providing null timeZone"(){
        when:
        new LoaderBuilder().timeZone(null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "loader builder should throws when providing null or empty format"(format){
        when:
        new LoaderBuilder().dateFormatPattern(null)

        then:
        thrown(IllegalArgumentException)

        where:
        format << [null, "", "   "]
    }

    @Unroll
    def "loader builder should throws when providing null or empty mappings"(mappings){
        when:
        new LoaderBuilder().mappings(null)

        then:
        thrown(IllegalArgumentException)

        where:
        mappings << [null, []]
    }
}
