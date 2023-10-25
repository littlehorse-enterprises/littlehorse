package io.littlehorse.test.internal.step;

import io.grpc.StatusRuntimeException;
import io.littlehorse.test.internal.LHTestException;
import io.littlehorse.test.internal.MismatchedConditionException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.EvaluatedCondition;

public abstract class MatchStep<V> extends AbstractStep {

    protected final Duration timeout;

    protected V lastEvaluatedValue;

    private ConditionFactory condition;

    private final V expectedValue;

    public MatchStep(V expectedValue, final Duration timeout, int id) {
        super(id);
        this.timeout = timeout;
        this.expectedValue = expectedValue;
        condition = Awaitility.await();
        if (timeout != null) {
            condition = condition.timeout(timeout);
        }
        condition = condition.conditionEvaluationListener(new AwaitilityConditionalListener());
    }

    public void waitUntilMatch(final Callable<V> supplier) {
        condition.ignoreException(StatusRuntimeException.class).until(supplier, matcher(expectedValue));
    }

    @Override
    protected void handleException(Throwable ex) throws LHTestException {
        if (ex instanceof ConditionTimeoutException) {
            throw new MismatchedConditionException(expectedValue, lastEvaluatedValue, id);
        }
        super.handleException(ex);
    }

    protected abstract Predicate<? super V> matcher(V expectedValue);

    private class AwaitilityConditionalListener implements ConditionEvaluationListener<V> {
        @Override
        public void conditionEvaluated(EvaluatedCondition<V> condition) {
            lastEvaluatedValue = condition.getValue();
        }
    }
}
