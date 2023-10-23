package io.littlehorse;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskNodeReferenceModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static StoredGetable<WfRun, WfRunModel> storedWfRun(String id) {
        return new StoredGetable<>(wfRun(id));
    }

    public static TaskDefModel taskDef(String name) {
        TaskDefModel taskDef = new TaskDefModel();
        taskDef.setName(name);
        taskDef.setCreatedAt(new Date());
        return taskDef;
    }

    public static VariableModel variable(String wfRunId) {
        return new VariableModel("test", variableValue(), wfRunId, 0, wfSpec("testWfSpecName"));
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

    public static UserTaskNodeRunModel userTaskNodeRun(String wfRunId) {
        UserTaskRunModel utr = userTaskRun(wfRunId);
        UserTaskNodeRunModel out = new UserTaskNodeRunModel();
        out.setUserTaskRunId(utr.getObjectId());
        return out;
    }

    public static UserTaskRunModel userTaskRun(String wfRunId) {
        UserTaskRunModel userTaskRun = new UserTaskRunModel();
        userTaskRun.setId(new UserTaskRunIdModel(wfRunId, "fdsa"));
        userTaskRun.setUserTaskDefId(new UserTaskDefIdModel("ut-name", 0));
        userTaskRun.setStatus(UserTaskRunStatus.ASSIGNED);
        userTaskRun.setUserId("33333");
        userTaskRun.setUserGroup("1234567");
        userTaskRun.setScheduledTime(new Date());
        userTaskRun.setNodeRunId(nodeRun().getObjectId());
        return userTaskRun;
    }

    public static WfSpecIdModel wfSpecId() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("testName", 0);
        return wfSpecId;
    }

    public static TaskNodeRunModel taskNodeRun() {
        TaskNodeRunModel taskNodeRun = new TaskNodeRunModel();
        taskNodeRun.setTaskRunId(taskRunId());
        return taskNodeRun;
    }

    public static TaskRunModel taskRun() {
        TaskRunModel taskRun = new TaskRunModel();
        taskRun.setId(taskRunId());
        taskRun.setTaskRunSource(
                new TaskRunSourceModel(new TaskNodeReferenceModel(nodeRun().getObjectId(), wfSpecId())));
        taskRun.setTaskDefName("test-name");
        taskRun.setMaxAttempts(10);
        taskRun.setScheduledAt(new Date());
        taskRun.setStatus(TaskStatus.TASK_SCHEDULED);
        return taskRun;
    }

    public static TaskRunIdModel taskRunId() {
        TaskRunIdModel taskRunId = new TaskRunIdModel("1234", "01010");
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

    public static AbstractGetable<?> getable(Class<?> getableClass, String id) {
        if (getableClass.equals(WfRunModel.class)) {
            return wfRun(id);
        } else {
            throw new IllegalArgumentException("There is no test data for " + getableClass.getName());
        }
    }

    public static Tag tag() {
        Tag tag = new Tag();
        tag.setTagType(TagStorageType.LOCAL);
        tag.setObjectType(GetableClassEnum.WF_RUN);
        tag.setCreatedAt(new Date());
        tag.setDescribedObjectId(UUID.randomUUID().toString());
        return tag;
    }

    public static ExternalEventModel externalEvent() {
        ExternalEventModel externalEvent = new ExternalEventModel();
        externalEvent.setExternalEventDefName("test-name");
        externalEvent.setClaimed(true);
        externalEvent.setWfRunId("0000000");
        externalEvent.setGuid("0000001");
        externalEvent.setContent(variableValue());
        return externalEvent;
    }

    public static VariableDefModel variableDef(String name, VariableType variableTypePb) {
        VariableDefModel variableDef = new VariableDefModel();
        variableDef.setName(name);
        variableDef.setType(variableTypePb);
        return variableDef;
    }

    public static ScheduledTaskModel scheduledTaskModel() {
        return new ScheduledTaskModel(
                taskDef("my-task").getObjectId(),
                List.of(),
                userTaskRun(UUID.randomUUID().toString()));
    }

    public static ServerACLModel acl() {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of("name"));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_PRINCIPAL));
        acl.setAllowedActions(List.of(ACLAction.WRITE_METADATA));
        return acl;
    }

    public static ServerACLModel adminAcl() {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of("name"));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_ALL_RESOURCE_TYPES));
        acl.setAllowedActions(List.of(ACLAction.ALL_ACTIONS));
        return acl;
    }
}
