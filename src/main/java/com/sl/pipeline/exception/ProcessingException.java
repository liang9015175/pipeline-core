package com.sl.pipeline.exception;

import lombok.Data;

@Data
public class ProcessingException extends StageException {
    private Object data;
    private Throwable throwable;

    public ProcessingException(Object data,Throwable e){
        this.data=data;
        this.throwable=e;
    }
}
