package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.wfrun.subnoderun.StartThreadRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.StartThreadNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StartThreadNodeModel extends SubNode<StartThreadNode> {

    public String threadSpecName;
    public Map<String, VariableAssignmentModel> variables;

    public StartThreadNodeModel() {
        variables = new HashMap<>();
    }

    public Class<StartThreadNode> getProtoBaseClass() {
        return StartThreadNode.class;
    }

    public void initFrom(Message proto) {
        StartThreadNode p = (StartThreadNode) proto;
        threadSpecName = p.getThreadSpecName();
        for (Map.Entry<String, VariableAssignment> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableAssignmentModel.fromProto(e.getValue()));
        }
    }

    public StartThreadNode.Builder toProto() {
        StartThreadNode.Builder out =
                StartThreadNode.newBuilder().setThreadSpecName(threadSpecName);

        for (Map.Entry<String, VariableAssignmentModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config) throws LHValidationError {
        WfSpecModel wfSpecModel = node.threadSpecModel.wfSpecModel;

        if (threadSpecName.equals(node.threadSpecModel.name)) {
            throw new LHValidationError(null, "Tried to start same thread");
        }

        ThreadSpecModel childThreadSpecModel = wfSpecModel.threadSpecs.get(threadSpecName);
        if (childThreadSpecModel == null) {
            throw new LHValidationError(
                    null, "Tried to start nonexistent thread " + threadSpecName);
        }

        childThreadSpecModel.validateStartVariablesByType(variables);
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assn : variables.values()) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        return out;
    }

    public StartThreadRunModel createSubNodeRun(Date time) {
        StartThreadRunModel out = new StartThreadRunModel();
        out.threadSpecName = threadSpecName;
        return out;
    }
}
