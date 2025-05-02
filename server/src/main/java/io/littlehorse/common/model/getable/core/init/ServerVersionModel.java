package io.littlehorse.common.model.getable.core.init;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ServerVersion;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class ServerVersionModel extends LHSerializable<ServerVersion> {

    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private Optional<String> preReleaseIdentifier;

    public ServerVersionModel() {}

    public ServerVersionModel(int majorVersion, int minorVersion, int patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.preReleaseIdentifier = Optional.empty();
    }

    public ServerVersionModel(int majorVersion, int minorVersion, int patchVersion, String preReleaseIdentifier) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.preReleaseIdentifier = Optional.of(preReleaseIdentifier);
    }

    @Override
    public ServerVersion.Builder toProto() {
        ServerVersion.Builder severVersionBuilder = ServerVersion.newBuilder()
                .setMajorVersion(majorVersion)
                .setMinorVersion(minorVersion)
                .setPatchVersion(patchVersion);

        if (preReleaseIdentifier.isPresent()) {
            severVersionBuilder.setPreReleaseIdentifier(preReleaseIdentifier.get());
        }

        return severVersionBuilder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ServerVersion serverVersion = (ServerVersion) proto;
        this.majorVersion = serverVersion.getMajorVersion();
        this.minorVersion = serverVersion.getMinorVersion();
        this.patchVersion = serverVersion.getPatchVersion();

        if (serverVersion.hasPreReleaseIdentifier()) {
            this.preReleaseIdentifier = Optional.of(serverVersion.getPreReleaseIdentifier());
        } else {
            this.preReleaseIdentifier = Optional.empty();
        }
    }

    @Override
    public Class<ServerVersion> getProtoBaseClass() {
        return ServerVersion.class;
    }
}
