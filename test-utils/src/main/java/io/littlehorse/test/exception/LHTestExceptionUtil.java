package io.littlehorse.test.exception;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.Callable;

public class LHTestExceptionUtil {

    public static boolean isNotFoundException(Throwable exn) {
        return exn instanceof StatusRuntimeException
                && ((StatusRuntimeException) exn).getStatus().getCode() == Code.NOT_FOUND;
    }

    public static boolean throwsNotFound(Callable<Void> callable) {
        StatusRuntimeException caught = null;
        try {
            callable.call();
        } catch (StatusRuntimeException exn) {
            if (LHTestExceptionUtil.isNotFoundException(exn)) {
                caught = exn;
            }
        } catch (Exception exn) {
            exn.printStackTrace();
        }
        return caught != null;
    }
}
