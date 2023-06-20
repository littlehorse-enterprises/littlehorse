package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.ExtEvtRegisteredOePb;
import java.util.Date;

public class ExtEvtRegisteredOe extends SubEvent<ExtEvtRegisteredOePb> {

    public String extEvtDefName;
    public String guid;
    public VariableValue content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;

    public Class<ExtEvtRegisteredOePb> getProtoBaseClass() {
        return ExtEvtRegisteredOePb.class;
    }

    public ExtEvtRegisteredOePb.Builder toProto() {
        ExtEvtRegisteredOePb.Builder out = ExtEvtRegisteredOePb
            .newBuilder()
            .setExtEvtDefName(extEvtDefName)
            .setGuid(guid)
            .setContent(content.toProto());
        if (threadRunNumber != null) out.setThreadRunNumber(threadRunNumber);
        if (nodeRunPosition != null) out.setNodeRunPosition(nodeRunPosition);
        return out;
    }

    public void initFrom(Message proto) {
        ExtEvtRegisteredOePb p = (ExtEvtRegisteredOePb) proto;
        extEvtDefName = p.getExtEvtDefName();
        guid = p.getGuid();
        content = VariableValue.fromProto(p.getContent());
        if (p.hasThreadRunNumber()) threadRunNumber = p.getThreadRunNumber();
        if (p.hasNodeRunPosition()) nodeRunPosition = p.getNodeRunPosition();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        // Nothing to do yet
    }
}
