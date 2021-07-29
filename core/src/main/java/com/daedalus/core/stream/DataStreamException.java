package com.daedalus.core.stream;

public class DataStreamException extends Exception{

    public DataStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStreamException(Throwable cause) {
        super(cause);
    }
}
