package com.daedalus.core.process.client;

class ClientAsyncException extends RuntimeException{

  public ClientAsyncException(String message) {
    super(message);
  }

  public ClientAsyncException(String message, Throwable cause) {
    super(message, cause);
  }
}
