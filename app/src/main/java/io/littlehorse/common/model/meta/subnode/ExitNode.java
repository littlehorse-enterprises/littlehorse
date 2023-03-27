package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.FailureDef;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.ExitRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExitNodePb;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ExitNode extends SubNode<ExitNodePb> {

    public FailureDef failureDef;

    public Class<ExitNodePb> getProtoBaseClass() {
        return ExitNodePb.class;
    }

    public void initFrom(Message proto) {
        ExitNodePb p = (ExitNodePb) proto;
        if (p.hasFailureDef()) failureDef = FailureDef.fromProto(p.getFailureDef());
    }

    public ExitNodePb.Builder toProto() {
        ExitNodePb.Builder out = ExitNodePb.newBuilder();
        if (failureDef != null) {
            out.setFailureDef(failureDef.toProto());
        }
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        if (failureDef != null) failureDef.validate();
    }

    public ExitRun createRun(Date time) {
        return new ExitRun();
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
