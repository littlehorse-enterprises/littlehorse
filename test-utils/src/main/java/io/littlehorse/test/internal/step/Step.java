package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

public interface Step {
    void execute(Object context, LittleHorseBlockingStub lhClient);
}
