package io.littlehorse.sdk.wfsdk.internal;

import com.google.protobuf.Message;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.EdgeCondition;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import io.littlehorse.sdk.common.proto.ExitNode;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import io.littlehorse.sdk.common.proto.FailureDef;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.InterruptDef;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.sdk.common.proto.SleepNode;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsNode;
import io.littlehorse.sdk.common.proto.StartThreadNode;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThrowEventNode;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WaitForConditionNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.wfsdk.IfElseBody;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import io.littlehorse.sdk.wfsdk.WorkflowIfStatement;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
final class WorkflowThreadImpl implements WorkflowThread {

    private WorkflowImpl parent;
    private ThreadSpec.Builder spec;
    private List<WfRunVariableImpl> wfRunVariables = new ArrayList<>();
    public String lastNodeName;
    public String name;
    private EdgeCondition lastNodeCondition;
    private boolean isActive;
    private ThreadRetentionPolicy retentionPolicy;
    private Queue<VariableMutation> variableMutations;

    public WorkflowThreadImpl(String name, WorkflowImpl parent, ThreadFunc func) {
        this.parent = Objects.requireNonNull(parent, "Parent workflow can not be null.");
        this.spec = ThreadSpec.newBuilder();
        this.name = name;
        this.variableMutations = new LinkedList<>();

        // For now, the creation of the entrypoint node is manual.
        Node entrypointNode =
                Node.newBuilder().setEntrypoint(EntrypointNode.newBuilder()).build();

        String entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        lastNodeName = entrypointNodeName;
        spec.putNodes(entrypointNodeName, entrypointNode);
        isActive = true;
        parent.getThreads().push(this);
        // Call the function and do its work
        func.threadFunction(this);

        // Now add an exit node.
        Node node = spec.getNodesOrThrow(lastNodeName);
        if (node.getNodeCase() != NodeCase.EXIT) {
            addNode("exit", NodeCase.EXIT, ExitNode.newBuilder().build());
        }
        isActive = false;

        if (getRetentionPolicy() != null) {
            spec.setRetentionPolicy(getRetentionPolicy());
        }
    }

    public ThreadSpec.Builder getSpec() {
        spec.clearVariableDefs();
        for (WfRunVariableImpl wfRunVariable : wfRunVariables) {
            spec.addVariableDefs(wfRunVariable.getSpec());
        }
        return spec;
    }

    @Override
    public void withRetentionPolicy(ThreadRetentionPolicy policy) {
        this.retentionPolicy = policy;
    }

    private ThreadRetentionPolicy getRetentionPolicy() {
        if (retentionPolicy != null) return retentionPolicy;

        return getParent().getDefaultThreadRetentionPolicy();
    }

