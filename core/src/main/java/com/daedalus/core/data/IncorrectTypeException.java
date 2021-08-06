package com.daedalus.core.data;

/**
 * Checked exception thrown when a parsing cannot be processed
 */
public class IncorrectTypeException extends Exception{

    /**
     * Construct an instance with given reason/description of the error
     *
     * @param message String with an description of the error
     */
    public IncorrectTypeException(String message) {
        super(message);
    }
}
