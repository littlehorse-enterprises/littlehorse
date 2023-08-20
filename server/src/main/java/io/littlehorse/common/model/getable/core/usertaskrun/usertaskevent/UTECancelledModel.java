package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECancelled;

public class UTECancelledModel extends LHSerializable<UTECancelled> {

    public Class<UTECancelled> getProtoBaseClass() {
        return UTECancelled.class;
    }

    public UTECancelled.Builder toProto() {
        UTECancelled.Builder out = UTECancelled.newBuilder();
        return out;
    }

    public void initFrom(Message proto) {
        // nothing to do;
    }
}
