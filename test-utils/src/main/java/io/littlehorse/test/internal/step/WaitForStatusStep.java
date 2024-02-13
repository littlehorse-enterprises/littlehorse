package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.test.internal.TestExecutionContext;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

public class WaitForStatusStep<V> extends MatchStep<V> {

    private final Function<TestExecutionContext, V> statusFunction;

    public WaitForStatusStep(Function<TestExecutionContext, V> statusFunction, V expectedStatus, int id) {
        this(statusFunction, expectedStatus, null, id);
    }

    public WaitForStatusStep(
            Function<TestExecutionContext, V> statusFunction, V expectedStatus, Duration timeout, int id) {
        super(expectedStatus, timeout, id);
        this.statusFunction = statusFunction;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        Callable<V> statusFunctionExecution = () -> statusFunction.apply(context);
        waitUntilMatch(statusFunctionExecution);
    }

    @Override
    protected Predicate<? super V> matcher(V expectedStatus) {
        return expectedStatus::equals;
    }
}
