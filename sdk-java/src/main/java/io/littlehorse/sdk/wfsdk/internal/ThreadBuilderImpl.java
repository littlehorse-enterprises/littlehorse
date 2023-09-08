package io.littlehorse.sdk.wfsdk.internal;

import com.google.protobuf.Message;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.EdgeCondition;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import io.littlehorse.sdk.common.proto.ExitNode;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import io.littlehorse.sdk.common.proto.FailureDef;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.InterruptDef;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.sdk.common.proto.SleepNode;
import io.littlehorse.sdk.common.proto.StartMultipleThreadsNode;
import io.littlehorse.sdk.common.proto.StartThreadNode;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTATask;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.FormatString;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadToWaitFor;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.wfsdk.IfElseBody;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
final class ThreadBuilderImpl implements ThreadBuilder {

    private WorkflowImpl parent;
    private ThreadSpec.Builder spec;
    private List<WfRunVariableImpl> wfRunVariables = new ArrayList<>();
    public String lastNodeName;
    public String name;
    private EdgeCondition lastNodeCondition;
    private boolean isActive;

    public ThreadBuilderImpl(String name, WorkflowImpl parent, ThreadFunc func) {
        this.parent = parent;
        this.spec = ThreadSpec.newBuilder();
        this.name = name;

        // For now, the creation of the entrypoint node is manual.
        Node entrypointNode =
                Node.newBuilder().setEntrypoint(EntrypointNode.newBuilder()).build();

        String entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        lastNodeName = entrypointNodeName;
        spec.putNodes(entrypointNodeName, entrypointNode);
        isActive = true;
        // Call the function and do its work
        func.threadFunction(this);

        // Now add an exit node.
        addNode("exit", NodeCase.EXIT, ExitNode.newBuilder().build());
        isActive = false;
    }

    public ThreadSpec.Builder getSpec() {
        spec.clearVariableDefs();
        for (WfRunVariableImpl wfRunVariable : wfRunVariables) {
            spec.addVariableDefs(wfRunVariable.getSpec());
        }
        return spec;
    }

    public UserTaskOutputImpl assignTaskToUser(String userTaskDefName, String userId) {
        return assignUserTaskHelper(userTaskDefName, userId, null);
    }

    @Override
    public UserTaskOutput assignTaskToUser(String userTaskDefName, String userId, String userGroup) {
        return assignUserTaskHelper(userTaskDefName, userId, userGroup);
    }

