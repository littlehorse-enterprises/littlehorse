package io.littlehorse.common.model.getable.global.wfspec.node.subnode;


import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.StartChildWfNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StartChildWfNodeModelTest {

    @Test
    public void shouldInitializeStartChildWfNodeModelFromProto() {
        VariableAssignment inputVar = VariableAssignment.newBuilder().setLiteralValue(LHLibUtil.objToVarVal("hi")).build();
        StartChildWfNode startChildWfNode = StartChildWfNode.newBuilder()
                .setWfSpecName("wf-spec-name")
                .setMajorVersion(2)
                .putVariables("input", inputVar)
                .build();
        StartChildWfNodeModel model = StartChildWfNodeModel.fromProto(startChildWfNode, StartChildWfNodeModel.class, mock(ExecutionContext.class));
        assertThat(model.getWfSpecName()).isEqualTo("wf-spec-name");
        assertThat(model.getMajorVersion()).isEqualTo(2);
        assertThat(model.getVariables()).hasSize(1).containsOnlyKeys("input");
    }

    @Test
    public void shouldGenerateProtoFromModel() {
        VariableAssignment inputVar = VariableAssignment.newBuilder().setLiteralValue(LHLibUtil.objToVarVal("hi")).build();
        StartChildWfNodeModel model = new StartChildWfNodeModel("wf-spec-name", 2, Map.of("input", VariableAssignmentModel.fromProto(inputVar, mock(ExecutionContext.class))));
        StartChildWfNode proto = model.toProto().build();
        assertThat(proto.getWfSpecName()).isEqualTo("wf-spec-name");
        assertThat(proto.getMajorVersion()).isEqualTo(2);
        assertThat(proto.getVariablesMap()).hasSize(1).containsOnlyKeys("input");
    }
}