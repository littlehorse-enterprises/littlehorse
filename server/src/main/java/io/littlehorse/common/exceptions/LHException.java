package io.littlehorse.common.exceptions;

public class LHException extends Exception {

    public LHException(Throwable cause, String msg) {
        super(msg, cause);
    }
}
