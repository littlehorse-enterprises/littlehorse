package io.littlehorse.jlib.wfsdk.internal;

import com.google.protobuf.Message;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.exception.LHSerdeError;
import io.littlehorse.jlib.common.exception.TaskSchemaMismatchError;
import io.littlehorse.jlib.common.proto.ComparatorPb;
import io.littlehorse.jlib.common.proto.EdgeConditionPb;
import io.littlehorse.jlib.common.proto.EdgePb;
import io.littlehorse.jlib.common.proto.EntrypointNodePb;
import io.littlehorse.jlib.common.proto.ExitNodePb;
import io.littlehorse.jlib.common.proto.ExternalEventNodePb;
import io.littlehorse.jlib.common.proto.FailureDefPb;
import io.littlehorse.jlib.common.proto.FailureHandlerDefPb;
import io.littlehorse.jlib.common.proto.InterruptDefPb;
import io.littlehorse.jlib.common.proto.NodePb;
import io.littlehorse.jlib.common.proto.NodePb.NodeCase;
import io.littlehorse.jlib.common.proto.NopNodePb;
import io.littlehorse.jlib.common.proto.SleepNodePb;
import io.littlehorse.jlib.common.proto.StartThreadNodePb;
import io.littlehorse.jlib.common.proto.TaskNodePb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.ThreadSpecPb;
import io.littlehorse.jlib.common.proto.UTActionTriggerPb;
import io.littlehorse.jlib.common.proto.UTActionTriggerPb.UTATaskPb;
import io.littlehorse.jlib.common.proto.UserTaskNodePb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb.FormatStringPb;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.jlib.common.proto.VariableMutationPb;
import io.littlehorse.jlib.common.proto.VariableMutationPb.NodeOutputSourcePb;
import io.littlehorse.jlib.common.proto.VariableMutationTypePb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.common.proto.VariableValuePb;
import io.littlehorse.jlib.common.proto.WaitForThreadNodePb;
import io.littlehorse.jlib.wfsdk.IfElseBody;
import io.littlehorse.jlib.wfsdk.NodeOutput;
import io.littlehorse.jlib.wfsdk.SpawnedThread;
import io.littlehorse.jlib.wfsdk.ThreadBuilder;
import io.littlehorse.jlib.wfsdk.ThreadFunc;
import io.littlehorse.jlib.wfsdk.UserTaskOutput;
import io.littlehorse.jlib.wfsdk.WfRunVariable;
import io.littlehorse.jlib.wfsdk.WorkflowCondition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadBuilderImpl implements ThreadBuilder {

    private Logger log = LoggerFactory.getLogger(ThreadBuilderImpl.class);

    private WorkflowImpl parent;
    private ThreadSpecPb.Builder spec;
    public String lastNodeName;
    public String name;
    private EdgeConditionPb lastNodeCondition;
    private boolean isActive;

    public ThreadBuilderImpl(String name, WorkflowImpl parent, ThreadFunc func) {
        this.parent = parent;
        this.spec = ThreadSpecPb.newBuilder();
        this.name = name;

        // For now, the creation of the entrypoint node is manual.
        NodePb entrypointNode = NodePb
            .newBuilder()
            .setEntrypoint(EntrypointNodePb.newBuilder())
            .build();

        String entrypointNodeName = "0-entrypoint-ENTRYPOINT";
        lastNodeName = entrypointNodeName;
        spec.putNodes(entrypointNodeName, entrypointNode);
        isActive = true;
        // Call the function and do its work
        func.threadFunction(this);

        // Now add an exit node.
        addNode("exit", NodeCase.EXIT, ExitNodePb.newBuilder().build());
        isActive = false;
    }

    public ThreadSpecPb.Builder getSpec() {
        return spec;
    }

    public UserTaskOutputImpl assignUserTaskToUser(
        String userTaskDefName,
        String userId
    ) {
        return assignUserTaskHelper(userTaskDefName, userId, null);
    }

    public UserTaskOutputImpl assignUserTaskToUser(
        String userTaskDefName,
        WfRunVariable userId
    ) {
        return assignUserTaskHelper(userTaskDefName, userId, null);
    }

    public UserTaskOutputImpl assignUserTaskToUserGroup(
        String userTaskDefName,
        String userGroup
    ) {
        return assignUserTaskHelper(userTaskDefName, null, userGroup);
    }

    public UserTaskOutputImpl assignUserTaskToUserGroup(
        String userTaskDefName,
        WfRunVariable userGroup
    ) {
        return assignUserTaskHelper(userTaskDefName, null, userGroup);
    }

    private UserTaskOutputImpl assignUserTaskHelper(
        String userTaskDefName,
        Object userId,
        Object userGroups
    ) {
        checkIfIsActive();
        // guaranteed that exatly one of userId or userGroup is not null
        UserTaskNodePb.Builder utNode = UserTaskNodePb
            .newBuilder()
            .setUserTaskDefName(userTaskDefName);

        if (userId != null) {
            VariableAssignmentPb userIdAssn = assignVariable(userId);
            utNode.setUserId(userIdAssn);
        } else {
            // guaranteed userGroup != null
            VariableAssignmentPb userIdAssn = assignVariable(userGroups);
            utNode.setUserGroup(userIdAssn);
        }

        // TODO LH-313: Return a special subclass of NodeOutputImpl that
        // allows for adding trigger actions

        String nodeName = addNode(
            userTaskDefName,
            NodeCase.USER_TASK,
            utNode.build()
        );
        return new UserTaskOutputImpl(nodeName, this);
    }

    public void scheduleTaskAfter(
        UserTaskOutput ut,
        WfRunVariable delaySeconds,
        String taskDefName,
        Object... args
    ) {
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, args);
    }

    public void scheduleTaskAfter(
        UserTaskOutput ut,
        int delaySeconds,
        String taskDefName,
        Object... args
    ) {
        scheduleTaskAfterHelper(ut, delaySeconds, taskDefName, args);
    }

    public void scheduleTaskAfterHelper(
        UserTaskOutput ut,
        Object delaySeconds,
        String taskDefName,
        Object... args
    ) {
        checkIfIsActive();
        VariableAssignmentPb assn = assignVariable(delaySeconds);
        TaskNodePb taskNode = createTaskNode(taskDefName, args);
        UTATaskPb utaTask = UTATaskPb.newBuilder().setTask(taskNode).build();

        UserTaskOutputImpl utImpl = (UserTaskOutputImpl) ut;
        if (!lastNodeName.equals(utImpl.nodeName)) {
            throw new RuntimeException("Tried to edit a stale User Task node!");
        }

        NodePb.Builder curNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        curNode
            .getUserTaskBuilder()
            .addActions(
                UTActionTriggerPb.newBuilder().setTask(utaTask).setDelaySeconds(assn)
            );
        spec.putNodes(lastNodeName, curNode.build());
        // TODO LH-334: return a modified child class of NodeOutput which lets
        // us mutate variables
    }

    public LHFormatStringImpl format(String format, WfRunVariable... args) {
        return new LHFormatStringImpl(this, format, args);
    }

    public NodeOutputImpl execute(String taskName, Object... args) {
        checkIfIsActive();
        TaskNodePb taskNode = createTaskNode(taskName, args);
        String nodeName = addNode(taskName, NodeCase.TASK, taskNode);
        return new NodeOutputImpl(nodeName, this);
    }

    private TaskNodePb createTaskNode(String taskName, Object... args) {
        TaskNodePb.Builder taskNode = TaskNodePb
            .newBuilder()
            .setTaskDefName(taskName);
        parent.addTaskDefName(taskName);

        for (Object var : args) {
            taskNode.addVariables(assignVariable(var));
        }
        return taskNode.build();
    }

    public void checkArgsVsTaskDef(
        List<VariableDefPb> taskDefInputVars,
        String taskDefName,
        Object... args
    ) throws TaskSchemaMismatchError {
        if (args.length != taskDefInputVars.size()) {
            throw new TaskSchemaMismatchError("Mismatched number of arguments!");
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            VariableTypePb argType;

            if (WfRunVariableImpl.class.isAssignableFrom(arg.getClass())) {
                WfRunVariableImpl wfVar = ((WfRunVariableImpl) arg);

                if (
                    (
                        wfVar.type == VariableTypePb.JSON_ARR ||
                        wfVar.type == VariableTypePb.JSON_OBJ
                    ) &&
                    wfVar.jsonPath != null
                ) {
                    log.info(
                        "There is a jsonpath, so not checking value because Json schema isn't yet implemented"
                    );
                    continue;
                }
                argType = wfVar.type;
            } else {
                argType = LHLibUtil.javaClassToLHVarType(arg.getClass());
            }

            if (!argType.equals(taskDefInputVars.get(i).getType())) {
                throw new TaskSchemaMismatchError(
                    "Mismatch var type for param " +
                    i +
                    "on taskdef " +
                    taskDefName +
                    ": " +
                    argType +
                    " not compatible with " +
                    taskDefInputVars.get(i).getType()
                );
            }
        }
    }

    public void addMutationToCurrentNode(VariableMutationPb mutation) {
        checkIfIsActive();
        NodePb.Builder builder = spec.getNodesOrThrow(lastNodeName).toBuilder();
        builder.addVariableMutations(mutation);
        spec.putNodes(lastNodeName, builder.build());
    }

    public WfRunVariableImpl addVariable(String name, Object typeOrDefaultVal) {
        checkIfIsActive();
        VariableDefPb.Builder varDefBuilder = VariableDefPb.newBuilder();

        if (typeOrDefaultVal instanceof VariableTypePb) {
            varDefBuilder.setType((VariableTypePb) typeOrDefaultVal);
        } else {
            try {
                VariableValuePb defaultVal = LHLibUtil.objToVarVal(typeOrDefaultVal);
                varDefBuilder.setType(defaultVal.getType());
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        varDefBuilder.setName(name);
        spec.addVariableDefs(varDefBuilder);
        return new WfRunVariableImpl(name, varDefBuilder.getType(), this);
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

        NodePb.Builder treeRoot = spec.getNodesOrThrow(treeRootNodeName).toBuilder();
        treeRoot.addOutgoingEdges(
            EdgePb
                .newBuilder()
                .setSinkNodeName(lastNodeName)
                .setCondition(cond.getReverse())
                .build()
        );
        spec.putNodes(treeRootNodeName, treeRoot.build());
    }

    private void addNopNode() {
        checkIfIsActive();
        addNode("nop", NodeCase.NOP, NopNodePb.newBuilder().build());
    }

    public void doIfElse(
        WorkflowCondition condition,
        IfElseBody ifBody,
        IfElseBody elseBody
    ) {
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
            throw new RuntimeException(
                "Not possible to have lastNodeCondition after internal call to " +
                "elseBody.body(this); please contact maintainers. This is a bug."
            );
        }

        NodePb.Builder lastNodeFromElseBlock = spec
            .getNodesOrThrow(lastNodeName)
            .toBuilder();
        lastNodeFromElseBlock.addOutgoingEdges(
            EdgePb
                .newBuilder()
                .setSinkNodeName(joinerNodeName)
                // No condition necessary since we need to just go straight to
                // the joiner node
                .build()
        );

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
        NodePb.Builder treeRoot = spec.getNodesOrThrow(treeRootNodeName).toBuilder();

        lastNodeCondition = cond.getSpec();
        // execute the tasks
        whileBody.threadFunction(this);

        // close off the tree
        addNopNode();
        String treeLastNodeName = lastNodeName;
        NodePb.Builder treeLast = spec.getNodesOrThrow(treeLastNodeName).toBuilder();

        // Now add the sideways path from root directly to last
        treeRoot.addOutgoingEdges(
            EdgePb
                .newBuilder()
                .setSinkNodeName(treeLastNodeName)
                .setCondition(cond.getReverse())
                .build()
        );

        // Now add the sideways path from last directly to root
        treeLast.addOutgoingEdges(
            EdgePb
                .newBuilder()
                .setSinkNodeName(treeRootNodeName)
                .setCondition(cond.getSpec())
                .build()
        );

        spec.putNodes(treeLastNodeName, treeLast.build());
    }

    public void sleepSeconds(Object secondsToSleep) {
        checkIfIsActive();
        SleepNodePb.Builder n = SleepNodePb
            .newBuilder()
            .setRawSeconds(assignVariable(secondsToSleep));
        addNode("sleep", NodeCase.SLEEP, n.build());
    }

    public SpawnedThreadImpl spawnThread(
        ThreadFunc threadFunc,
        String threadName,
        Map<String, Object> inputVars
    ) {
        checkIfIsActive();
        if (inputVars == null) {
            inputVars = new HashMap<>();
        }
        threadName = parent.addSubThread(threadName, threadFunc);

        Map<String, VariableAssignmentPb> varAssigns = new HashMap<>();
        for (Map.Entry<String, Object> var : inputVars.entrySet()) {
            varAssigns.put(var.getKey(), assignVariable(var.getValue()));
        }

        StartThreadNodePb startThread = StartThreadNodePb
            .newBuilder()
            .setThreadSpecName(threadName)
            .putAllVariables(varAssigns)
            .build();

        String nodeName = addNode(threadName, NodeCase.START_THREAD, startThread);
        WfRunVariableImpl internalStartedThreadVar = addVariable(
            nodeName,
            VariableTypePb.INT
        );

        // The output of a StartThreadNode is just an integer containing the name
        // of the thread.
        mutate(
            internalStartedThreadVar,
            VariableMutationTypePb.ASSIGN,
            new NodeOutputImpl(nodeName, this)
        );

        return new SpawnedThreadImpl(this, threadName, internalStartedThreadVar);
    }

    public void addTimeoutToExtEvt(NodeOutputImpl node, int timeoutSeconds) {
        checkIfIsActive();
        NodePb.Builder n = spec.getNodesOrThrow(node.nodeName).toBuilder();
        if (n.getNodeCase() != NodeCase.EXTERNAL_EVENT) {
            throw new RuntimeException("Tried to set timeout on non-ext evt node!");
        }

        ExternalEventNodePb.Builder evt = n.getExternalEventBuilder();
        evt.setTimeoutSeconds(
            VariableAssignmentPb
                .newBuilder()
                .setLiteralValue(
                    VariableValuePb
                        .newBuilder()
                        .setInt(timeoutSeconds)
                        .setType(VariableTypePb.INT)
                )
        );

        n.setExternalEvent(evt);

        spec.putNodes(node.nodeName, n.build());
    }

    public void mutate(
        WfRunVariable lhsVar,
        VariableMutationTypePb type,
        Object rhs
    ) {
        checkIfIsActive();
        WfRunVariableImpl lhs = (WfRunVariableImpl) lhsVar;
        VariableMutationPb.Builder mutation = VariableMutationPb
            .newBuilder()
            .setLhsName(lhs.name)
            .setOperation(type);

        if (lhs.jsonPath != null) {
            mutation.setLhsJsonPath(lhs.jsonPath);
        }

        if (NodeOutputImpl.class.isAssignableFrom(rhs.getClass())) {
            NodeOutputImpl no = (NodeOutputImpl) rhs;
            if (!no.nodeName.equals(this.lastNodeName)) {
                System.out.println(no.nodeName);
                System.out.println(this.lastNodeName);
                System.out.println(name);

                throw new RuntimeException(
                    "Cannot use an old NodeOutput from node " + no.nodeName
                );
            }

            NodeOutputSourcePb.Builder nodeOutputSource = NodeOutputSourcePb.newBuilder();
            if (no.jsonPath != null) {
                nodeOutputSource.setJsonpath(no.jsonPath);
            }

            mutation.setNodeOutput(nodeOutputSource);
        } else if (WfRunVariableImpl.class.isAssignableFrom(rhs.getClass())) {
            WfRunVariableImpl var = (WfRunVariableImpl) rhs;
            VariableAssignmentPb.Builder varBuilder = VariableAssignmentPb.newBuilder();

            if (var.jsonPath != null) {
                varBuilder.setJsonPath(var.jsonPath);
            }
            varBuilder.setVariableName(var.name);

            mutation.setSourceVariable(varBuilder);
        } else {
            // At this point, we're going to treat it as a regular POJO, which means
            // likely a json obj.

            VariableValuePb rhsVal;
            try {
                rhsVal = LHLibUtil.objToVarVal(rhs);
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }

            mutation.setLiteralValue(rhsVal);
        }

        this.addMutationToCurrentNode(mutation.build());
    }

    public NodeOutputImpl waitForThread(SpawnedThread threadToWaitFor) {
        checkIfIsActive();
        SpawnedThreadImpl threadToWait = (SpawnedThreadImpl) threadToWaitFor;
        WaitForThreadNodePb waitNode = WaitForThreadNodePb
            .newBuilder()
            .setThreadRunNumber(assignVariable(threadToWait.internalThreadVar))
            .build();

        String nodeName = addNode(
            threadToWait.childThreadName,
            NodeCase.WAIT_FOR_THREAD,
            waitNode
        );

        return new NodeOutputImpl(nodeName, this);
    }

    public NodeOutputImpl waitForEvent(String externalEventDefName) {
        checkIfIsActive();
        ExternalEventNodePb waitNode = ExternalEventNodePb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName)
            .build();

        parent.addExternalEventDefName(externalEventDefName);

        return new NodeOutputImpl(
            addNode(externalEventDefName, NodeCase.EXTERNAL_EVENT, waitNode),
            this
        );
    }

    public void fail(String failureName, String message) {
        fail(null, failureName, message);
    }

    public void fail(Object output, String failureName, String message) {
        checkIfIsActive();
        FailureDefPb.Builder failureBuilder = FailureDefPb.newBuilder();
        if (output != null) failureBuilder.setContent(assignVariable(output));
        if (message != null) failureBuilder.setMessage(message);
        failureBuilder.setFailureName(failureName);
        failureBuilder.setFailureCode(TaskResultCodePb.FAILED);

        ExitNodePb exitNode = ExitNodePb
            .newBuilder()
            .setFailureDef(failureBuilder)
            .build();

        addNode(failureName, NodeCase.EXIT, exitNode);
    }

    public void registerInterruptHandler(String interruptName, ThreadFunc handler) {
        checkIfIsActive();
        String threadName = "interrupt-" + interruptName;
        threadName = parent.addSubThread(threadName, handler);
        parent.addExternalEventDefName(interruptName);

        spec.addInterruptDefs(
            InterruptDefPb
                .newBuilder()
                .setExternalEventDefName(interruptName)
                .setHandlerSpecName(threadName)
                .build()
        );
    }

    public void handleException(
        NodeOutput nodeOutput,
        String exceptionName,
        ThreadFunc handler
    ) {
        checkIfIsActive();
        NodeOutputImpl node = (NodeOutputImpl) nodeOutput;
        String threadName = "exn-handler-" + node.nodeName + "-" + exceptionName;
        threadName = parent.addSubThread(threadName, handler);
        FailureHandlerDefPb.Builder handlerDef = FailureHandlerDefPb
            .newBuilder()
            .setHandlerSpecName(threadName);
        if (exceptionName != null) {
            handlerDef.setSpecificFailure(exceptionName);
        }

        // Add the failure handler to the most recent node
        NodePb.Builder lastNodeBuilder = spec
            .getNodesOrThrow(node.nodeName)
            .toBuilder();

        lastNodeBuilder.addFailureHandlers(handlerDef);
        spec.putNodes(node.nodeName, lastNodeBuilder.build());
    }

    public WorkflowConditionImpl condition(
        Object lhs,
        ComparatorPb comparator,
        Object rhs
    ) {
        EdgeConditionPb.Builder edge = EdgeConditionPb
            .newBuilder()
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

        NodePb.Builder feederNode = spec.getNodesOrThrow(lastNodeName).toBuilder();
        EdgePb.Builder edge = EdgePb.newBuilder().setSinkNodeName(nextNodeName);
        if (lastNodeCondition != null) {
            edge.setCondition(lastNodeCondition);
            lastNodeCondition = null;
        }
        feederNode.addOutgoingEdges(edge);
        spec.putNodes(lastNodeName, feederNode.build());

        NodePb.Builder node = NodePb.newBuilder();
        switch (type) {
            case TASK:
                node.setTask((TaskNodePb) subNode);
                break;
            case ENTRYPOINT:
                node.setEntrypoint((EntrypointNodePb) subNode);
                break;
            case EXIT:
                node.setExit((ExitNodePb) subNode);
                break;
            case EXTERNAL_EVENT:
                node.setExternalEvent((ExternalEventNodePb) subNode);
                break;
            case SLEEP:
                node.setSleep((SleepNodePb) subNode);
                break;
            case START_THREAD:
                node.setStartThread((StartThreadNodePb) subNode);
                break;
            case WAIT_FOR_THREAD:
                node.setWaitForThread((WaitForThreadNodePb) subNode);
                break;
            case NOP:
                node.setNop((NopNodePb) subNode);
                break;
            case USER_TASK:
                node.setUserTask((UserTaskNodePb) subNode);
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

    public VariableAssignmentPb assignVariable(Object variable) {
        checkIfIsActive();
        VariableAssignmentPb.Builder builder = VariableAssignmentPb.newBuilder();

        if (variable.getClass().equals(WfRunVariableImpl.class)) {
            WfRunVariableImpl wrv = (WfRunVariableImpl) variable;
            if (wrv.jsonPath != null) {
                builder.setJsonPath(wrv.jsonPath);
            }
            builder.setVariableName(wrv.name);
        } else if (variable.getClass().equals(NodeOutputImpl.class)) {
            throw new RuntimeException(
                "Error: Cannot use NodeOutput directly as input to" +
                " task. First save to a WfRunVariable."
            );
        } else if (variable.getClass().equals(LHFormatStringImpl.class)) {
            LHFormatStringImpl format = (LHFormatStringImpl) variable;
            builder.setFormatString(
                FormatStringPb
                    .newBuilder()
                    .setFormat(assignVariable(format.getFormat()))
                    .addAllArgs(format.getArgs())
            );
        } else {
            try {
                VariableValuePb defVal = LHLibUtil.objToVarVal(variable);
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
