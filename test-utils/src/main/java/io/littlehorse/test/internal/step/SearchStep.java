package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.test.CapturedResult;
import io.littlehorse.test.internal.TestExecutionContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

public class SearchStep<I, O> implements Step {

    private final CapturedResultImpl<O> resultHolder;
    private final Function<TestExecutionContext, I> requestBuilder;

    private final Method targetRpc;

    public SearchStep(
            Class<I> requestType, Function<TestExecutionContext, I> requestBuilder, CapturedResult<O> resultHolder) {
        if (!(resultHolder instanceof CapturedResultImpl)) {
            throw new IllegalArgumentException("resultHolder must be CapturedResultImpl");
        }
        this.resultHolder = (CapturedResultImpl) resultHolder;
        this.requestBuilder = requestBuilder;
        this.targetRpc = findTargetRpc(requestType, resultHolder.type());
    }

    private Method findTargetRpc(Class<?> requestType, Class<?> responseType) {
        for (Method declaredMethod : LittleHorseBlockingStub.class.getDeclaredMethods()) {
            boolean match = Arrays.equals(declaredMethod.getParameterTypes(), new Class[] {requestType})
                    && declaredMethod.getReturnType().equals(responseType);
            if (match) {
                return declaredMethod;
            }
        }
        throw new IllegalArgumentException(String.format(
                "There is no RPC method registered for Request type %s and Response type %s",
                requestType.getSimpleName(), responseType.getSimpleName()));
    }

    @Override
    public void execute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        try {
            Object response = targetRpc.invoke(lhClient, requestBuilder.apply(context));
            resultHolder.set(resultHolder.type().cast(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
