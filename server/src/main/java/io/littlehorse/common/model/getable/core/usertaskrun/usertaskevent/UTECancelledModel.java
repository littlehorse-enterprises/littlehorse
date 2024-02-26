package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECancelled;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class UTECancelledModel extends LHSerializable<UTECancelled> {

    private String message = "";

    public UTECancelledModel() {}

    public UTECancelledModel(String message) {
        this.message = message;
    }

    public Class<UTECancelled> getProtoBaseClass() {
        return UTECancelled.class;
    }

    public UTECancelled.Builder toProto() {
        UTECancelled.Builder out = UTECancelled.newBuilder().setMessage(message);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UTECancelled p = (UTECancelled) proto;
        this.message = p.getMessage();
    }
}
