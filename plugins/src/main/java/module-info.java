module com.github.daedalus.plugins {
  requires static lombok;
  requires com.github.daedalus.core;
  requires com.google.gson;

  exports com.github.daedalus.plugins.json;
}