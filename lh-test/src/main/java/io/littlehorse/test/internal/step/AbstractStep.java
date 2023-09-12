package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.test.internal.LHTestException;

public abstract class AbstractStep implements Step {

    protected final int id;

    public AbstractStep(int id) {
        this.id = id;
    }

    @Override
    public void execute(Object context, LHPublicApiBlockingStub lhClient) {
        try {
            tryExecute(context, lhClient);
        } catch (Throwable throwable) {
            this.handleException(throwable);
        }
    }

    protected void handleException(Throwable ex) throws LHTestException {
        throw new StepExecutionException(id, ex);
    }

    abstract void tryExecute(Object context, LHPublicApiBlockingStub lhClient);
}
