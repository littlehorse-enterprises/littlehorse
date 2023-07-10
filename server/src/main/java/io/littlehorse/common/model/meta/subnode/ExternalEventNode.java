package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExternalEventNodePb;
import java.util.Date;

public class ExternalEventNode extends SubNode<ExternalEventNodePb> {

    public String externalEventDefName;
    public VariableAssignment timeoutSeconds;

    public ExternalEventDef externalEventDef;

    public ExternalEventNode() {}

    public Class<ExternalEventNodePb> getProtoBaseClass() {
        return ExternalEventNodePb.class;
    }

    public void initFrom(Message proto) {
        ExternalEventNodePb p = (ExternalEventNodePb) proto;
        externalEventDefName = p.getExternalEventDefName();
        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignment.fromProto(p.getTimeoutSeconds());
        }
    }

    public ExternalEventNodePb.Builder toProto() {
        ExternalEventNodePb.Builder out = ExternalEventNodePb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName);

        if (timeoutSeconds != null) out.setTimeoutSeconds(timeoutSeconds.toProto());
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDef eed = stores.getExternalEventDef(externalEventDefName);

        // TODO: validate the timeout

        if (eed == null) {
            throw new LHValidationError(
                null,
                "Refers to nonexistent ExternalEventDef " + externalEventDefName
            );
        }
    }

    public ExternalEventRun createSubNodeRun(Date time) {
        ExternalEventRun out = new ExternalEventRun();
        out.externalEventDefName = externalEventDefName;

        return out;
    }
}
