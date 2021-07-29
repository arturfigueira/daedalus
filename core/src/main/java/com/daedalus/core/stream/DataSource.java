package com.daedalus.core.stream;

public interface DataSource {
    DataReader next() throws DataStreamException;
    boolean hasNext() throws DataStreamException;
}
