package io.littlehorse.common.model.getable.global.wfspec.thread;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.NodeOutputReferenceModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.TaskNode.TaskToExecuteCase;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ThreadSpecModelTest {

    @Test
    void testGetUsedNodeNames() {
        ThreadSpecModel threadSpec = new ThreadSpecModel();
        threadSpec.setName("test-thread");

        NodeModel node1 = new NodeModel();
        TaskNodeModel taskNode1 = new TaskNodeModel();
        taskNode1.setTaskDefId(new TaskDefIdModel("task1"));
        taskNode1.setTaskToExecuteType(TaskToExecuteCase.TASK_DEF_ID);

        VariableAssignmentModel assignment = new VariableAssignmentModel();
        assignment.setRhsSourceType(SourceCase.NODE_OUTPUT);
        NodeOutputReferenceModel ref = LHSerializable.fromProto(
                NodeOutputReference.newBuilder().setNodeName("node-2").build(), NodeOutputReferenceModel.class, null);
        assignment.setNodeOutputReference(ref);
        taskNode1.setVariables(List.of(assignment));

        node1.setTaskNode(taskNode1);
        node1.setType(Node.NodeCase.TASK);

        NodeModel node2 = new NodeModel();
        StartThreadNodeModel startNode = new StartThreadNodeModel();

        VariableAssignmentModel assignment2 = new VariableAssignmentModel();
        assignment2.setRhsSourceType(SourceCase.NODE_OUTPUT);
        NodeOutputReferenceModel ref2 = LHSerializable.fromProto(
                NodeOutputReference.newBuilder().setNodeName("node-3").build(), NodeOutputReferenceModel.class, null);
        assignment2.setNodeOutputReference(ref2);
        startNode.getVariables().put("var", assignment2);

        node2.setStartThreadNode(startNode);
        node2.setType(Node.NodeCase.START_THREAD);

        NodeModel node3 = new NodeModel();
        TaskNodeModel taskNode3 = new TaskNodeModel();
        taskNode3.setTaskDefId(new TaskDefIdModel("task3"));
        taskNode3.setTaskToExecuteType(TaskToExecuteCase.TASK_DEF_ID);
        node3.setTaskNode(taskNode3);
        node3.setType(Node.NodeCase.TASK);

        threadSpec.setNodes(Map.of("node-1", node1, "node-2", node2, "node-3", node3));

        Set<String> usedNodeNames = threadSpec.getRequiredNodeNames();

        assertThat(usedNodeNames).containsExactlyInAnyOrder("node-2", "node-3");
    }
}
