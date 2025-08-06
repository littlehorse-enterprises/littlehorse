package io.littlehorse;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
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
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.sdk.common.proto.TaskNode.TaskToExecuteCase;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.mockito.Answers;
import org.mockito.Mockito;

public class TestUtil {

    public static WfRunModel wfRun(String id) {
        WfRunModel wfRunModel = new WfRunModel();
        wfRunModel.setId(new WfRunIdModel(id));
        wfRunModel.setWfSpecId(new WfSpecIdModel("test-spec-name", 0, 0));
        wfRunModel.status = LHStatus.RUNNING;
        wfRunModel.setStartTime(new Date());
        return wfRunModel;
    }

    public static StoredGetable<WfRun, WfRunModel> storedWfRun(String id) {
        return new StoredGetable<>(wfRun(id));
    }

    public static TaskDefModel taskDef(String name) {
        TaskDefModel taskDef = new TaskDefModel();
        taskDef.setId(new TaskDefIdModel(name));
        taskDef.setReturnType(new ReturnTypeModel(VariableType.STR));
        return taskDef;
    }

    public static VariableModel variable(String wfRunId) {
        return new VariableModel(
                "test", variableValue(), new WfRunIdModel(wfRunId), 0, wfSpec("testWfSpecName"), false);
    }

    public static NodeRunModel nodeRun() {
        NodeRunModel nodeRunModel = new NodeRunModel();
        nodeRunModel.setId(new NodeRunIdModel("0000000", 1, 0));
        nodeRunModel.setStatus(LHStatus.RUNNING);
        nodeRunModel.setType(NodeRun.NodeTypeCase.TASK);
        nodeRunModel.setArrivalTime(new Date());
        nodeRunModel.setWfSpecId(wfSpecId());
        nodeRunModel.setThreadSpecName("test-thread");
        nodeRunModel.setNodeName("test-node-name");
        nodeRunModel.setTaskRun(taskNodeRun());
        return nodeRunModel;
    }

    public static NodeRunModel nodeRun(String wfRunId) {
        NodeRunModel nodeRunModel = new NodeRunModel();
        nodeRunModel.setId(new NodeRunIdModel(wfRunId, 1, 0));
        nodeRunModel.setStatus(LHStatus.RUNNING);
        nodeRunModel.setType(NodeRun.NodeTypeCase.TASK);
        nodeRunModel.setArrivalTime(new Date());
        nodeRunModel.setWfSpecId(wfSpecId());
        nodeRunModel.setThreadSpecName("test-thread");
        nodeRunModel.setNodeName("test-node-name");
        nodeRunModel.setTaskRun(taskNodeRun());
        return nodeRunModel;
    }

    public static UserTaskNodeRunModel userTaskNodeRun(String wfRunId, CoreProcessorContext processorContext) {
        UserTaskRunModel utr = userTaskRun(wfRunId, processorContext);
        UserTaskNodeRunModel out = new UserTaskNodeRunModel();
        out.setUserTaskRunId(utr.getObjectId());
        return out;
    }

    public static UserTaskRunModel userTaskRun(String wfRunId, CoreProcessorContext processorContext) {
        UserTaskRunModel userTaskRun = new UserTaskRunModel(processorContext);
        userTaskRun.setId(new UserTaskRunIdModel(new WfRunIdModel(wfRunId), "fdsa"));
        userTaskRun.setUserTaskDefId(new UserTaskDefIdModel("ut-name", 0));
        userTaskRun.setStatus(UserTaskRunStatus.ASSIGNED);
        userTaskRun.setUserId("33333");
        userTaskRun.setUserGroup("1234567");
        userTaskRun.setScheduledTime(new Date());
        userTaskRun.setNodeRunId(nodeRun(wfRunId).getObjectId());
        return userTaskRun;
    }

