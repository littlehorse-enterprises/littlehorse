package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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
}
