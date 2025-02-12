package io.littlehorse.sdk.common.exception;

public class LHJsonProcessingException extends Exception {

    public LHJsonProcessingException(String msg) {
        super(new Exception(msg));
    }
}
