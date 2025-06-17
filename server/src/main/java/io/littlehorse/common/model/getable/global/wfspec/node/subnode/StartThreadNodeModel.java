package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.StartThreadNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class StartThreadNodeModel extends SubNode<StartThreadNode> {

    public String threadSpecName;
    public Map<String, VariableAssignmentModel> variables;

    public StartThreadNodeModel() {
        variables = new HashMap<>();
    }

    public Class<StartThreadNode> getProtoBaseClass() {
        return StartThreadNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        StartThreadNode p = (StartThreadNode) proto;
        threadSpecName = p.getThreadSpecName();
        for (Map.Entry<String, VariableAssignment> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableAssignmentModel.fromProto(e.getValue(), context));
        }
    }

    public StartThreadNode.Builder toProto() {
        StartThreadNode.Builder out = StartThreadNode.newBuilder().setThreadSpecName(threadSpecName);

        for (Map.Entry<String, VariableAssignmentModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws LHApiException {
        WfSpecModel wfSpecModel = node.threadSpec.wfSpec;

        if (threadSpecName.equals(node.threadSpec.name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to start same thread");
        }

        ThreadSpecModel childThreadSpecModel = wfSpecModel.threadSpecs.get(threadSpecName);
        if (childThreadSpecModel == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to start nonexistent thread " + threadSpecName);
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

    @Override
    public StartThreadRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        StartThreadRunModel out = new StartThreadRunModel();
        out.threadSpecName = threadSpecName;
        return out;
    }
}
