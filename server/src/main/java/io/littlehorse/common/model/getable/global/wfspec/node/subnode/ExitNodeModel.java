package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExitRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.ExitNode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ExitNodeModel extends SubNode<ExitNode> {

    public FailureDefModel failureDef;

    public Class<ExitNode> getProtoBaseClass() {
        return ExitNode.class;
    }

    public void initFrom(Message proto) {
        ExitNode p = (ExitNode) proto;
        if (p.hasFailureDef())
            failureDef = FailureDefModel.fromProto(p.getFailureDef());
    }

    public ExitNode.Builder toProto() {
        ExitNode.Builder out = ExitNode.newBuilder();
        if (failureDef != null) {
            out.setFailureDef(failureDef.toProto());
        }
        return out;
    }

    public void validate(ReadOnlyMetadataStore stores, LHConfig config) throws LHValidationError {
        if (failureDef != null)
            failureDef.validate();
    }

    public ExitRunModel createSubNodeRun(Date time) {
        return new ExitRunModel();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        HashSet<String> out = new HashSet<>();
        if (failureDef != null) {
            out.addAll(failureDef.getNeededVariableNames());
        }
        return out;
    }
}
