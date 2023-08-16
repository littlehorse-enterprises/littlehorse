package io.littlehorse;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.subnode.TaskNodeModel;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.common.model.wfrun.UserGroup;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.subnoderun.TaskNodeRun;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskNodeRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskNodeReference;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TestUtil {

    public static WfRunModel wfRun(String id) {
        WfRunModel wfRunModel = new WfRunModel();
        wfRunModel.setId(id);
        wfRunModel.setWfSpecName("test-spec-name");
        wfRunModel.setWfSpecVersion(0);
        wfRunModel.status = LHStatus.RUNNING;
        wfRunModel.setStartTime(new Date());
        return wfRunModel;
    }

    public static Variable variable(String wfRunId) {
        return new Variable(
            "test",
            variableValue(),
            wfRunId,
            0,
            wfSpec("testWfSpecName")
        );
    }

    public static NodeRunModel nodeRun() {
        NodeRunModel nodeRunModel = new NodeRunModel();
        nodeRunModel.setWfRunId("0000000");
        nodeRunModel.setPosition(0);
        nodeRunModel.setThreadRunNumber(1);
        nodeRunModel.setStatus(LHStatus.RUNNING);
        nodeRunModel.setType(NodeRun.NodeTypeCase.TASK);
        nodeRunModel.setArrivalTime(new Date());
        nodeRunModel.setWfSpecId(wfSpecId());
        nodeRunModel.setThreadSpecName("test-thread");
        nodeRunModel.setNodeName("test-node-name");
        nodeRunModel.setTaskRun(taskNodeRun());
        return nodeRunModel;
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
        userTaskRun.setUserTaskDefId(new UserTaskDefIdModel("ut-name", 0));
        userTaskRun.setStatus(UserTaskRunStatusPb.ASSIGNED);
        userTaskRun.setOwnerCase(UserTaskRunPb.OwnerCase.USER);
        userTaskRun.setUser(new User("33333"));
        userTaskRun.setUserGroup(new UserGroup("1234567"));
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

    public static VariableValueModel variableValue() {
        VariableValueModel variableValue = new VariableValueModel();
        variableValue.setStrVal("testVarValue");
        variableValue.setType(VariableType.STR);
        return variableValue;
    }

    public static WfSpecModel wfSpec(String name) {
        WfSpecModel spec = new WfSpecModel();
        spec.setName(name);
        spec.setCreatedAt(new Date());
        spec.setEntrypointThreadName("testEntrypointThreadName");
        spec.setStatus(LHStatus.RUNNING);
        spec.setThreadSpecs(Map.of("entrypoint", threadSpec()));
        return spec;
    }

    public static ThreadSpecModel threadSpec() {
        ThreadSpecModel threadSpecModel = new ThreadSpecModel();
        threadSpecModel.setName("test-name");
        threadSpecModel.setNodes(Map.of("node-1", node()));
        return threadSpecModel;
    }

    public static NodeModel node() {
        NodeModel node = new NodeModel();
        node.setTaskNode(taskNode());
        node.setType(Node.NodeCase.TASK);
        return node;
    }

    public static TaskNodeModel taskNode() {
        TaskNodeModel taskNode = new TaskNodeModel();
        taskNode.setTaskDefName("test-task-def-name");
        return taskNode;
    }

    public static Getable<?> getable(Class<?> getableClass, String id) {
        if (getableClass.equals(WfRunModel.class)) {
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

    public static VariableDefModel variableDef(
        String name,
        VariableType variableTypePb
    ) {
        VariableDefModel variableDef = new VariableDefModel();
        variableDef.setName(name);
        variableDef.setType(variableTypePb);
        return variableDef;
    }
}
