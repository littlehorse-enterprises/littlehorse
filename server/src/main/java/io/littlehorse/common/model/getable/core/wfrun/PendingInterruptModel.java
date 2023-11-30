package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.PendingInterrupt;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class PendingInterruptModel extends LHSerializable<PendingInterrupt> {

    public ExternalEventIdModel externalEventId;
    public String handlerSpecName;
    public int interruptedThreadId;

    public Class<PendingInterrupt> getProtoBaseClass() {
        return PendingInterrupt.class;
    }

    public PendingInterrupt.Builder toProto() {
        PendingInterrupt.Builder out = PendingInterrupt.newBuilder()
                .setHandlerSpecName(handlerSpecName)
                .setExternalEventId(externalEventId.toProto())
                .setInterruptedThreadId(interruptedThreadId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PendingInterrupt p = (PendingInterrupt) proto;
        externalEventId = LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class, context);
        handlerSpecName = p.getHandlerSpecName();
        interruptedThreadId = p.getInterruptedThreadId();
    }

    public static PendingInterruptModel fromProto(PendingInterrupt p, ExecutionContext context) {
        PendingInterruptModel out = new PendingInterruptModel();
        out.initFrom(p, context);
        return out;
    }
}
