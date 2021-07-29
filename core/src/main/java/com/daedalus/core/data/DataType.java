package com.daedalus.core.data;

interface DataType<R> {
    R parse(final Object rawObject) throws IncorrectTypeException;
    boolean isA(final Object object);
}
