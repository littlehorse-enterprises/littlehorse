package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;

public interface TestBootstrapper {
    LHConfig getWorkerConfig();

    LHPublicApiBlockingStub getLhClient();
}