    @Override
    public void releaseToGroupOnDeadline(UserTaskOutput userTaskOutput, Object deadlineSeconds) {
        checkIfIsActive();
        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) userTaskOutput;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new IllegalStateException("Tried to edit a stale User Task node!");
        }
        if (!curNode.getUserTask().hasUserId()) {
            throw new IllegalStateException("The User Task is not assigned to any user");
        }
        if (!curNode.getUserTask().hasUserGroup()) {
            throw new IllegalStateException("The User Task is assigned to a user without a group.");
        }
        VariableAssignment userGroup = curNode.getUserTask().getUserGroup();
        reassignToGroupOnDeadline(userGroup, curNode, deadlineSeconds);
    }

    private void reassignToGroupOnDeadline(
            VariableAssignment userGroup, Node.Builder currentNode, Object deadlineSeconds) {
        UTActionTrigger.UTAReassign reassignPb =
                UTActionTrigger.UTAReassign.newBuilder().setUserGroup(userGroup).build();
        UTActionTrigger actionTrigger = UTActionTrigger.newBuilder()
                .setReassign(reassignPb)
                .setHook(UTActionTrigger.UTHook.ON_TASK_ASSIGNED)
                .setDelaySeconds(assignVariable(deadlineSeconds))
                .build();
        currentNode.getUserTaskBuilder().addActions(actionTrigger);
        spec.putNodes(lastNodeName, currentNode.build());
    }

    @Override
    public void reassignUserTask(UserTaskOutput userTask, Object userId, Object userGroup, Object deadlineSeconds) {
        checkIfIsActive();
        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) userTask;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new IllegalStateException("Tried to edit a stale User Task node!");
        }
        UTActionTrigger.UTAReassign.Builder reassignment =
                UTActionTrigger.UTAReassign.newBuilder().setUserId(assignVariable(userId));
        if (userGroup != null) {
            reassignment.setUserGroup(assignVariable(userGroup));
        }
        if (userId != null) {
            reassignment.setUserId(assignVariable(userId));
        }
        UTActionTrigger actionTrigger = UTActionTrigger.newBuilder()
                .setReassign(reassignment)
                .setHook(UTActionTrigger.UTHook.ON_TASK_ASSIGNED)
                .setDelaySeconds(assignVariable(deadlineSeconds))
                .build();
        curNode.getUserTaskBuilder().addActions(actionTrigger);
        spec.putNodes(lastNodeName, curNode.build());
    }

    @Override
    public UserTaskOutputImpl assignUserTask(String userTaskDefName, Object userId, Object userGroup) {
        checkIfIsActive();
        // guaranteed that exatly one of userId or userGroup is not null
        UserTaskNode.Builder utNode = UserTaskNode.newBuilder().setUserTaskDefName(userTaskDefName);

        if (userId instanceof String && ((String) userId).trim().isEmpty()) {
            throw new IllegalArgumentException("UserId can't be empty");
        }

        if (userGroup instanceof String && ((String) userGroup).trim().isEmpty()) {
            throw new IllegalArgumentException("UserGroup can't be empty");
        }

        if (userId != null) {
            VariableAssignment userIdAssn = assignVariable(userId);
            utNode.setUserId(userIdAssn);
        }

        if (userGroup != null) {
            VariableAssignment userGroupAssn = assignVariable(userGroup);
            utNode.setUserGroup(userGroupAssn);
        }

        // TODO LH-313: Return a special subclass of NodeOutputImpl that
        // allows for adding trigger actions

        String nodeName = addNode(userTaskDefName, NodeCase.USER_TASK, utNode.build());
        return new UserTaskOutputImpl(nodeName, this);
    }

    @Override
    public void scheduleReminderTask(
            UserTaskOutput ut, WfRunVariable delaySeconds, String taskDefName, Serializable... args) {
        // List<Object> nextArgs = new ArrayList<>();
        // for (Object arg : args) nextArgs.add(arg);
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, UTActionTrigger.UTHook.ON_ARRIVAL, args);
    }

    @Override
    public void scheduleReminderTask(UserTaskOutput ut, int delaySeconds, String taskDefName, Serializable... args) {
        // List<Object> nextArgs = new ArrayList<>();
        // for (Object arg : args) nextArgs.add(arg);
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, UTActionTrigger.UTHook.ON_ARRIVAL, args);
    }

    @Override
    public void scheduleReminderTaskOnAssignment(
            UserTaskOutput ut, int delaySeconds, String taskDefName, Serializable... args) {
        // List<Object> nextArgs = new ArrayList<>();
        // for (Object arg : args) nextArgs.add(arg);
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, UTActionTrigger.UTHook.ON_TASK_ASSIGNED, args);
    }

    @Override
    public void scheduleReminderTaskOnAssignment(
            UserTaskOutput ut, WfRunVariable delaySeconds, String taskDefName, Serializable... args) {
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, UTActionTrigger.UTHook.ON_TASK_ASSIGNED, args);
    }

    public void scheduleTaskAfterHelper(
            UserTaskOutput ut,
            Serializable delaySeconds,
            String taskDefName,
            UTActionTrigger.UTHook utHook,
            Serializable... args) {
        checkIfIsActive();
        VariableAssignment assn = assignVariable(delaySeconds);
        TaskNode taskNode = createTaskNode(
                TaskNode.newBuilder().setTaskDefId(TaskDefId.newBuilder().setName(taskDefName)), args);
        parent.addTaskDefName(taskDefName);
        UTATask utaTask = UTATask.newBuilder().setTask(taskNode).build();

        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) ut;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new RuntimeException("Tried to edit a stale User Task node!");
        }

        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UTActionTrigger.Builder newUtActionBuilder =
                UTActionTrigger.newBuilder().setTask(utaTask).setHook(utHook).setDelaySeconds(assn);
        curNode.getUserTaskBuilder().addActions(newUtActionBuilder);
        spec.putNodes(lastNodeName, curNode.build());
        // TODO LH-334: return a modified child class of NodeOutput which lets
        // us mutate variables
    }

    public void registerExternalEventDef(ExternalEventNodeOutputImpl nodeOutputImpl) {
        parent.addExternalEventDefToRegister(nodeOutputImpl);
    }

    public void registerWorkflowEventDef(ThrowEventNodeOutputImpl nodeOutputImpl) {
        parent.addWorkflowEventDefToRegister(nodeOutputImpl);
    }

    @Override
    public void cancelUserTaskRunAfter(UserTaskOutput userTask, Serializable delaySeconds) {
        checkIfIsActive();
        scheduleUserTaskCancellationAfterDeadline(userTask, delaySeconds, UTActionTrigger.UTHook.ON_ARRIVAL);
    }

    @Override
    public void cancelUserTaskRunAfterAssignment(UserTaskOutput userTask, Serializable delaySeconds) {
        checkIfIsActive();
        scheduleUserTaskCancellationAfterDeadline(userTask, delaySeconds, UTActionTrigger.UTHook.ON_TASK_ASSIGNED);
    }

    private void scheduleUserTaskCancellationAfterDeadline(
            UserTaskOutput userTask, Serializable delaySeconds, UTActionTrigger.UTHook hook) {
        VariableAssignment assn = assignVariable(delaySeconds);
        UTActionTrigger.UTACancel utaCancel =
                UTActionTrigger.UTACancel.newBuilder().build();
        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) userTask;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new RuntimeException("Tried to edit a stale User Task node!");
        }
        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UTActionTrigger.Builder newUtActionBuilder =
                UTActionTrigger.newBuilder().setCancel(utaCancel).setHook(hook).setDelaySeconds(assn);
        curNode.getUserTaskBuilder().addActions(newUtActionBuilder);
        spec.putNodes(lastNodeName, curNode.build());
    }

    public LHFormatStringImpl format(String format, WfRunVariable... args) {
        return new LHFormatStringImpl(this, format, args);
    }

    @Override
    public TaskNodeOutputImpl execute(String taskName, Serializable... args) {
        checkIfIsActive();
        parent.addTaskDefName(taskName);
        TaskNode taskNode = createTaskNode(
                TaskNode.newBuilder().setTaskDefId(TaskDefId.newBuilder().setName(taskName)), args);
        String nodeName = addNode(taskName, NodeCase.TASK, taskNode);
        return new TaskNodeOutputImpl(nodeName, this);
    }

    @Override
    public TaskNodeOutputImpl execute(WfRunVariable taskName, Serializable... args) {
        checkIfIsActive();
        TaskNode taskNode = createTaskNode(TaskNode.newBuilder().setDynamicTask(assignVariable(taskName)), args);
        String nodeName = addNode(((WfRunVariableImpl) taskName).getName(), NodeCase.TASK, taskNode);
        return new TaskNodeOutputImpl(nodeName, this);
    }

    @Override
    public TaskNodeOutputImpl execute(LHFormatString taskName, Serializable... args) {
        checkIfIsActive();
        TaskNode taskNode = createTaskNode(TaskNode.newBuilder().setDynamicTask(assignVariable(taskName)), args);
        String nodeName = addNode(((LHFormatStringImpl) taskName).getFormat(), NodeCase.TASK, taskNode);
        return new TaskNodeOutputImpl(nodeName, this);
    }

    private TaskNode createTaskNode(TaskNode.Builder taskNode, Serializable... args) {

        for (Object var : args) {
            taskNode.addVariables(assignVariable(var));
        }

        if (parent.getDefaultTaskTimeout() != null) {
            taskNode.setTimeoutSeconds(parent.getDefaultTaskTimeout());
        }

        taskNode.setRetries(parent.getDefaultSimpleRetries());

        if (parent.getDefaultExponentialBackoffRetryPolicy().isPresent()) {
            taskNode.setExponentialBackoff(
                    parent.getDefaultExponentialBackoffRetryPolicy().get());
        }

        return taskNode.build();
    }

    public void checkArgsVsTaskDef(List<VariableDef> taskDefInputVars, String taskDefName, Object... args)
            throws TaskSchemaMismatchError {
        if (args.length != taskDefInputVars.size()) {
            throw new TaskSchemaMismatchError("Mismatched number of arguments!");
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            VariableType argType;

            if (WfRunVariableImpl.class.isAssignableFrom(arg.getClass())) {
                WfRunVariableImpl wfVar = ((WfRunVariableImpl) arg);

                if (wfVar.getDefinedType() == DefinedTypeCase.PRIMITIVE_TYPE
                        && (wfVar.typeDef.getPrimitiveType() == VariableType.JSON_ARR
                                || wfVar.typeDef.getPrimitiveType() == VariableType.JSON_OBJ)
                        && wfVar.jsonPath != null) {
                    log.info("There is a jsonpath, so not checking value because Json schema isn't"
                            + " yet implemented");
                    continue;
                }
                argType = wfVar.typeDef.getPrimitiveType();
            } else {
                argType = LHLibUtil.javaClassToLHVarType(arg.getClass());
            }

            if (!argType.equals(taskDefInputVars.get(i).getTypeDef().getPrimitiveType())) {
                throw new TaskSchemaMismatchError("Mismatch var type for param "
                        + i
                        + "on taskdef "
                        + taskDefName
                        + ": "
                        + argType
                        + " not compatible with "
                        + taskDefInputVars.get(i).getTypeDef());
            }
        }
    }

    @Override
    public LHExpression multiply(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.MULTIPLY, rhs);
    }

    @Override
    public LHExpression add(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.ADD, rhs);
    }

    @Override
    public LHExpression divide(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.DIVIDE, rhs);
    }

    @Override
    public LHExpression subtract(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.SUBTRACT, rhs);
    }

    @Override
    public LHExpression extend(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.EXTEND, rhs);
    }

    @Override
    public LHExpression removeIfPresent(Serializable lhs, Serializable rhs) {
        return new LHExpressionImpl(lhs, VariableMutationType.REMOVE_IF_PRESENT, rhs);
    }

    @Override
    public LHExpression removeIndex(Serializable lhs, Serializable index) {
        return new LHExpressionImpl(lhs, VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(Serializable lhs, Serializable key) {
        return new LHExpressionImpl(lhs, VariableMutationType.REMOVE_KEY, key);
    }

    public WfRunVariableImpl addVariable(String name, Object typeOrDefaultVal) {
        checkIfIsActive();
        WfRunVariableImpl wfRunVariable = WfRunVariableImpl.createPrimitiveVar(name, typeOrDefaultVal, this);
        wfRunVariables.add(wfRunVariable);
        return wfRunVariable;
    }

    private WfRunVariableImpl addStructVariable(String name, LHStructDefType clazz) {
        checkIfIsActive();

        WfRunVariableImpl wfRunVariable = WfRunVariableImpl.createStructDefVar(name, clazz, this);
        wfRunVariables.add(wfRunVariable);
        return wfRunVariable;
    }

    @Override
    public WfRunVariable declareBool(String name) {
        return addVariable(name, VariableType.BOOL);
    }

    @Override
    public WfRunVariable declareInt(String name) {
        return addVariable(name, VariableType.INT);
    }

    @Override
    public WfRunVariable declareStr(String name) {
        return addVariable(name, VariableType.STR);
    }

    @Override
    public WfRunVariable declareDouble(String name) {
        return addVariable(name, VariableType.DOUBLE);
    }

    @Override
    public WfRunVariable declareBytes(String name) {
        return addVariable(name, VariableType.BYTES);
    }

    @Override
    public WfRunVariable declareJsonArr(String name) {
        return addVariable(name, VariableType.JSON_ARR);
    }

    @Override
    public WfRunVariable declareJsonObj(String name) {
        return addVariable(name, VariableType.JSON_OBJ);
    }

    @Override
    public WfRunVariable declareStruct(String name, Class<?> clazz) {
        return addStructVariable(name, new LHStructDefType(clazz));
    }

    @Override
    public WorkflowIfStatement doIf(WorkflowCondition condition, IfElseBody ifBody) {
        WorkflowConditionImpl cond = (WorkflowConditionImpl) condition;

        addNopNode();
        String firstNopNodeName = lastNodeName;
        lastNodeCondition = cond.getSpec();

        ifBody.body(this);

        addNopNode();
        String lastNopNodeName = lastNodeName;

        Node.Builder treeRoot = spec.getNodesOrThrow(firstNopNodeName).toBuilder();
        treeRoot.addOutgoingEdges(
                Edge.newBuilder().setSinkNodeName(lastNopNodeName).build());
        spec.putNodes(firstNopNodeName, treeRoot.build());

        return new WorkflowIfStatementImpl(this, firstNopNodeName, lastNopNodeName);
    }

    WorkflowIfStatement doElseIf(
            WorkflowIfStatement inputIfStatement, WorkflowCondition inputCondition, IfElseBody ifElseBody) {
        WorkflowIfStatementImpl ifStatement = (WorkflowIfStatementImpl) inputIfStatement;
        // Remove else edge from firstNopNode
        Edge elseEdge = removeLastOutgoingEdgeFromNode(ifStatement.getFirstNopNodeName());

        // Get the last Node of the parent thread
        String lastNodeOfParentThreadName = lastNodeName;

        ifElseBody.body(this);

        String lastNodeOfBodyName = lastNodeName;

        // If no nodes were added from the body...
        if (lastNodeOfParentThreadName.equals(lastNodeOfBodyName)) {
            Edge.Builder edgeToNopNode = Edge.newBuilder()
                    .setSinkNodeName(ifStatement.getLastNopNodeName())
                    .addAllVariableMutations(this.collectVariableMutations());

            if (inputCondition != null) {
                edgeToNopNode.setCondition(((WorkflowConditionImpl) inputCondition).getSpec());
            }

            addOutgoingEdgeToNode(ifStatement.getFirstNopNodeName(), edgeToNopNode.build());
        } else {
            // Remove edge between last node of parent thread and first node of body
            Edge lastOutgoingEdge = removeLastOutgoingEdgeFromNode(lastNodeOfParentThreadName);

            String firstNodeOfBodyName = lastOutgoingEdge.getSinkNodeName();

            // Add edge from the firstNopNode to the first node of the body
            Edge.Builder edgeToBody = Edge.newBuilder()
                    .setSinkNodeName(firstNodeOfBodyName)
                    .addAllVariableMutations(lastOutgoingEdge.getVariableMutationsList());

            if (inputCondition != null) {
                edgeToBody.setCondition(((WorkflowConditionImpl) inputCondition).getSpec());
            }

            addOutgoingEdgeToNode(ifStatement.getFirstNopNodeName(), edgeToBody.build());

            // Add edge from last node of the body to last NOP node
            addOutgoingEdgeToNode(
                    lastNodeOfBodyName,
                    Edge.newBuilder()
                            .setSinkNodeName(ifStatement.getLastNopNodeName())
                            .addAllVariableMutations(this.collectVariableMutations())
                            .build());
        }

        // If else condition was not specified, add back 'else' edge we removed earlier
        if (inputCondition != null) {
            addOutgoingEdgeToNode(ifStatement.getFirstNopNodeName(), elseEdge);
        }

        lastNodeName = lastNodeOfParentThreadName;

        return ifStatement;
    }

    private Edge removeLastOutgoingEdgeFromNode(String nodeName) {
        Node.Builder nodeBuilder = this.spec.getNodesOrThrow(nodeName).toBuilder();
        ArrayList<Edge> outgoingEdges = new ArrayList<>(nodeBuilder.getOutgoingEdgesList());
        Edge lastEdge = outgoingEdges.remove(outgoingEdges.size() - 1);
        nodeBuilder.clearOutgoingEdges();
        nodeBuilder.addAllOutgoingEdges(outgoingEdges);
        this.spec.putNodes(nodeName, nodeBuilder.build());
        return lastEdge;
    }

    private void addOutgoingEdgeToNode(String nodeName, Edge edge) {
        Node.Builder nodeBuilder = this.spec.getNodesOrThrow(nodeName).toBuilder();
        nodeBuilder.addOutgoingEdges(edge);
        this.spec.putNodes(nodeName, nodeBuilder.build());
    }

    private void addNopNode() {
        checkIfIsActive();
        addNode("nop", NodeCase.NOP, NopNode.newBuilder().build());
    }

    @Override
    public void doIfElse(WorkflowCondition condition, IfElseBody ifBody, IfElseBody elseBody) {
        checkIfIsActive();

        this.doIf(condition, ifBody).doElse(elseBody);
    }

    private List<VariableMutation> collectVariableMutations() {
        List<VariableMutation> variablesFromIfBlock = new ArrayList<>(variableMutations.size());
        while (!variableMutations.isEmpty()) {
            variablesFromIfBlock.add(variableMutations.poll());
        }
        return variablesFromIfBlock;
    }

    @Override
    public void doWhile(WorkflowCondition condition, ThreadFunc whileBody) {
        checkIfIsActive();
        WorkflowConditionImpl cond = (WorkflowConditionImpl) condition;

        // Start a new tree. Basically, we gotta take the last node we ran
        // then we need to put a condition on it...
        addNopNode();
        String treeRootNodeName = lastNodeName;

        lastNodeCondition = cond.getSpec();
        // execute the tasks
        whileBody.threadFunction(this);

        // close off the tree
        addNopNode();
        String treeLastNodeName = lastNodeName;

        // Now add the sideways path from root directly to last
        Node.Builder treeRoot = spec.getNodesOrThrow(treeRootNodeName).toBuilder();
        treeRoot.addOutgoingEdges(Edge.newBuilder()
                .setSinkNodeName(treeLastNodeName)
                .setCondition(cond.getReverse())
                .build());
        spec.putNodes(treeRootNodeName, treeRoot.build());

        // Now add the sideways path from last directly to root
        Node.Builder treeLast = spec.getNodesOrThrow(treeLastNodeName).toBuilder();
        treeLast.addOutgoingEdges(Edge.newBuilder()
                .setSinkNodeName(treeRootNodeName)
                .setCondition(cond.getSpec())
                .build());

        spec.putNodes(treeLastNodeName, treeLast.build());
    }

    @Override
    public SpawnedThreads spawnThreadForEach(WfRunVariable wfRunVariable, String threadName, ThreadFunc threadFunc) {
        return spawnThreadForEach(wfRunVariable, threadName, threadFunc, Map.of());
    }

    @Override
    public SpawnedThreads spawnThreadForEach(
            WfRunVariable wfRunVariable, String threadName, ThreadFunc threadFunc, Map<String, Object> inputVars) {

        checkIfIsActive();
        String finalThreadName = parent.addSubThread(threadName, threadFunc);
        StartMultipleThreadsNode.Builder startMultiplesThreadNode = StartMultipleThreadsNode.newBuilder()
                .setThreadSpecName(finalThreadName)
                .setIterable(assignVariable(wfRunVariable));

        for (Map.Entry<String, Object> inputVar : inputVars.entrySet()) {
            startMultiplesThreadNode.putVariables(inputVar.getKey(), assignVariable(inputVar.getValue()));
        }

        String nodeName = addNode(threadName, NodeCase.START_MULTIPLE_THREADS, startMultiplesThreadNode.build());
        WfRunVariableImpl internalStartedThreadVar = addVariable(nodeName, VariableType.JSON_ARR);
        mutate(internalStartedThreadVar, VariableMutationType.ASSIGN, new NodeOutputImpl(nodeName, this));
        return new SpawnedThreadsIterator(internalStartedThreadVar);
    }

    @Override
    public void sleepSeconds(Object secondsToSleep) {
        checkIfIsActive();
        SleepNode.Builder n = SleepNode.newBuilder().setRawSeconds(assignVariable(secondsToSleep));
        addNode("sleep", NodeCase.SLEEP, n.build());
    }

    @Override
    public void sleepUntil(WfRunVariable timestamp) {
        checkIfIsActive();
        SleepNode.Builder n = SleepNode.newBuilder().setTimestamp(assignVariable(timestamp));
        addNode("sleep", NodeCase.SLEEP, n.build());
    }

    @Override
    public SpawnedThreadImpl spawnThread(ThreadFunc threadFunc, String threadName, Map<String, Object> inputVars) {
        checkIfIsActive();
        if (inputVars == null) {
            inputVars = new HashMap<>();
        }
        threadName = parent.addSubThread(threadName, threadFunc);

        Map<String, VariableAssignment> varAssigns = new HashMap<>();
        for (Map.Entry<String, Object> var : inputVars.entrySet()) {
            varAssigns.put(var.getKey(), assignVariable(var.getValue()));
        }

        StartThreadNode startThread = StartThreadNode.newBuilder()
                .setThreadSpecName(threadName)
                .putAllVariables(varAssigns)
                .build();

        String nodeName = addNode(threadName, NodeCase.START_THREAD, startThread);
        WfRunVariableImpl internalStartedThreadVar = addVariable(nodeName, VariableType.INT);

        // The output of a StartThreadNode is just an integer containing the name
        // of the thread.
        mutate(internalStartedThreadVar, VariableMutationType.ASSIGN, new NodeOutputImpl(nodeName, this));

        return new SpawnedThreadImpl(this, threadName, internalStartedThreadVar);
    }

    public void overrideTaskRetries(TaskNodeOutputImpl node, int retries) {
        checkIfIsActive();
        Node.Builder nb = spec.getNodesOrThrow(node.nodeName).toBuilder();
        if (nb.getNodeCase() != NodeCase.TASK) {
            throw new IllegalStateException("Impossible to not have task node here");
        }

        TaskNode.Builder taskBuilder = nb.getTaskBuilder();
        taskBuilder.setRetries(retries);

        nb.setTask(taskBuilder);
        spec.putNodes(node.nodeName, nb.build());
    }

    public void overrideTaskExponentialBackoffPolicy(TaskNodeOutputImpl node, ExponentialBackoffRetryPolicy policy) {
        checkIfIsActive();
        Node.Builder nb = spec.getNodesOrThrow(node.nodeName).toBuilder();
        if (nb.getNodeCase() != NodeCase.TASK) {
            throw new IllegalStateException("Impossible to not have task node here");
        }

        TaskNode.Builder taskBuilder = nb.getTaskBuilder();
        taskBuilder.setExponentialBackoff(policy);

        nb.setTask(taskBuilder);
        spec.putNodes(node.nodeName, nb.build());
    }

    public void addCorrelationIdToExtEvtNode(
            ExternalEventNodeOutputImpl node, Serializable correlationId, boolean maskCorrelationKey) {
        Node.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();

        VariableAssignment correlationValue = assignVariable(correlationId);

        ExternalEventNode.Builder evt = n.getExternalEventBuilder();
        evt.setCorrelationKey(correlationValue);
        evt.setMaskCorrelationKey(maskCorrelationKey);
        n.setExternalEvent(evt);

        spec.putNodes(node.nodeName, n.build());
    }

    public void addTimeoutToExtEvtNode(ExternalEventNodeOutputImpl node, int timeoutSeconds) {
        checkIfIsActive();
        Node.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();

        VariableAssignment timeoutValue = VariableAssignment.newBuilder()
                .setLiteralValue(VariableValue.newBuilder().setInt(timeoutSeconds))
                .build();

        ExternalEventNode.Builder evt = n.getExternalEventBuilder();
        evt.setTimeoutSeconds(timeoutValue);
        n.setExternalEvent(evt);

        spec.putNodes(node.nodeName, n.build());
    }

    public void addTimeoutToTaskNode(TaskNodeOutputImpl node, int timeoutSeconds) {
        checkIfIsActive();
        Node.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();

        TaskNode.Builder task = n.getTaskBuilder();
        task.setTimeoutSeconds(timeoutSeconds);
        n.setTask(task);

        spec.putNodes(node.nodeName, n.build());
    }

    public void addFailureHandlerOnWaitForThreadsNode(WaitForThreadsNodeOutputImpl node, FailureHandlerDef handler) {
        checkIfIsActive();
        Node.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();

        if (n.getNodeCase() != NodeCase.WAIT_FOR_THREADS) {
            throw new IllegalStateException("orzdash this should only be a WAIT_FOR_THREADS node");
        }

        WaitForThreadsNode.Builder subBuilder = n.getWaitForThreadsBuilder();
        subBuilder.addPerThreadFailureHandlers(handler);
        n.setWaitForThreads(subBuilder);

        spec.putNodes(node.nodeName, n.build());
    }

    @Override
    public void mutate(WfRunVariable lhsVar, VariableMutationType type, Object rhs) {
        checkIfIsActive();
        WfRunVariableImpl lhs = (WfRunVariableImpl) lhsVar;
        VariableMutation.Builder mutation =
                VariableMutation.newBuilder().setLhsName(lhs.name).setOperation(type);

        if (lhs.jsonPath != null) {
            mutation.setLhsJsonPath(lhs.jsonPath);
        }

        mutation.setRhsAssignment(assignVariable(rhs));

        this.variableMutations.add(mutation.build());
    }

    @Override
    public WaitForThreadsNodeOutput waitForThreads(SpawnedThreads threads) {
        checkIfIsActive();
        WaitForThreadsNode waitNode = threads.buildNode();
        String nodeName = addNode("threads", NodeCase.WAIT_FOR_THREADS, waitNode);
        return new WaitForThreadsNodeOutputImpl(nodeName, this, spec);
    }

    @Override
    public ThrowEventNodeOutputImpl throwEvent(String workflowEventDefName, Serializable content) {
        checkIfIsActive();
        parent.addWorkflowEventDefName(workflowEventDefName);
        ThrowEventNode node = ThrowEventNode.newBuilder()
                .setEventDefId(WorkflowEventDefId.newBuilder()
                        .setName(workflowEventDefName)
                        .build())
                .setContent(assignVariable(content))
                .build();
        addNode("throw-" + workflowEventDefName, NodeCase.THROW_EVENT, node);
        return new ThrowEventNodeOutputImpl(workflowEventDefName, this);
    }

    @Override
    public ExternalEventNodeOutputImpl waitForEvent(String externalEventDefName) {
        checkIfIsActive();
        ExternalEventNode waitNode = ExternalEventNode.newBuilder()
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(externalEventDefName))
                .build();

        parent.addExternalEventDefName(externalEventDefName);

        return new ExternalEventNodeOutputImpl(
                addNode(externalEventDefName, NodeCase.EXTERNAL_EVENT, waitNode), externalEventDefName, this);
    }

    @Override
    public WaitForConditionNodeOutputImpl waitForCondition(WorkflowCondition condition) {
        checkIfIsActive();
        WorkflowConditionImpl condImpl = (WorkflowConditionImpl) condition;
        WaitForConditionNode waitNode = WaitForConditionNode.newBuilder()
                .setCondition(condImpl.getSpec())
                .build();

        String nodeName = addNode("wait-for-condition", NodeCase.WAIT_FOR_CONDITION, waitNode);
        return new WaitForConditionNodeOutputImpl(nodeName, this);
    }

    public void complete() {
        checkIfIsActive();
        ExitNode exitNode = ExitNode.newBuilder().build();
        addNode("complete", NodeCase.EXIT, exitNode);
    }

    public void complete(Serializable result) {
        checkIfIsActive();
        ExitNode exitNode =
                ExitNode.newBuilder().setReturnContent(assignVariable(result)).build();

        addNode("complete", NodeCase.EXIT, exitNode);
    }

    public void fail(String failureName, String message) {
        fail(null, failureName, message);
    }

    public void fail(Object output, String failureName, String message) {
        checkIfIsActive();
        FailureDef.Builder failureBuilder = FailureDef.newBuilder();
        if (output != null) failureBuilder.setContent(assignVariable(output));
        if (message != null) failureBuilder.setMessage(message);
        failureBuilder.setFailureName(failureName);

        ExitNode exitNode = ExitNode.newBuilder().setFailureDef(failureBuilder).build();

        addNode(failureName, NodeCase.EXIT, exitNode);
    }

    public void registerInterruptHandler(String interruptName, ThreadFunc handler) {
        checkIfIsActive();
        String threadName = "interrupt-" + interruptName;
        threadName = parent.addSubThread(threadName, handler);
        parent.addExternalEventDefName(interruptName);

        spec.addInterruptDefs(InterruptDef.newBuilder()
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(interruptName))
                .setHandlerSpecName(threadName)
                .build());
    }

    public void handleException(NodeOutput nodeOutput, String exceptionName, ThreadFunc handler) {
        addExceptionHandler(nodeOutput, exceptionName, handler);
    }

    @Override
    public void handleException(NodeOutput node, ThreadFunc handler) {
        addExceptionHandler(node, null, handler);
    }

    @Override
    public void handleError(NodeOutput node, LHErrorType error, ThreadFunc handler) {
        addErrorHandler(node, error, handler);
    }

    @Override
    public void handleError(NodeOutput node, ThreadFunc handler) {
        addErrorHandler(node, null, handler);
    }

    @Override
    public void handleAnyFailure(NodeOutput nodeOutput, ThreadFunc handler) {
        checkIfIsActive();
        NodeOutputImpl node = (NodeOutputImpl) nodeOutput;
        String threadName = "exn-handler-" + node.nodeName + "-any-failure";
        threadName = parent.addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        addFailureHandlerDef(handlerDef.build(), node);
    }

    private void addFailureHandlerDef(FailureHandlerDef handlerDef, NodeOutputImpl node) {
        // Add the failure handler to the most recent node
        Node.Builder lastNodeBuilder = spec.getNodesOrThrow(node.nodeName).toBuilder();

        lastNodeBuilder.addFailureHandlers(handlerDef);
        spec.putNodes(node.nodeName, lastNodeBuilder.build());
    }

    private void addExceptionHandler(NodeOutput nodeOutput, String exceptionName, ThreadFunc handler) {
        checkIfIsActive();
        NodeOutputImpl node = (NodeOutputImpl) nodeOutput;
        String threadName = "exn-handler-" + node.nodeName + "-" + exceptionName;
        threadName = parent.addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        if (exceptionName != null) {
            handlerDef.setSpecificFailure(exceptionName);
        } else {
            handlerDef.setAnyFailureOfType(FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION);
        }
        addFailureHandlerDef(handlerDef.build(), node);
    }

    private void addErrorHandler(NodeOutput nodeOutput, LHErrorType errorType, ThreadFunc handler) {
        checkIfIsActive();
        NodeOutputImpl node = (NodeOutputImpl) nodeOutput;
        String threadName = "exn-handler-" + node.nodeName + "-"
                + (errorType != null ? errorType.name() : FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR);
        threadName = parent.addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        if (errorType != null) {
            handlerDef.setSpecificFailure(errorType.name());
        } else {
            handlerDef.setAnyFailureOfType(FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR);
        }
        addFailureHandlerDef(handlerDef.build(), node);
    }

    public WorkflowConditionImpl condition(Object lhs, Comparator comparator, Object rhs) {
        EdgeCondition.Builder edge = EdgeCondition.newBuilder()
                .setComparator(comparator)
                .setLeft(assignVariable(lhs))
                .setRight(assignVariable(rhs));

        return new WorkflowConditionImpl(edge.build());
    }

    private String addNode(String name, NodeCase type, Message subNode) {
        checkIfIsActive();
        String nextNodeName = getNodeName(name, type);

        if (lastNodeName == null) {
            throw new IllegalStateException("Not possible to have null last node here");
        }

        Node.Builder feederNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        Edge.Builder edge = Edge.newBuilder().setSinkNodeName(nextNodeName);

        edge.addAllVariableMutations(collectVariableMutations());

        if (lastNodeCondition != null) {
            edge.setCondition(lastNodeCondition);
            lastNodeCondition = null;
        }
        if (feederNode.getNodeCase() != NodeCase.EXIT) {
            feederNode.addOutgoingEdges(edge);
            spec.putNodes(lastNodeName, feederNode.build());
        }

        Node.Builder node = Node.newBuilder();
        switch (type) {
            case TASK:
                node.setTask((TaskNode) subNode);
                break;
            case ENTRYPOINT:
                node.setEntrypoint((EntrypointNode) subNode);
                break;
            case EXIT:
                node.setExit((ExitNode) subNode);
                break;
            case EXTERNAL_EVENT:
                node.setExternalEvent((ExternalEventNode) subNode);
                break;
            case SLEEP:
                node.setSleep((SleepNode) subNode);
                break;
            case START_THREAD:
                node.setStartThread((StartThreadNode) subNode);
                break;
            case WAIT_FOR_THREADS:
                node.setWaitForThreads((WaitForThreadsNode) subNode);
                break;
            case NOP:
                node.setNop((NopNode) subNode);
                break;
            case USER_TASK:
                node.setUserTask((UserTaskNode) subNode);
                break;
            case START_MULTIPLE_THREADS:
                node.setStartMultipleThreads((StartMultipleThreadsNode) subNode);
                break;
            case THROW_EVENT:
                node.setThrowEvent((ThrowEventNode) subNode);
                break;
            case WAIT_FOR_CONDITION:
                node.setWaitForCondition((WaitForConditionNode) subNode);
                break;
            case NODE_NOT_SET:
                // not possible
                throw new RuntimeException("Not possible");
        }

        spec.putNodes(nextNodeName, node.build());
        lastNodeName = nextNodeName;

        return nextNodeName;
    }

    private String getNodeName(String name, NodeCase type) {
        return "" + spec.getNodesCount() + "-" + name + "-" + type;
    }

    public VariableAssignment assignVariable(Object variable) {
        checkIfIsActive();
        return BuilderUtil.assignVariable(variable);
    }

    private void checkIfIsActive() {
        if (!isActive) {
            throw new RuntimeException("Using a inactive thread");
        }
    }
}
