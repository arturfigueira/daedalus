package com.github.daedalus.core.elastic.data;

import com.github.daedalus.core.elastic.IncorrectTypeException;

public interface DataType<R> {
    R parse(final Object rawObject) throws IncorrectTypeException;
    boolean isA(final Object object);
}
