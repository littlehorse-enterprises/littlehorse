package io.littlehorse.test.internal.step;

import io.littlehorse.test.LHClientTestWrapper;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

public class WaitForStatusStep<V> implements Step {

    private final Function<Object, V> statusFunction;
    private final V expectedStatus;

    private Duration timeout = null;

    public WaitForStatusStep(Function<Object, V> statusFunction, V expectedStatus) {
        this.statusFunction = statusFunction;
        this.expectedStatus = expectedStatus;
    }

    public WaitForStatusStep(Function<Object, V> statusFunction, V expectedStatus, Duration timeout) {
        this(statusFunction, expectedStatus);
        this.timeout = timeout;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        Callable<V> statusFunctionExecution = () -> statusFunction.apply(context);
        ConditionFactory awaitCondition = Awaitility.await();
        if (timeout != null) {
            awaitCondition = awaitCondition.timeout(timeout);
        }
        awaitCondition.until(statusFunctionExecution, currentStatus -> currentStatus.equals(expectedStatus));
    }
}
