package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;

class WaitForThreadsNodeOutputImpl extends NodeOutputImpl implements WaitForThreadsNodeOutput {

    private final ThreadSpec.Builder threadSpec;

    public WaitForThreadsNodeOutputImpl(String nodeName, WorkflowThreadImpl parent, ThreadSpec.Builder threadSpec) {
        super(nodeName, parent);
        this.threadSpec = threadSpec;
    }

    public WaitForThreadsNodeOutput handleExceptionOnChild(String exceptionName, ThreadFunc handler) {
        String threadName = "exn-handler-" + this.nodeName + "-"
                + (exceptionName != null ? exceptionName : FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION);
        threadName = parent.getParent().addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        if (exceptionName != null) {
            handlerDef.setSpecificFailure(exceptionName);
        } else {
            handlerDef.setAnyFailureOfType(FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION);
        }

        parent.addFailureHandlerOnWaitForThreadsNode(this, handlerDef.build());

        return this;
    }

    @Override
    public WaitForThreadsNodeOutput handleErrorOnChild(LHErrorType errorType, ThreadFunc handler) {
        String threadName = "error-handler-" + this.nodeName + "-"
                + (errorType != null ? errorType.name() : FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR);
        threadName = parent.getParent().addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        if (errorType != null) {
            handlerDef.setSpecificFailure(errorType.name());
        } else {
            handlerDef.setAnyFailureOfType(FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR);
        }

        parent.addFailureHandlerOnWaitForThreadsNode(this, handlerDef.build());

        return this;
    }

    @Override
    public WaitForThreadsNodeOutput handleAnyFailureOnChild(ThreadFunc handler) {
        String threadName = "failure-handler-" + this.nodeName + "-ANY_FAILURE";
        threadName = parent.getParent().addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        parent.addFailureHandlerOnWaitForThreadsNode(this, handlerDef.build());
        return this;
    }
}
