package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;

public interface Step {
    void execute(Object context, LHPublicApiBlockingStub lhClient);
}
