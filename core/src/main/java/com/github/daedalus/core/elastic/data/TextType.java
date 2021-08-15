package com.github.daedalus.core.elastic.data;

import com.github.daedalus.core.elastic.IncorrectTypeException;

public class  TextType implements DataType<String> {

    @Override
    public String parse(Object rawObject) throws IncorrectTypeException {
        return rawObject == null ? null : rawObject.toString();
    }

    @Override
    public boolean isA(final Object object) {
        return object instanceof String;
    }
}