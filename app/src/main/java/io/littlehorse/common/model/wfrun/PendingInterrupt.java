package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.jlib.common.proto.PendingInterruptPb;

public class PendingInterrupt extends LHSerializable<PendingInterruptPb> {

    public ExternalEventId externalEventId;
    public String handlerSpecName;
    public int interruptedThreadId;

    public Class<PendingInterruptPb> getProtoBaseClass() {
        return PendingInterruptPb.class;
    }

    public PendingInterruptPb.Builder toProto() {
        PendingInterruptPb.Builder out = PendingInterruptPb
            .newBuilder()
            .setHandlerSpecName(handlerSpecName)
            .setExternalEventId(externalEventId.toProto())
            .setInterruptedThreadId(interruptedThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        PendingInterruptPb p = (PendingInterruptPb) proto;
        externalEventId =
            LHSerializable.fromProto(p.getExternalEventId(), ExternalEventId.class);
        handlerSpecName = p.getHandlerSpecName();
        interruptedThreadId = p.getInterruptedThreadId();
    }

    public static PendingInterrupt fromProto(PendingInterruptPb p) {
        PendingInterrupt out = new PendingInterrupt();
        out.initFrom(p);
        return out;
    }
}
