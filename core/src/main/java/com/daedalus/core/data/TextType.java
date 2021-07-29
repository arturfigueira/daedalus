package com.daedalus.core.data;

class TextType implements DataType<String> {

    //TODO: How to handle Date to String
    @Override
    public String parse(Object rawObject) throws IncorrectTypeException {
        return rawObject == null ? null : rawObject.toString();
    }

    @Override
    public boolean isA(final Object object) {
        return object instanceof String;
    }
}