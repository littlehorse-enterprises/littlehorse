package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.test.internal.TestExecutionContext;

/**
 * USE WITH CAUTION. Improper use of this class can cause flaky tests. Use only when
 * you are very sure that it won't introduce undue delay or flakiness.
 */
public class SleepStep extends AbstractStep {

    private long millis;

    public SleepStep(long millis, int id) {
        super(id);
        this.millis = millis;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
    }
}
