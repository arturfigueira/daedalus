package com.daedalus.core.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ElasticDataType {
    BOOLEAN(new BooleanType()),
    COMPLETION(new CompletionType()),
    KEYWORD(new TextType()),
    DATE(new DateType()),
    TEXT(new TextType()),
    LONG(new NumberType<>(Long.class)),
    INTEGER(new NumberType<>(Integer.class)),
    SHORT(new NumberType<>(Short.class)),
    BYTE(new NumberType<>(Byte.class)),
    DOUBLE(new NumberType<>(Double.class)),
    FLOAT(new NumberType<>(Float.class)),
    IP(new IpType());

    private final String id;
    private final DataType<?> type;

    ElasticDataType(final DataType<?> t){
        this.id = this.name().toLowerCase();
        this.type = t;
    }

    public boolean isA(final Object object){
        return this.type.isA(object);
    }

    public Object parse(final Object input) throws IncorrectTypeException {
        return this.type.parse(input);
    }

    @Override
    public String toString() {
        return "DataType{" +
                "id='" + id + '\'' +
                '}';
    }
}