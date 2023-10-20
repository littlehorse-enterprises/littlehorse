package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartMultipleThreadsRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsNode;
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
    public void initFrom(Message proto) throws LHSerdeError {
        StartMultipleThreadsNode node = (StartMultipleThreadsNode) proto;
        threadSpecName = node.getThreadSpecName();
        variables = new HashMap<>();
        node.getVariablesMap().forEach((variableName, variableAssignment) -> {
            variables.put(variableName, VariableAssignmentModel.fromProto(variableAssignment));
        });
        iterable = LHSerializable.fromProto(node.getIterable(), VariableAssignmentModel.class);
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
    public SubNodeRun<?> createSubNodeRun(Date time) {
        StartMultipleThreadsRunModel out = new StartMultipleThreadsRunModel();
        out.setThreadSpecName(threadSpecName);
        return out;
    }

    @Override
    public void validate(ReadOnlyMetadataProcessorDAO readOnlyDao, LHServerConfig config) throws LHApiException {}
}
