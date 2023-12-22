package io.littlehorse.test.exception;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;

public class LHTestExceptionUtil {

    public static boolean isNotFoundException(Throwable exn) {
        return exn instanceof StatusRuntimeException
                && ((StatusRuntimeException) exn).getStatus().getCode() == Code.NOT_FOUND;
    }
}
