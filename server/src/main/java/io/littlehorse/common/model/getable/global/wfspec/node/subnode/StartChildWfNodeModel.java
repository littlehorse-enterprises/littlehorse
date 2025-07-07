package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartChildWfNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StartChildWfNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import lombok.Getter;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
public class StartChildWfNodeModel extends SubNode<StartChildWfNode> {

    private String wfSpecName;
    private Integer majorVersion;
    private Map<String, VariableAssignmentModel> variables;

    public StartChildWfNodeModel() {
    }

    public StartChildWfNodeModel(String wfSpecName, Integer majorVersion, Map<String, VariableAssignmentModel> variables) {
        this.wfSpecName = wfSpecName;
        this.majorVersion = majorVersion;
        this.variables = variables;
    }

    @Override
    public StartChildWfNodeRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new StartChildWfNodeRunModel();
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws LHApiException {

    }

    @Override
    public StartChildWfNode.Builder toProto() {
        StartChildWfNode.Builder out = StartChildWfNode.newBuilder().setWfSpecName(wfSpecName);
        if (majorVersion != null) {
            out.setMajorVersion(majorVersion);
        }
        for (Map.Entry<String, VariableAssignmentModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StartChildWfNode p = (StartChildWfNode) proto;
        variables = new HashMap<>();
        this.wfSpecName = p.getWfSpecName();
        if (p.hasMajorVersion()) {
            this.majorVersion = p.getMajorVersion();
        }
        for (Map.Entry<String, VariableAssignment> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableAssignmentModel.fromProto(e.getValue(), context));
        }
        variables = Collections.unmodifiableMap(variables);
    }

    @Override
    public Class<StartChildWfNode> getProtoBaseClass() {
        return StartChildWfNode.class;
    }
}
