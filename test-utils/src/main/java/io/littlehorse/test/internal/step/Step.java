package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.test.internal.TestExecutionContext;

public interface Step {
    void execute(TestExecutionContext context, LittleHorseBlockingStub lhClient);
}
