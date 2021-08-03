# Daedalus
ElasticSearch Bulk Data Indexer Library

[![Daedalus CI Pipeline](https://github.com/arturfigueira/daedalus/actions/workflows/gradle.yml/badge.svg)](https://github.com/arturfigueira/daedalus/actions/workflows/gradle.yml)
[![License](https://img.shields.io/github/license/arturfigueira/daedalus)](https://github.com/arturfigueira/daedalus/blob/feature/readme_contributing/LICENSE)


[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=arturfigueira_daedalus&metric=coverage)](https://sonarcloud.io/dashboard?id=arturfigueira_daedalus)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=arturfigueira_daedalus&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=arturfigueira_daedalus)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=arturfigueira_daedalus&metric=security_rating)](https://sonarcloud.io/dashboard?id=arturfigueira_daedalus)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=arturfigueira_daedalus&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=arturfigueira_daedalus)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=arturfigueira_daedalus&metric=bugs)](https://sonarcloud.io/dashboard?id=arturfigueira_daedalus)


Daedalus is a java library designed to facilitate bulk indexing into elastic search. It provides data reading, data conversion and async indexing.


## Overview
Daedalus is divided into two modules: Core and Plugins. The core module is responsible to process data, convert it into an indexable, format based on mappings and elastic's data types, and to bulk index them. Plugins provide concrete classes to read data from external sources.

### Plugin Modules
Daedalus is designed around plugins, which are responsible to provide data, from external sources, to be indexed by the core module. That being said, the core functionality, bulk indexing, can work with whatever data source, CSV files, JSON files and databases.
Is also possible to develop your own plugin, following the interfaces that are available.

The last release contains the following plugins:

|  Plugin       |   File Type   | Data Type |Description |
|---------------|---------------|-----------|------------|
| JsonSource    | File          | JSON      | Reads all files from a specified directory at the file system, converting its content to JSON |


### Client Connections
Daedalus won't be responsible to manage elastic search connections, this must be done externally by the application that is adopting daedalus. The library is designed with [Elastic Search Low Level Rest Client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-low.html) in mind, but is also open to custom client implementations over the same Low level API, provided by Elastic Search

Core module offers a ElasticClient interface, which contains the basic structure that a custom client must have. The library also offers a concrete implementation for Elastic's High-Level Rest client.
```java
//Creates credentials provider for elastics client connection 
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

credentialsProvider.setCredentials(AuthScope.ANY,
new UsernamePasswordCredentials("my_elastic_api_user","my_elastic_pwd"));

//Creates a new high level client using the official Elasticsearch client 
RestHighLevelClient client = new RestHighLevelClient(
RestClient.builder(
new HttpHost("elastic_host", 9200, "http"))
.setHttpClientConfigCallback(
httpAsyncClientBuilder -> httpAsyncClientBuilder
.setDefaultCredentialsProvider(credentialsProvider))

);

//Wraps the official client into daedalus client
var clientWrapper = new HighLevelClientWrapper.Wrap(client).build();
```

## Usage Examples

### Reading data from a folder and indexing
```java
//Path to were daedalus will read json files containing data to be indexed
var path = Path.of("resources/books").toURI());
var jsonSource = new JsonSource(path.toString(), StandardCharsets.UTF_8);

//wraps an Elastic Search client
var clientWrapper = new Wrap(client).capacity(500).build();

//Creates a mapping which defines the elastic index where the data will be indexed
var mappings = new ArrayList<DataMapping>();
mappings.add(new DataMapping("name", ElasticDataType.COMPLETION));
mappings.add(new DataMapping("isbn", ElasticDataType.TEXT));
mappings.add(new DataMapping("category", ElasticDataType.KEYWORD));

//Build a new instance of a data loader. Loader will coordinate the asynchronous operation
var loader = new Loader.Builder()
.elasticClient(clientWrapper)
.mapDataWith(mappings)
.build();

//start loading all files provided by JsonSource, indexing them into elastic`s books index
loader.toIndex("books").from(jsonSource);
```

## License
Copyright 2021 Artur Figueira

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.