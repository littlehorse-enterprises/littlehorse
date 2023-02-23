package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.ExtEvtMatchedOePb;
import io.littlehorse.jlib.common.proto.ExtEvtMatchedOePbOrBuilder;

public class ExtEvtMatchedOe extends SubEvent<ExtEvtMatchedOePb> {

    public String extEvtDefName;
    public String extEvtGuid;
    public int threadRunNumber;
    public int nodeRunPosition;

    public Class<ExtEvtMatchedOePb> getProtoBaseClass() {
        return ExtEvtMatchedOePb.class;
    }

    public ExtEvtMatchedOePb.Builder toProto() {
        ExtEvtMatchedOePb.Builder out = ExtEvtMatchedOePb
            .newBuilder()
            .setExtEvtDefName(extEvtDefName)
            .setExtEvtGuid(extEvtGuid)
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExtEvtMatchedOePbOrBuilder p = (ExtEvtMatchedOePbOrBuilder) proto;
        extEvtDefName = p.getExtEvtDefName();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        extEvtGuid = p.getExtEvtGuid();
    }
}
