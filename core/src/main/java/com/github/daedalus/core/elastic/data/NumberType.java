package com.github.daedalus.core.elastic.data;

import com.github.daedalus.core.elastic.IncorrectTypeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberType<N extends Number> implements DataType<N> {

    protected Class<N> clazz;

    public NumberType(Class<N> clazz) {
        this.clazz = clazz;
    }

    @Override
    public N parse(Object rawObject) throws IncorrectTypeException {
        N value = null;

        if(rawObject != null){
            if(rawObject instanceof String){
                value = parseNumber((String) rawObject);
            }else if(this.isA(rawObject)){
                value = this.clazz.cast(rawObject);
            }else{
                throw new IncorrectTypeException("Unable to parse "+rawObject+" to "+this.clazz.getSimpleName());
            }
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    protected N parseNumber(String value) throws IncorrectTypeException{
        try {
            final var clazzParseMethod = this.getParseMethod();
            return (N) clazzParseMethod.invoke("", value);
        } catch (IllegalAccessException  e) {
            throw new UnsupportedOperationException
                    ("Unsupported parsing for "+value+" to "+this.clazz.getName(), e);
        } catch (ClassCastException | NumberFormatException | InvocationTargetException e) {
            throw new IncorrectTypeException("Unable to parse String "+value+" to "+this.clazz.getSimpleName());
        }
    }

    private Method getParseMethod() {
        Method parseMethod;
        try{
            String clazzSimpleName = this.clazz.getSimpleName();
            final var methodName = "parse" + clazzSimpleName;
            parseMethod = this.clazz.getMethod(methodName, String.class);
        } catch (NoSuchMethodException e) {
            parseMethod = Arrays
                    .stream(this.clazz.getMethods())
                    .filter(method -> method.getName().startsWith("parse"))
                    .findFirst()
                    .orElseThrow(()->new UnsupportedOperationException("Could not find viable parse method at "
                            +this.clazz.getName(), e));
        }
        return parseMethod;
    }

    @Override
    public boolean isA(Object object) {
        return this.clazz.isInstance(object);
    }
}