package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

public interface TestBootstrapper {
    LHConfig getWorkerConfig();

    LittleHorseBlockingStub getLhClient();

    LittleHorseBlockingStub getAnonymousClient();
}
