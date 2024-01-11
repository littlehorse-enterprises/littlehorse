package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.LHTestException;
import io.littlehorse.test.internal.TestExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStep implements Step {

    protected final int id;
    private Logger logger = LoggerFactory.getLogger(Step.class);

    public AbstractStep(int id) {
        this.id = id;
    }

    @Override
    public void execute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        try {
            tryExecute(context, lhClient);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println(context.toString());
            printWfRun(context.getWfRunId(), lhClient);
            this.handleException(throwable);
        }
    }

    private void printWfRun(WfRunId id, LittleHorseBlockingStub lhClient) {
        WfRun wfRun = lhClient.getWfRun(id);
        logger.debug(LHLibUtil.protoToJson(wfRun));
    }

    protected void handleException(Throwable ex) throws LHTestException {
        throw new StepExecutionException(id, ex);
    }

    abstract void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient);
}
