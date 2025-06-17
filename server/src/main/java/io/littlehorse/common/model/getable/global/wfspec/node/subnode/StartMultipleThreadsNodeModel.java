package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartMultipleThreadsRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class StartMultipleThreadsNodeModel extends SubNode<StartMultipleThreadsNode> {

    private String threadSpecName;
    private Map<String, VariableAssignmentModel> variables;
    private VariableAssignmentModel iterable;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StartMultipleThreadsNode node = (StartMultipleThreadsNode) proto;
        threadSpecName = node.getThreadSpecName();
        variables = new HashMap<>();
        node.getVariablesMap().forEach((variableName, variableAssignment) -> {
            variables.put(variableName, VariableAssignmentModel.fromProto(variableAssignment, context));
        });
        iterable = LHSerializable.fromProto(node.getIterable(), VariableAssignmentModel.class, context);
    }

    @Override
    public StartMultipleThreadsNode.Builder toProto() {
        StartMultipleThreadsNode.Builder out = StartMultipleThreadsNode.newBuilder();
        out.setIterable(iterable.toProto());
        out.setThreadSpecName(threadSpecName);
        variables.forEach((variableName, variableAssignment) -> {
            out.putVariables(variableName, variableAssignment.toProto().build());
        });
        return out;
    }

    @Override
    public Class<StartMultipleThreadsNode> getProtoBaseClass() {
        return StartMultipleThreadsNode.class;
    }

    @Override
    public SubNodeRun<?> createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        StartMultipleThreadsRunModel out = new StartMultipleThreadsRunModel();
        out.setThreadSpecName(threadSpecName);
        return out;
    }

    @Override
    public void validate(MetadataProcessorContext ctx) {}
}
