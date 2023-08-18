package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import java.util.Date;

public class ExternalEventNodeModel extends SubNode<ExternalEventNode> {

    public String externalEventDefName;
    public VariableAssignmentModel timeoutSeconds;

    public ExternalEventDefModel externalEventDef;

    public ExternalEventNodeModel() {}

    public Class<ExternalEventNode> getProtoBaseClass() {
        return ExternalEventNode.class;
    }

    public void initFrom(Message proto) {
        ExternalEventNode p = (ExternalEventNode) proto;
        externalEventDefName = p.getExternalEventDefName();
        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignmentModel.fromProto(p.getTimeoutSeconds());
        }
    }

    public ExternalEventNode.Builder toProto() {
        ExternalEventNode.Builder out =
                ExternalEventNode.newBuilder().setExternalEventDefName(externalEventDefName);

        if (timeoutSeconds != null) out.setTimeoutSeconds(timeoutSeconds.toProto());
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config) throws LHValidationError {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDefModel eed = stores.getExternalEventDef(externalEventDefName);

        // TODO: validate the timeout

        if (eed == null) {
            throw new LHValidationError(
                    null, "Refers to nonexistent ExternalEventDef " + externalEventDefName);
        }
    }

    public ExternalEventRunModel createSubNodeRun(Date time) {
        ExternalEventRunModel out = new ExternalEventRunModel();
        out.externalEventDefName = externalEventDefName;

        return out;
    }
}
