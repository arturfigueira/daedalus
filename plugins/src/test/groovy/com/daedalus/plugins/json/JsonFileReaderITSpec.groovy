package com.daedalus.plugins.json


import com.daedalus.core.stream.DataReader
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Path

@Title("Json File Reader Integration Specifications")
class JsonFileReaderITSpec extends Specification {

    @Shared def fileReader

    def setupSpec(){
        def path = Path.of(getClass().getResource('/samples/users.json').toURI())
        fileReader = new JsonFileReader(path.toString(), StandardCharsets.UTF_8)
    }

    @Unroll
    def "Reader should paginate reading according to the criteria"(){
        given:
        def criteria = new DataReader.Criteria(page, 1)

        when:
        def dataNodes = fileReader.read(criteria)

        then:
        dataNodes.size() == 1
        dataNodes.get(0).getProperties().size() == 5
        dataNodes.get(0).getProperties().get("name") == name
        dataNodes.get(0).getProperties().get("age") == age
        dataNodes.get(0).getProperties().get("birthday") == birthday
        dataNodes.get(0).getProperties().get("nationality") == nationality
        dataNodes.get(0).getProperties().get("createdDate") == createdDate

        where:
        page || name            |   age |   birthday    | nationality       | createdDate
        2    || "Mac Doe"       |   30  | "04/10/1991"  | "Irish"           | "Wed Jun 05 2021 11:15:31 GMT-0300"
        0    || "Johan Doe"     |   22  | "05/08/1999"  | "German"          | "Wed Jun 09 2021 11:15:31 GMT-0300"
        9    ||  "Josh Doe"     |   37  | "08/27/1984"  | "Canadian"        | "Wed Jun 08 2021 09:15:00 GMT-0300"
    }

    def "Reader should return all elements if page size is greater than the json array"(){
        given:
        def criteria = new DataReader.Criteria(0, 15)

        when:
        def dataNodes = fileReader.read(criteria)

        then:
        dataNodes.size() == 10
        dataNodes.get(0).getProperties().size() == 5
        dataNodes.get(0).getProperties().get("name") == "Johan Doe"
        dataNodes.get(9).getProperties().get("name") == "Josh Doe"
    }

    def "Reader should return an empty list if is requested more pages than available"(){
        given:
        def criteria = new DataReader.Criteria(2, 10)

        when:
        def dataNodes = fileReader.read(criteria)

        then:
        dataNodes.size() == 0
    }
}
