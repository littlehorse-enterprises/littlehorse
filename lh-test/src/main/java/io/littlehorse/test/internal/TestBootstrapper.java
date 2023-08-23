package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;

public interface TestBootstrapper {
    LHWorkerConfig getWorkerConfig();

    LHPublicApiBlockingStub getLhClient();
}
