package io.littlehorse.sdk.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class LHJsonProcessingException extends Exception {

    public LHJsonProcessingException(JsonProcessingException exn) {
        super(exn);
    }
}
