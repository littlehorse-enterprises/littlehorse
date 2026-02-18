package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentTriggerReferenceModel extends LHSerializable<WfRun.ParentTriggerReference> {

    private NodeRunIdModel triggeringNodeRun;
    private NodeRunIdModel waitingNodeRun;

    @Override
    public Class<WfRun.ParentTriggerReference> getProtoBaseClass() {
        return WfRun.ParentTriggerReference.class;
    }

    @Override
    public WfRun.ParentTriggerReference.Builder toProto() {
        WfRun.ParentTriggerReference.Builder out =
                WfRun.ParentTriggerReference.newBuilder().setTriggeringNodeRun(triggeringNodeRun.toProto());

        if (waitingNodeRun != null) {
            out.setWaitingNodeRun(waitingNodeRun.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WfRun.ParentTriggerReference p = (WfRun.ParentTriggerReference) proto;
        triggeringNodeRun = LHSerializable.fromProto(p.getTriggeringNodeRun(), NodeRunIdModel.class, context);

        if (p.hasWaitingNodeRun()) {
            waitingNodeRun = LHSerializable.fromProto(p.getWaitingNodeRun(), NodeRunIdModel.class, context);
        }
    }

    public static ParentTriggerReferenceModel fromProto(WfRun.ParentTriggerReference p, ExecutionContext context) {
        ParentTriggerReferenceModel out = new ParentTriggerReferenceModel();
        out.initFrom(p, context);
        return out;
    }
}
