package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.LHTestException;

public class StepExecutionException extends LHTestException {

    private final int id;
    private final WfRunId wfRunId;
    private final String message;

    public StepExecutionException(int id, WfRunId wfRunId, String message) {
        super();
        this.id = id;
        this.wfRunId = wfRunId;
        this.message = message;
    }

    public StepExecutionException(int id, WfRunId wfRunId, Throwable cause) {
        super(cause);
        this.id = id;
        this.wfRunId = wfRunId;
        this.message = cause.getMessage();
    }

    @Override
    public String getMessage() {
        return String.format("WfRun %s Failed to execute step %s: %s", LHLibUtil.wfRunIdToString(wfRunId), id, message);
    }
}
