package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.LHTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStep implements Step {

    protected final int id;
    private Logger logger = LoggerFactory.getLogger(Step.class);

    public AbstractStep(int id) {
        this.id = id;
    }

    @Override
    public void execute(Object context, LittleHorseBlockingStub lhClient) {
        try {
            tryExecute(context, lhClient);
        } catch (Throwable throwable) {
            printWfRun(context.toString(), lhClient);
            this.handleException(throwable);
        }
    }

    private void printWfRun(String id, LittleHorseBlockingStub lhClient) {
        WfRunId wfRunId = WfRunId.newBuilder().setId(id).build();
        WfRun wfRun = lhClient.getWfRun(wfRunId);
        logger.debug(LHLibUtil.protoToJson(wfRun));
    }

    protected void handleException(Throwable ex) throws LHTestException {
        throw new StepExecutionException(id, ex);
    }

    abstract void tryExecute(Object context, LittleHorseBlockingStub lhClient);
}
