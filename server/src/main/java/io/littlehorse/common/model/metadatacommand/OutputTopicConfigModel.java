package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class OutputTopicConfigModel extends LHSerializable<OutputTopicConfig> {
    private OutputTopicRecordingLevel defaultRecordingLevel;

    @Override
    public Class<OutputTopicConfig> getProtoBaseClass() {
        return OutputTopicConfig.class;
    }

    @Override
    public OutputTopicConfig.Builder toProto() {
        OutputTopicConfig.Builder result =
                OutputTopicConfig.newBuilder().setDefaultRecordingLevel(defaultRecordingLevel);
        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        OutputTopicConfig p = (OutputTopicConfig) proto;
        this.defaultRecordingLevel = p.getDefaultRecordingLevel();
    }

    public OutputTopicRecordingLevel getDefaultRecordingLevel() {
        return this.defaultRecordingLevel;
    }

    public void setDefaultRecordingLevel(final OutputTopicRecordingLevel defaultRecordingLevel) {
        this.defaultRecordingLevel = defaultRecordingLevel;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OutputTopicConfigModel)) return false;
        final OutputTopicConfigModel other = (OutputTopicConfigModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$defaultRecordingLevel = this.getDefaultRecordingLevel();
        final Object other$defaultRecordingLevel = other.getDefaultRecordingLevel();
        if (this$defaultRecordingLevel == null
                ? other$defaultRecordingLevel != null
                : !this$defaultRecordingLevel.equals(other$defaultRecordingLevel)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OutputTopicConfigModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $defaultRecordingLevel = this.getDefaultRecordingLevel();
        result = result * PRIME + ($defaultRecordingLevel == null ? 43 : $defaultRecordingLevel.hashCode());
        return result;
    }
}
