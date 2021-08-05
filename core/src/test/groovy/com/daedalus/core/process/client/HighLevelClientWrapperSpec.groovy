package com.daedalus.core.process.client

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executor

class HighLevelClientWrapperSpec extends Specification {

    static RestHighLevelClient CLIENT

    def setupSpec(){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic","38nuu9Nue3593"));

        CLIENT = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 4200, "http"))
                        .setHttpClientConfigCallback(
                                httpAsyncClientBuilder -> httpAsyncClientBuilder
                                        .setDefaultCredentialsProvider(credentialsProvider)))
    }

    @Unroll
    def "Wrap should throw with invalid arguments"(client, capacity, timeout, executor){
        when:
        new HighLevelClientWrapper.Wrap(client)
                .capacity(capacity)
                .timeout(timeout)
                .executor(executor)
                .build()

        then:
        thrown(IllegalArgumentException)

        where:
        client  | capacity      | timeout       | executor
        null    | 1             | 10            | Mock(Executor.class)
        CLIENT  | -1            | 10            | Mock(Executor.class)
        CLIENT  | 10            | -1            | Mock(Executor.class)
        CLIENT  | 10            | 10            | null
    }
}
