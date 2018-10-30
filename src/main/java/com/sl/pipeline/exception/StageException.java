package com.sl.pipeline.exception;

/**
 * stage 异常
 */
public class StageException extends RuntimeException {
    public StageException(){
        super();
    }
    public StageException(String message){
        super(message);
    }
    public StageException(String message,Throwable throwable){
        super(message,throwable);
    }
}
