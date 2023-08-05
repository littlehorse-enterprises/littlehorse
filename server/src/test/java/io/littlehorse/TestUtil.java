package io.littlehorse;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.Group;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.subnoderun.TaskNodeRun;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskNodeRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskNodeReference;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodePb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
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

    public static Variable variable(String wfRunId) {
        Variable variable = new Variable();
        variable.setWfRunId(wfRunId);
        variable.setThreadRunNumber(0);
        variable.setName("test");
        variable.setValue(variableValue());
        variable.setWfSpec(wfSpec("testWfSpecName"));
        return variable;
    }

    public static NodeRun nodeRun() {
        NodeRun nodeRun = new NodeRun();
        nodeRun.setWfRunId("0000000");
        nodeRun.setPosition(0);
        nodeRun.setThreadRunNumber(1);
        nodeRun.setStatus(LHStatusPb.RUNNING);
        nodeRun.setType(NodeRunPb.NodeTypeCase.TASK);
        nodeRun.setArrivalTime(new Date());
        nodeRun.setWfSpecId(wfSpecId());
        nodeRun.setThreadSpecName("test-thread");
        nodeRun.setNodeName("test-node-name");
        nodeRun.setTaskRun(taskNodeRun());
        return nodeRun;
    }

    public static UserTaskNodeRun userTaskNodeRun(String wfRunId) {
        UserTaskRun utr = userTaskRun(wfRunId);
        UserTaskNodeRun out = new UserTaskNodeRun();
        out.setUserTaskRunId(utr.getObjectId());
        return out;
    }

    public static UserTaskRun userTaskRun(String wfRunId) {
        UserTaskRun userTaskRun = new UserTaskRun();
        userTaskRun.setId(new UserTaskRunId(wfRunId, "fdsa"));
        userTaskRun.setUserTaskDefId(new UserTaskDefId("ut-name", 0));
        userTaskRun.setStatus(UserTaskRunStatusPb.ASSIGNED);
        userTaskRun.setOwnerCase(UserTaskRunPb.OwnerCase.USER);
        userTaskRun.setUser(new User("33333"));
        userTaskRun.setGroup(new Group("1234567"));
        userTaskRun.setScheduledTime(new Date());
        userTaskRun.setNodeRunId(nodeRun().getObjectId());
        return userTaskRun;
    }

    public static WfSpecId wfSpecId() {
        WfSpecId wfSpecId = new WfSpecId("testName", 0);
        return wfSpecId;
    }

    public static TaskNodeRun taskNodeRun() {
        TaskNodeRun taskNodeRun = new TaskNodeRun();
        taskNodeRun.setTaskRunId(taskRunId());
        return taskNodeRun;
    }

    public static TaskRun taskRun() {
        TaskRun taskRun = new TaskRun();
        taskRun.setId(taskRunId());
        taskRun.setTaskRunSource(
            new TaskRunSource(
                new TaskNodeReference(nodeRun().getObjectId(), wfSpecId())
            )
        );
        taskRun.setTaskDefName("test-name");
        taskRun.setMaxAttempts(10);
        taskRun.setScheduledAt(new Date());
        taskRun.setStatus(TaskStatusPb.TASK_SCHEDULED);
        return taskRun;
    }

    public static TaskRunId taskRunId() {
        TaskRunId taskRunId = new TaskRunId("1234", "01010");
        return taskRunId;
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
        spec.setThreadSpecs(Map.of("entrypoint", threadSpec()));
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

    public static ExternalEvent externalEvent() {
        ExternalEvent externalEvent = new ExternalEvent();
        externalEvent.setExternalEventDefName("test-name");
        externalEvent.setClaimed(true);
        externalEvent.setWfRunId("0000000");
        externalEvent.setGuid("0000001");
        externalEvent.setContent(variableValue());
        return externalEvent;
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
