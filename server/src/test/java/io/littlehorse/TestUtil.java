package io.littlehorse;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TestUtil {

    public static WfRun wfRun(String id) {
        WfRun wfRun = new WfRun();
        wfRun.setId(id);
        wfRun.setWfSpecName("test-spec-name");
        wfRun.setWfSpecVersion(0);
        wfRun.status = LHStatusPb.RUNNING;
        wfRun.setStartTime(new Date());
        return wfRun;
    }

    public static Variable variable(String id) {
        Variable variable = new Variable();
        variable.setWfRunId(id);
        variable.setThreadRunNumber(0);
        variable.setName("test");
        variable.setValue(variableValue());
        variable.setWfSpec(wfSpec("testWfSpecName"));
        return variable;
    }

    public static VariableValue variableValue() {
        VariableValue variableValue = new VariableValue();
        variableValue.setStrVal("testVarValue");
        variableValue.setType(VariableTypePb.STR);
        return variableValue;
    }

    public static WfSpec wfSpec(String name) {
        WfSpec spec = new WfSpec();
        spec.setName(name);
        spec.setCreatedAt(new Date());
        spec.setEntrypointThreadName("testEntrypointThreadName");
        spec.setStatus(LHStatusPb.RUNNING);
        return spec;
    }

    public static ThreadSpec threadSpec() {
        ThreadSpec threadSpec = new ThreadSpec();
        threadSpec.setName("test-name");
        threadSpec.setNodes(Map.of("node-1", node()));
        return threadSpec;
    }

    public static Node node() {
        Node node = new Node();
        node.setTaskNode(taskNode());
        node.setType(NodePb.NodeCase.TASK);
        return node;
    }

    public static TaskNode taskNode() {
        TaskNode taskNode = new TaskNode();
        taskNode.setTaskDefName("test-task-def-name");
        return taskNode;
    }

    public static Getable<?> getable(Class<?> getableClass, String id) {
        if (getableClass.equals(WfRun.class)) {
            return wfRun(id);
        } else {
            throw new IllegalArgumentException(
                "There is no test data for " + getableClass.getName()
            );
        }
    }

    public static Tag tag() {
        Tag tag = new Tag();
        tag.setTagType(TagStorageTypePb.LOCAL);
        tag.setObjectType(GetableClassEnumPb.WF_RUN);
        tag.setCreatedAt(new Date());
        tag.setDescribedObjectId(UUID.randomUUID().toString());
        return tag;
    }

    public static VariableDef variableDef(
        String name,
        VariableTypePb variableTypePb
    ) {
        VariableDef variableDef = new VariableDef();
        variableDef.setName(name);
        variableDef.setType(variableTypePb);
        return variableDef;
    }
}