    @Override
    public void reassignToGroupOnDeadline(UserTaskOutput userTaskOutput, int deadlineSeconds) {
        checkIfIsActive();
        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) userTaskOutput;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new IllegalStateException("Tried to edit a stale User Task node!");
        }
        UserTaskNode.UserAssignment userAssignment =
                curNode.getUserTaskBuilder().getUser();
        if (userAssignment == null) {
            throw new IllegalStateException("The User Task is not assigned to any user");
        }
        VariableAssignment userGroup = userAssignment.getUserGroup();
        if (userGroup == null) {
            throw new IllegalStateException("The User Task is assigned to a user without a group.");
        }
        UTActionTrigger.UTAReassign reassignPb =
                UTActionTrigger.UTAReassign.newBuilder().setUserGroup(userGroup).build();
        UTActionTrigger actionTrigger = UTActionTrigger.newBuilder()
                .setReassign(reassignPb)
                .setHook(UTActionTrigger.UTHook.ON_TASK_ASSIGNED)
                .setDelaySeconds(assignVariable(deadlineSeconds))
                .build();
        curNode.getUserTaskBuilder().addActions(actionTrigger);
        spec.putNodes(lastNodeName, curNode.build());
    }

    @Override
    public void reassignToUserOnDeadline(UserTaskOutput userTaskOutput, String userId, int deadlineSeconds) {
        checkIfIsActive();
        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) userTaskOutput;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new IllegalStateException("Tried to edit a stale User Task node!");
        }
        UTActionTrigger.UTAReassign reassignPb = UTActionTrigger.UTAReassign.newBuilder()
                .setUserId(assignVariable(userId))
                .build();
        UTActionTrigger actionTrigger = UTActionTrigger.newBuilder()
                .setReassign(reassignPb)
                .setHook(UTActionTrigger.UTHook.ON_TASK_ASSIGNED)
                .setDelaySeconds(assignVariable(deadlineSeconds))
                .build();
        curNode.getUserTaskBuilder().addActions(actionTrigger);
        spec.putNodes(lastNodeName, curNode.build());
    }

    public UserTaskOutputImpl assignTaskToUser(String userTaskDefName, WfRunVariable userId) {
        return assignUserTaskHelper(userTaskDefName, userId, null);
    }

    @Override
    public UserTaskOutput assignTaskToUser(String userTaskDefName, WfRunVariable userId, String userGroup) {
        return assignUserTaskHelper(userTaskDefName, userId, userGroup);
    }

    @Override
    public UserTaskOutput assignTaskToUser(String userTaskDefName, WfRunVariable userId, WfRunVariable userGroup) {
        return assignUserTaskHelper(userTaskDefName, userId, userGroup);
    }

    public UserTaskOutputImpl assignTaskToUserGroup(String userTaskDefName, String userGroup) {
        return assignUserTaskHelper(userTaskDefName, null, userGroup);
    }

    public UserTaskOutputImpl assignTaskToUserGroup(String userTaskDefName, WfRunVariable userGroup) {
        return assignUserTaskHelper(userTaskDefName, null, userGroup);
    }

    private UserTaskOutputImpl assignUserTaskHelper(String userTaskDefName, Object userId, Object userGroups) {
        checkIfIsActive();
        // guaranteed that exatly one of userId or userGroup is not null
        UserTaskNode.Builder utNode = UserTaskNode.newBuilder().setUserTaskDefName(userTaskDefName);

        if (userId != null) {
            VariableAssignment userIdAssn = assignVariable(userId);
            VariableAssignment userGroupAssn = userGroups != null ? assignVariable(userGroups) : null;
            UserTaskNode.UserAssignment.Builder userAssignmentBuilder = UserTaskNode.UserAssignment.newBuilder();
            userAssignmentBuilder.setUserId(userIdAssn);
            if (userGroupAssn != null) {
                userAssignmentBuilder.setUserGroup(userGroupAssn);
            }
            utNode.setUser(userAssignmentBuilder);
        } else {
            VariableAssignment userIdAssn = assignVariable(userGroups);
            utNode.setUserGroup(userIdAssn);
        }

        // TODO LH-313: Return a special subclass of NodeOutputImpl that
        // allows for adding trigger actions

        String nodeName = addNode(userTaskDefName, NodeCase.USER_TASK, utNode.build());
        return new UserTaskOutputImpl(nodeName, this);
    }

    public void scheduleReminderTask(
            UserTaskOutput ut, WfRunVariable delaySeconds, String taskDefName, Object... args) {
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, args);
    }

    public void scheduleReminderTask(UserTaskOutput ut, int delaySeconds, String taskDefName, Object... args) {
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, args);
    }

    public void scheduleTaskAfterHelper(UserTaskOutput ut, Object delaySeconds, String taskDefName, Object... args) {
        checkIfIsActive();
        VariableAssignment assn = assignVariable(delaySeconds);
        TaskNode taskNode = createTaskNode(taskDefName, args);
        UTATask utaTask = UTATask.newBuilder().setTask(taskNode).build();

        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) ut;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new RuntimeException("Tried to edit a stale User Task node!");
        }

        Node.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        UTActionTrigger.Builder newUtActionBuilder = UTActionTrigger.newBuilder()
                .setTask(utaTask)
                .setHook(UTActionTrigger.UTHook.ON_ARRIVAL)
                .setDelaySeconds(assn);
        curNode.getUserTaskBuilder().addActions(newUtActionBuilder);
        spec.putNodes(lastNodeName, curNode.build());
        // TODO LH-334: return a modified child class of NodeOutput which lets
        // us mutate variables
    }

    public LHFormatStringImpl format(String format, WfRunVariable... args) {
        return new LHFormatStringImpl(this, format, args);
    }

    public NodeOutputImpl execute(String taskName, Object... args) {
        checkIfIsActive();
        TaskNode taskNode = createTaskNode(taskName, args);
        String nodeName = addNode(taskName, NodeCase.TASK, taskNode);
        return new NodeOutputImpl(nodeName, this);
    }

    private TaskNode createTaskNode(String taskName, Object... args) {
        TaskNode.Builder taskNode = TaskNode.newBuilder().setTaskDefName(taskName);
        parent.addTaskDefName(taskName);

        for (Object var : args) {
            taskNode.addVariables(assignVariable(var));
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

                if ((wfVar.type == VariableType.JSON_ARR || wfVar.type == VariableType.JSON_OBJ)
                        && wfVar.jsonPath != null) {
                    log.info("There is a jsonpath, so not checking value because Json schema isn't"
                            + " yet implemented");
                    continue;
                }
                argType = wfVar.type;
            } else {
                argType = LHLibUtil.javaClassToLHVarType(arg.getClass());
            }

            if (!argType.equals(taskDefInputVars.get(i).getType())) {
                throw new TaskSchemaMismatchError("Mismatch var type for param "
                        + i
                        + "on taskdef "
                        + taskDefName
                        + ": "
                        + argType
                        + " not compatible with "
                        + taskDefInputVars.get(i).getType());
            }
        }
    }

    public void addMutationToCurrentNode(VariableMutation mutation) {
        checkIfIsActive();
        Node.Builder builder = spec.getNodesOrThrow(lastNodeName).toBuilder();
        builder.addVariableMutations(mutation);
        spec.putNodes(lastNodeName, builder.build());
    }

    public WfRunVariableImpl addVariable(String name, Object typeOrDefaultVal) {
        checkIfIsActive();
        WfRunVariableImpl wfRunVariable = new WfRunVariableImpl(name, typeOrDefaultVal);
        wfRunVariables.add(wfRunVariable);
        return wfRunVariable;
    }

    public void doIf(WorkflowCondition condition, IfElseBody ifBody) {
        checkIfIsActive();
        WorkflowConditionImpl cond = (WorkflowConditionImpl) condition;

        // Start a new tree. Basically, we gotta take the last node we ran
        // then we need to put a condition on it...
        addNopNode();
        String treeRootNodeName = lastNodeName;
        lastNodeCondition = cond.getSpec();

        // execute the tasks
        ifBody.body(this);

        // close off the tree
        addNopNode();

        Node.Builder treeRoot = spec.getNodesOrThrow(treeRootNodeName).toBuilder();
        treeRoot.addOutgoingEdges(Edge.newBuilder()
                .setSinkNodeName(lastNodeName)
                .setCondition(cond.getReverse())
                .build());
        spec.putNodes(treeRootNodeName, treeRoot.build());
    }

    private void addNopNode() {
        checkIfIsActive();
        addNode("nop", NodeCase.NOP, NopNode.newBuilder().build());
    }

    public void doIfElse(WorkflowCondition condition, IfElseBody ifBody, IfElseBody elseBody) {
        checkIfIsActive();
        WorkflowConditionImpl cond = (WorkflowConditionImpl) condition;

        // start a tree and do an if.
        addNopNode();
        String treeRootNodeName = lastNodeName;
        lastNodeCondition = cond.getSpec();
        ifBody.body(this);

        // Close off the tree. The bottom node from the ifBlock tree is also
        // going to be the bottom node from the elseBlock.
        addNopNode();
        String joinerNodeName = lastNodeName;

        // Now go back to tree root and do the else.
        lastNodeName = treeRootNodeName; // back to tree root
        lastNodeCondition = cond.getReverse(); // flip to else {}
        elseBody.body(this); // do the body

        // Now need to join the last node to the joiner node.
        if (lastNodeCondition != null) {
            throw new RuntimeException("Not possible to have lastNodeCondition after internal call to "
                    + "elseBody.body(this); please contact maintainers. This is a bug.");
        }

        Node.Builder lastNodeFromElseBlock = spec.getNodesOrThrow(lastNodeName).toBuilder();
        lastNodeFromElseBlock.addOutgoingEdges(Edge.newBuilder()
                .setSinkNodeName(joinerNodeName)
                // No condition necessary since we need to just go straight to
                // the joiner node
                .build());

        spec.putNodes(lastNodeName, lastNodeFromElseBlock.build());

        // Now we want to resume from the joiner node
        lastNodeName = joinerNodeName;
    }

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
        return new SpawnedThreadsImpl(this, threadName, internalStartedThreadVar);
    }

    public void sleepSeconds(Object secondsToSleep) {
        checkIfIsActive();
        SleepNode.Builder n = SleepNode.newBuilder().setRawSeconds(assignVariable(secondsToSleep));
        addNode("sleep", NodeCase.SLEEP, n.build());
    }

    public void sleepUntil(WfRunVariable timestamp) {
        checkIfIsActive();
        SleepNode.Builder n = SleepNode.newBuilder().setTimestamp(assignVariable(timestamp));
        addNode("sleep", NodeCase.SLEEP, n.build());
    }

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

    public void addTimeoutToExtEvt(NodeOutputImpl node, int timeoutSeconds) {
        checkIfIsActive();
        Node.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();
        if (n.getNodeCase() != NodeCase.EXTERNAL_EVENT) {
            throw new RuntimeException("Tried to set timeout on non-ext evt node!");
        }

        ExternalEventNode.Builder evt = n.getExternalEventBuilder();
        evt.setTimeoutSeconds(VariableAssignment.newBuilder()
                .setLiteralValue(
                        VariableValue.newBuilder().setInt(timeoutSeconds).setType(VariableType.INT)));

        n.setExternalEvent(evt);

        spec.putNodes(node.nodeName, n.build());
    }

    public void mutate(WfRunVariable lhsVar, VariableMutationType type, Object rhs) {
        checkIfIsActive();
        WfRunVariableImpl lhs = (WfRunVariableImpl) lhsVar;
        VariableMutation.Builder mutation =
                VariableMutation.newBuilder().setLhsName(lhs.name).setOperation(type);

        if (lhs.jsonPath != null) {
            mutation.setLhsJsonPath(lhs.jsonPath);
        }

        if (NodeOutputImpl.class.isAssignableFrom(rhs.getClass())) {
            NodeOutputImpl no = (NodeOutputImpl) rhs;
            if (!no.nodeName.equals(this.lastNodeName)) {
                log.debug("Mutating {} {} {}", no.nodeName, this.lastNodeName, name);

                throw new RuntimeException("Cannot use an old NodeOutput from node " + no.nodeName);
            }

            NodeOutputSource.Builder nodeOutputSource = NodeOutputSource.newBuilder();
            if (no.jsonPath != null) {
                nodeOutputSource.setJsonpath(no.jsonPath);
            }

            mutation.setNodeOutput(nodeOutputSource);
        } else if (WfRunVariableImpl.class.isAssignableFrom(rhs.getClass())) {
            WfRunVariableImpl var = (WfRunVariableImpl) rhs;
            VariableAssignment.Builder varBuilder = VariableAssignment.newBuilder();

            if (var.jsonPath != null) {
                varBuilder.setJsonPath(var.jsonPath);
            }
            varBuilder.setVariableName(var.name);

            mutation.setSourceVariable(varBuilder);
        } else {
            // At this point, we're going to treat it as a regular POJO, which means
            // likely a json obj.

            VariableValue rhsVal;
            try {
                rhsVal = LHLibUtil.objToVarVal(rhs);
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }

            mutation.setLiteralValue(rhsVal);
        }

        this.addMutationToCurrentNode(mutation.build());
    }

    public WaitForThreadsNodeOutput waitForThreads(SpawnedThread... threadsToWaitFor) {
        checkIfIsActive();
        WaitForThreadsNode.Builder waitNode = WaitForThreadsNode.newBuilder();

        for (int i = 0; i < threadsToWaitFor.length; i++) {
            SpawnedThreadImpl st = (SpawnedThreadImpl) threadsToWaitFor[i];
            waitNode.addThreads(ThreadToWaitFor.newBuilder().setThreadRunNumber(assignVariable(st.internalThreadVar)));
        }
        waitNode.setPolicy(WaitForThreadsPolicy.STOP_ON_FAILURE);
        String nodeName = addNode("threads", NodeCase.WAIT_FOR_THREADS, waitNode.build());

        return new WaitForThreadsNodeOutputImpl(nodeName, this, spec);
    }

    @Override
    public WaitForThreadsNodeOutput waitForThreads(SpawnedThreads threads) {
        checkIfIsActive();
        WaitForThreadsNode.Builder waitNode = WaitForThreadsNode.newBuilder();
        SpawnedThreadsImpl spawnedThreads = (SpawnedThreadsImpl) threads;
        waitNode.setThreadList(assignVariable(spawnedThreads.getInternalThreadVar()));
        waitNode.setPolicy(WaitForThreadsPolicy.STOP_ON_FAILURE);
        String nodeName = addNode("threads", NodeCase.WAIT_FOR_THREADS, waitNode.build());
        return new WaitForThreadsNodeOutputImpl(nodeName, this, spec);
    }

    public NodeOutputImpl waitForEvent(String externalEventDefName) {
        checkIfIsActive();
        ExternalEventNode waitNode = ExternalEventNode.newBuilder()
                .setExternalEventDefName(externalEventDefName)
                .build();

        parent.addExternalEventDefName(externalEventDefName);

        return new NodeOutputImpl(addNode(externalEventDefName, NodeCase.EXTERNAL_EVENT, waitNode), this);
    }

    public void complete() {
        checkIfIsActive();
        ExitNode exitNode = ExitNode.newBuilder().build();
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
                .setExternalEventDefName(interruptName)
                .setHandlerSpecName(threadName)
                .build());
    }

    public void handleException(NodeOutput nodeOutput, String exceptionName, ThreadFunc handler) {
        checkIfIsActive();
        NodeOutputImpl node = (NodeOutputImpl) nodeOutput;
        String threadName = "exn-handler-" + node.nodeName + "-" + exceptionName;
        threadName = parent.addSubThread(threadName, handler);
        FailureHandlerDef.Builder handlerDef = FailureHandlerDef.newBuilder().setHandlerSpecName(threadName);
        if (exceptionName != null) {
            handlerDef.setSpecificFailure(exceptionName);
        }

        // Add the failure handler to the most recent node
        Node.Builder lastNodeBuilder = spec.getNodesOrThrow(node.nodeName).toBuilder();

        lastNodeBuilder.addFailureHandlers(handlerDef);
        spec.putNodes(node.nodeName, lastNodeBuilder.build());
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
            throw new RuntimeException("Not possible to have null last node here");
        }

        Node.Builder feederNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        Edge.Builder edge = Edge.newBuilder().setSinkNodeName(nextNodeName);
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
        VariableAssignment.Builder builder = VariableAssignment.newBuilder();

        if (variable == null) {
            builder.setLiteralValue(VariableValue.newBuilder().setType(VariableType.NULL));
        } else if (variable.getClass().equals(WfRunVariableImpl.class)) {
            WfRunVariableImpl wrv = (WfRunVariableImpl) variable;
            if (wrv.jsonPath != null) {
                builder.setJsonPath(wrv.jsonPath);
            }
            builder.setVariableName(wrv.name);
        } else if (variable.getClass().equals(NodeOutputImpl.class)) {
            throw new RuntimeException(
                    "Error: Cannot use NodeOutput directly as input to task. First save to a WfRunVariable.");
        } else if (variable.getClass().equals(LHFormatStringImpl.class)) {
            LHFormatStringImpl format = (LHFormatStringImpl) variable;
            builder.setFormatString(FormatString.newBuilder()
                    .setFormat(assignVariable(format.getFormat()))
                    .addAllArgs(format.getArgs()));

        } else {
            try {
                VariableValue defVal = LHLibUtil.objToVarVal(variable);
                builder.setLiteralValue(defVal);
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        return builder.build();
    }

    private void checkIfIsActive() {
        if (!isActive) {
            throw new RuntimeException("Using a inactive thread");
        }
    }
}
