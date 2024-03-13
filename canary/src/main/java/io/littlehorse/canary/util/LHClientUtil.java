package io.littlehorse.canary.util;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ServerVersionResponse;

public class LHClientUtil {

    private LHClientUtil() {}

    public static String getServerVersion(final LittleHorseBlockingStub stub) {
        final ServerVersionResponse response = stub.getServerVersion(Empty.getDefaultInstance());
        return "%s.%s.%s%s"
                .formatted(
                        response.getMajorVersion(),
                        response.getMinorVersion(),
                        response.getPatchVersion(),
                        response.hasPreReleaseIdentifier() ? "-" + response.getPreReleaseIdentifier() : "");
    }
}
