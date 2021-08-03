package com.daedalus.core.stream


import spock.lang.Specification

class StoreResultSpec extends Specification {
    def "A Success should have no error and be considered stored"(){
        when:
        def success = StoreResult.success()

        then:
        success.stored
        success.getError() == null
    }

    def "A Failure should have an error and be considered not stored"(){
        when:
        def failure = StoreResult.failure(new IllegalAccessException())

        then:
        !failure.stored
        failure.getError().getClass() == IllegalAccessException
    }
}
