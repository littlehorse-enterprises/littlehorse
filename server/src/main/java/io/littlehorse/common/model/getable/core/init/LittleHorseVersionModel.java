package io.littlehorse.common.model.getable.core.init;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.LittleHorseVersion;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class LittleHorseVersionModel extends LHSerializable<LittleHorseVersion> {

    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private Optional<String> preReleaseIdentifier;

    public LittleHorseVersionModel() {}

    public LittleHorseVersionModel(int majorVersion, int minorVersion, int patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.preReleaseIdentifier = Optional.empty();
    }

    public LittleHorseVersionModel(int majorVersion, int minorVersion, int patchVersion, String preReleaseIdentifier) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.preReleaseIdentifier = Optional.of(preReleaseIdentifier);
    }

    @Override
    public LittleHorseVersion.Builder toProto() {
        LittleHorseVersion.Builder severVersionBuilder = LittleHorseVersion.newBuilder()
                .setMajorVersion(majorVersion)
                .setMinorVersion(minorVersion)
                .setPatchVersion(patchVersion);

        if (preReleaseIdentifier.isPresent()) {
            severVersionBuilder.setPreReleaseIdentifier(preReleaseIdentifier.get());
        }

        return severVersionBuilder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        LittleHorseVersion LittleHorseVersion = (LittleHorseVersion) proto;
        this.majorVersion = LittleHorseVersion.getMajorVersion();
        this.minorVersion = LittleHorseVersion.getMinorVersion();
        this.patchVersion = LittleHorseVersion.getPatchVersion();

        if (LittleHorseVersion.hasPreReleaseIdentifier()) {
            this.preReleaseIdentifier = Optional.of(LittleHorseVersion.getPreReleaseIdentifier());
        } else {
            this.preReleaseIdentifier = Optional.empty();
        }
    }

    @Override
    public Class<LittleHorseVersion> getProtoBaseClass() {
        return LittleHorseVersion.class;
    }
}
