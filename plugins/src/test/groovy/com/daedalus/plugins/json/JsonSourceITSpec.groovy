package com.daedalus.plugins.json

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Title("Json Source Integration Specifications")
class JsonSourceITSpec extends Specification {

    @Shared File tempDir

    def setup(){
        tempDir = File.createTempDir()
    }

    def cleanup(){
        tempDir.listFiles().each {it.delete()}
        tempDir.delete()
    }

    @Unroll
    def "Won't construct with incorrect arguments"(rootDir, charset){
        when:
        new JsonSource(rootDir, charset)

        then:
        thrown(IllegalArgumentException)

        where:
        rootDir                                                 | charset
        ""                                                      | StandardCharsets.UTF_8
        "    "                                                  | StandardCharsets.UTF_8
        null                                                    | StandardCharsets.UTF_8
        Path.of(getClass().getResource("").toURI()).toString()  | null
    }

    def "Should navigate over all files at rootDir"(){
        given:
        10.times{
            File.createTempFile("book.$it", "-sample.json", tempDir)
        }

        def jsonSource = JsonSource.withDefaultCharset(tempDir.getAbsolutePath())
        def count = 0;

        when:
        while(jsonSource.hasNext()) {
            jsonSource.next()
            count++
        }

        then:
        count == 10
    }

    def "Should ignore inner folders"(){
        given:
        5.times{
            File.createTempFile("book.$it", "-sample.json", tempDir)
        }

        def innerPath = Path.of(tempDir.getAbsolutePath(), "innerFolder")
        Files.createDirectory(innerPath)
        File.createTempFile("book.100", "-sample.json", new File(innerPath.toString()))

        def jsonSource = JsonSource.withDefaultCharset(tempDir.getAbsolutePath())
        def count = 0;

        when:
        while(jsonSource.hasNext()) {
            jsonSource.next()
            count++
        }

        then:
        count == 5
    }

    def "Shouldn't iterate if the folder is empty"(){
        given:
        def jsonSource = JsonSource.withDefaultCharset(tempDir.getAbsolutePath())

        when:
        def hasNext = jsonSource.hasNext()

        then:
        !hasNext
    }
}
