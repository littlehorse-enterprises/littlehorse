package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskEventPb.UTECancelledPb;

public class UTECancelled extends LHSerializable<UTECancelledPb> {

    public Class<UTECancelledPb> getProtoBaseClass() {
        return UTECancelledPb.class;
    }

    public UTECancelledPb.Builder toProto() {
        UTECancelledPb.Builder out = UTECancelledPb.newBuilder();
        return out;
    }

    public void initFrom(Message proto) {
        // nothing to do;
    }
}
