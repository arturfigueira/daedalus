module com.github.daedalus.core {

  requires static lombok;
  requires elasticsearch;
  requires elasticsearch.rest.high.level.client;
  requires elasticsearch.rest.client;
  requires elasticsearch.x.content;
  requires commons.validator;

  exports com.github.daedalus.core.stream;
  exports com.github.daedalus.core.process;
  exports com.github.daedalus.core.process.client;
  exports com.github.daedalus.core.elastic;
  exports com.github.daedalus.core.elastic.data;

}