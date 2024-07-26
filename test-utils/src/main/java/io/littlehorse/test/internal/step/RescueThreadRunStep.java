package io.littlehorse.test.internal.step;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RescueThreadRunRequest;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.function.Consumer;

public class RescueThreadRunStep extends AbstractStep {

    private final int threadRunNumber;
    private final boolean skipCurrentNode;
    private final Consumer<StatusRuntimeException> exceptionConsumer;

    public RescueThreadRunStep(
            int threadRunNumber, boolean skipCurrentNode, Consumer<StatusRuntimeException> exceptionConsumer, int id) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.skipCurrentNode = skipCurrentNode;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub client) {
        try {
            client.rescueThreadRun(RescueThreadRunRequest.newBuilder()
                    .setWfRunId(context.getWfRunId())
                    .setThreadRunNumber(threadRunNumber)
                    .setSkipCurrentNode(skipCurrentNode)
                    .build());
        } catch (StatusRuntimeException exn) {
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(exn);
            } else {
                throw exn;
            }
        }
    }
}
