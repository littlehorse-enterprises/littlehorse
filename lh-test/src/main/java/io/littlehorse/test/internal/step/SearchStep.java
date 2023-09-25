package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.test.CapturedResult;
import io.littlehorse.test.WfRunTestContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

public class SearchStep<I, O> implements Step {

    private final CapturedResult<O> resultHolder;
    private final Function<WfRunTestContext, I> requestBuilder;

    private final Method targetRpc;

    public SearchStep(
            Class<I> requestType, Function<WfRunTestContext, I> requestBuilder, CapturedResult<O> resultHolder) {
        this.resultHolder = resultHolder;
        this.requestBuilder = requestBuilder;
        this.targetRpc = findTargetRpc(requestType, resultHolder.type());
    }

    private Method findTargetRpc(Class<?> requestType, Class<?> responseType) {
        for (Method declaredMethod : LHPublicApiBlockingStub.class.getDeclaredMethods()) {
            boolean match = Arrays.equals(declaredMethod.getParameterTypes(), new Class[] {requestType})
                    && declaredMethod.getReturnType().equals(responseType);
            if (match) {
                return declaredMethod;
            }
        }
        throw new RuntimeException("not found!");
    }

    @Override
    public void execute(Object context, LHPublicApiBlockingStub lhClient) {
        WfRunTestContext wfRunContext = context::toString;
        try {
            Object response = targetRpc.invoke(lhClient, requestBuilder.apply(wfRunContext));
            resultHolder.set(resultHolder.type().cast(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
