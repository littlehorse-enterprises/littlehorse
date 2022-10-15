package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.PendingInterruptPb;
import io.littlehorse.common.proto.PendingInterruptPbOrBuilder;

public class PendingInterrupt extends LHSerializable<PendingInterruptPb> {

    public String externalEventId;
    public String handlerSpecName;
    public int interruptedThreadId;

    public Class<PendingInterruptPb> getProtoBaseClass() {
        return PendingInterruptPb.class;
    }

    public PendingInterruptPb.Builder toProto() {
        PendingInterruptPb.Builder out = PendingInterruptPb
            .newBuilder()
            .setHandlerSpecName(handlerSpecName)
            .setExternalEventId(externalEventId)
            .setInterruptedThreadId(interruptedThreadId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PendingInterruptPbOrBuilder p = (PendingInterruptPbOrBuilder) proto;
        externalEventId = p.getExternalEventId();
        handlerSpecName = p.getHandlerSpecName();
        interruptedThreadId = p.getInterruptedThreadId();
    }

    public static PendingInterrupt fromProto(PendingInterruptPbOrBuilder p) {
        PendingInterrupt out = new PendingInterrupt();
        out.initFrom(p);
        return out;
    }
}
