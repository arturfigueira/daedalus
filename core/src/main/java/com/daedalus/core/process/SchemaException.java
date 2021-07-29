package com.daedalus.core.process;

import java.util.List;

public class SchemaException extends Exception{
    public SchemaException(String message) {
        super(message);
    }

    public SchemaException(List<String> messages){
        super(String.join(", ", messages));
    }
}
