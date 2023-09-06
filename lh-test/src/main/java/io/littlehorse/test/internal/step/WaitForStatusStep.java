package io.littlehorse.test.internal.step;

import io.littlehorse.test.LHClientTestWrapper;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.awaitility.Awaitility;

public class WaitForStatusStep<V> implements Step {

    private final Function<Object, V> statusFunction;
    private final V expectedStatus;

    public WaitForStatusStep(Function<Object, V> statusFunction, V expectedStatus) {
        this.statusFunction = statusFunction;
        this.expectedStatus = expectedStatus;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        Callable<V> statusFunctionExecution = () -> statusFunction.apply(context);
        Awaitility.await().until(statusFunctionExecution, currentStatus -> currentStatus.equals(expectedStatus));
    }
}
