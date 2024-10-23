package io.littlehorse.common.model.getable.core.init;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.ServerVersion;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ServerVersionModel extends LHSerializable<ServerVersion> {

    private int major_version;
    private int minor_version;
    private int patch_version;

    public ServerVersionModel() {}
    ;

    public ServerVersionModel(int major_version, int minor_version, int patch_version) {
        this.major_version = major_version;
        this.minor_version = minor_version;
        this.patch_version = patch_version;
    }

    @Override
    public ServerVersion.Builder toProto() {
        return ServerVersion.newBuilder()
                .setMajorVersion(major_version)
                .setMinorVersion(minor_version)
                .setPatchVersion(patch_version);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ServerVersion serverVersion = (ServerVersion) proto;
        this.major_version = serverVersion.getMajorVersion();
        this.minor_version = serverVersion.getMinorVersion();
        this.patch_version = serverVersion.getPatchVersion();
    }

    @Override
    public Class<ServerVersion> getProtoBaseClass() {
        return ServerVersion.class;
    }
}
