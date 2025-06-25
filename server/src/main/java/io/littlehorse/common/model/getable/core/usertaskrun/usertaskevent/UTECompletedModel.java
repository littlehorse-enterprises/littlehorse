package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECompleted;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class UTECompletedModel extends LHSerializable<UTECompleted> {

    public UTECompletedModel() {}

    public Class<UTECompleted> getProtoBaseClass() {
        return UTECompleted.class;
    }

    public UTECompleted.Builder toProto() {
        UTECompleted.Builder out = UTECompleted.newBuilder();
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {}
}
