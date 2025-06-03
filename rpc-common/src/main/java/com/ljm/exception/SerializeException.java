package com.ljm.exception;

/**
 * 序列化异常
 * @author ljm
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String message) {
        super(message);
    }
    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