    public static UserTaskRunModel userTaskRun(
            String wfRunId, NodeRunModel nodeRun, CoreProcessorContext processorContext) {
        UserTaskRunModel userTaskRun = new UserTaskRunModel(processorContext);
        userTaskRun.setId(new UserTaskRunIdModel(new WfRunIdModel(wfRunId), "fdsa"));
        userTaskRun.setUserTaskDefId(new UserTaskDefIdModel("ut-name", 0));
        userTaskRun.setStatus(UserTaskRunStatus.ASSIGNED);
        userTaskRun.setUserId("33333");
        userTaskRun.setUserGroup("1234567");
        userTaskRun.setScheduledTime(new Date());
        userTaskRun.setNodeRunId(nodeRun.getObjectId());
        return userTaskRun;
    }

    public static WfSpecIdModel wfSpecId() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("testName", 0, 0);
        return wfSpecId;
    }

    public static TaskNodeRunModel taskNodeRun() {
        TaskNodeRunModel taskNodeRun = new TaskNodeRunModel(Mockito.mock());
        taskNodeRun.setTaskRunId(taskRunId());
        return taskNodeRun;
    }

    public static TaskRunModel taskRun() {
        return taskRun(taskRunId(), new TaskDefIdModel("test-name"));
    }

    public static TaskRunModel taskRun(TaskRunIdModel taskRunId, TaskDefIdModel taskDefId) {
        TaskRunModel taskRun = new TaskRunModel();
        taskRun.setId(taskRunId);
        taskRun.setTaskRunSource(new TaskRunSourceModel(
                new TaskNodeReferenceModel(nodeRun().getObjectId(), wfSpecId()), Mockito.mock()));
        taskRun.setTaskDefId(taskDefId);
        taskRun.setScheduledAt(new Date());
        taskRun.setStatus(TaskStatus.TASK_SCHEDULED);

        TaskAttemptModel attempt = new TaskAttemptModel();
        attempt.setStatus(TaskStatus.TASK_SCHEDULED);
        taskRun.getAttempts().add(attempt);
        return taskRun;
    }

    public static TaskRunIdModel taskRunId() {
        TaskRunIdModel taskRunId = new TaskRunIdModel(new WfRunIdModel("1234"), "01010");
        return taskRunId;
    }

    public static VariableValueModel variableValue() {
        VariableValueModel variableValue = new VariableValueModel("testVarValue");
        return variableValue;
    }

    public static WfSpecModel wfSpec(String name) {
        WfSpecModel spec = new WfSpecModel(Mockito.mock());
        spec.setId(new WfSpecIdModel(name, 0, 0));
        spec.setCreatedAt(new Date());
        spec.setEntrypointThreadName("entrypoint");
        spec.setThreadSpecs(Map.of("entrypoint", threadSpec()));
        return spec;
    }

    public static ThreadSpecModel threadSpec() {
        ThreadSpecModel threadSpecModel = new ThreadSpecModel();
        threadSpecModel.setName("test-name");
        threadSpecModel.setNodes(Map.of("node-1", node()));
        return threadSpecModel;
    }

    public static ThreadVarDefModel threadVarDef(
            String variableName, VariableType type, WfRunVariableAccessLevel accessLevel) {
        return new ThreadVarDefModel(variableDef(variableName, type), false, false, accessLevel);
    }

    public static NodeModel node() {
        NodeModel node = new NodeModel();
        node.setTaskNode(taskNode());
        node.setType(Node.NodeCase.TASK);
        return node;
    }

    public static TaskNodeModel taskNode() {
        TaskNodeModel taskNode = new TaskNodeModel();
        taskNode.setTaskDefId(new TaskDefIdModel("test-task-def-name"));
        taskNode.setTaskToExecuteType(TaskToExecuteCase.TASK_DEF_ID);
        return taskNode;
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
        ExternalEventModel externalEvent = new ExternalEventModel(
                variableValue(),
                new ExternalEventIdModel(
                        new WfRunIdModel("0000000"), new ExternalEventDefIdModel("test-name"), "0000001"),
                null,
                null,
                null);
        return externalEvent;
    }

    public static ExternalEventModel externalEvent(String wfRunId) {
        ExternalEventModel externalEvent = new ExternalEventModel(
                variableValue(),
                new ExternalEventIdModel(
                        new WfRunIdModel(wfRunId), new ExternalEventDefIdModel("test-name"), "0000001"),
                null,
                null,
                null);
        return externalEvent;
    }

    public static WorkflowEventModel workflowEvent(String wfRunId) {
        WorkflowEventModel workflowEvent = new WorkflowEventModel(
                new WorkflowEventIdModel(new WfRunIdModel(wfRunId), new WorkflowEventDefIdModel("test-name"), 0),
                variableValue(),
                nodeRun(wfRunId));
        return workflowEvent;
    }

    public static VariableDefModel variableDef(String name, VariableType variableTypePb) {
        VariableDefModel variableDef = new VariableDefModel();
        variableDef.setName(name);
        variableDef.setTypeDef(new TypeDefinitionModel(variableTypePb));
        return variableDef;
    }

    public static ScheduledTaskModel scheduledTaskModel() {
        return scheduledTaskModel(UUID.randomUUID().toString());
    }

    public static ScheduledTaskModel scheduledTaskModel(String wfRunId) {
        return new ScheduledTaskModel(
                taskDef("my-task").getObjectId(),
                List.of(),
                userTaskRun(wfRunId, Mockito.mock()),
                Mockito.mock(Answers.RETURNS_DEEP_STUBS));
    }

    public static ServerACLModel acl() {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of("name"));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_ALL_RESOURCES));
        acl.setAllowedActions(List.of(ACLAction.ALL_ACTIONS));
        return acl;
    }

    public static ServerACLModel aclWithTenantResource() {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of("name"));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_TENANT));
        acl.setAllowedActions(List.of(ACLAction.WRITE_METADATA));
        return acl;
    }

    public static ServerACLModel aclWithPrincipalResource() {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of("name"));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_PRINCIPAL));
        acl.setAllowedActions(List.of(ACLAction.WRITE_METADATA));
        return acl;
    }

    public static ServerACLsModel singleAcl() {
        return ServerACLsModel.fromProto(
                ServerACLs.newBuilder().addAcls(acl().toProto()).build(), ServerACLsModel.class, null);
    }

    public static ServerACLsModel singleAclWithTenantResource() {
        return ServerACLsModel.fromProto(
                ServerACLs.newBuilder()
                        .addAcls(aclWithTenantResource().toProto())
                        .build(),
                ServerACLsModel.class,
                null);
    }

    public static ServerACLsModel singleAclWithPrincipalResource() {
        return ServerACLsModel.fromProto(
                ServerACLs.newBuilder()
                        .addAcls(aclWithPrincipalResource().toProto())
                        .build(),
                ServerACLsModel.class,
                null);
    }

    public static ServerACLModel adminAcl() {
        return adminAcl("name");
    }

    public static ServerACLModel adminAcl(String name) {
        ServerACLModel acl = new ServerACLModel();
        acl.setName(Optional.of(name));
        acl.setPrefix(Optional.empty());
        acl.setResources(List.of(ACLResource.ACL_ALL_RESOURCES));
        acl.setAllowedActions(List.of(ACLAction.ALL_ACTIONS));
        return acl;
    }

    public static ServerACLsModel singleAdminAcl(String aclName) {
        ServerACLs acls =
                ServerACLs.newBuilder().addAcls(adminAcl(aclName).toProto()).build();
        return ServerACLsModel.fromProto(acls, ServerACLsModel.class, null);
    }

    public static KeyValueStore<String, Bytes> testStore(String storeName) {
        return Mockito.spy(
                Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(storeName), Serdes.String(), Serdes.Bytes())
                        .withLoggingDisabled()
                        .build());
    }

    public static FailureHandlerDefModel exceptionHandler(String failureName) {
        FailureHandlerDefModel handlerDef = new FailureHandlerDefModel();
        handlerDef.handlerSpecName = "my-handler";
        handlerDef.specificFailure = failureName;
        return handlerDef;
    }

    public static FailureDefModel exceptionFailureDef(String failureName) {
        FailureDefModel failureDef = new FailureDefModel();
        failureDef.failureName = failureName;
        return failureDef;
    }
}
