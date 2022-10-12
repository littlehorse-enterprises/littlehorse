package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.subnoderun.StartThreadRun;
import io.littlehorse.common.proto.StartThreadNodePb;
import io.littlehorse.common.proto.StartThreadNodePbOrBuilder;
import io.littlehorse.common.proto.VariableAssignmentPb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StartThreadNode extends SubNode<StartThreadNodePb> {

    public String threadSpecName;
    public Map<String, VariableAssignment> variables;

    public StartThreadNode() {
        variables = new HashMap<>();
    }

    public Class<StartThreadNodePb> getProtoBaseClass() {
        return StartThreadNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        StartThreadNodePbOrBuilder p = (StartThreadNodePbOrBuilder) proto;
        threadSpecName = p.getThreadSpecName();
        for (Map.Entry<String, VariableAssignmentPb> e : p
            .getVariablesMap()
            .entrySet()) {
            variables.put(e.getKey(), VariableAssignment.fromProto(e.getValue()));
        }
    }

    public StartThreadNodePb.Builder toProto() {
        StartThreadNodePb.Builder out = StartThreadNodePb
            .newBuilder()
            .setThreadSpecName(threadSpecName);

        for (Map.Entry<String, VariableAssignment> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        WfSpec wfSpec = node.threadSpec.wfSpec;

        if (threadSpecName.equals(node.threadSpec.name)) {
            throw new LHValidationError(null, "Tried to start same thread");
        }

        ThreadSpec childThreadSpec = wfSpec.threadSpecs.get(threadSpecName);
        if (childThreadSpec == null) {
            throw new LHValidationError(
                null,
                "Tried to start nonexistent thread " + threadSpecName
            );
        }

        childThreadSpec.validateStartVariablesByType(variables);
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignment assn : variables.values()) {
            out.addAll(assn.getRequiredVariableNames());
        }
        return out;
    }

    public StartThreadRun createRun(Date time) {
        StartThreadRun out = new StartThreadRun();
        out.threadSpecName = threadSpecName;
        return out;
    }
}
