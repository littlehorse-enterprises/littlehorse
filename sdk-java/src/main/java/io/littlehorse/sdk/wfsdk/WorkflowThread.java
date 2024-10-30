package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** This interface is what is used to define the logic of a ThreaSpec in a ThreadFunc. */
public interface WorkflowThread {
    /**
     * This is the reserved Variable Name that can be used as a WfRunVariable in an Interrupt
     * Handler or Exception Handler thread.
     */
    public static final String HANDLER_INPUT_VAR = "INPUT";

    /**
     * Overrides the retention policy for all ThreadRun's of this ThreadSpec in the
     * WfRun.
     *
     *
     * @param policy is the Thread Retention Policy.
     */
    public void withRetentionPolicy(ThreadRetentionPolicy policy);

    /**
     * Adds a TASK node to the ThreadSpec.
     *
     * @param taskName The name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     * @return A NodeOutput for that TASK node.
     */
    TaskNodeOutput execute(String taskName, Serializable... args);

    /**
     * Adds a TASK node to the ThreadSpec.
     *
     * @param taskName a WfRunVariable containing the name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     * @return A NodeOutput for that TASK node.
     */
    TaskNodeOutput execute(WfRunVariable taskName, Serializable... args);

    /**
     * Adds a TASK node to the ThreadSpec.
     *
     * @param taskName an LHFormatString containing the name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     * @return A NodeOutput for that TASK node.
     */
    TaskNodeOutput execute(LHFormatString taskName, Serializable... args);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to. Can be either String or WfRunVariable.
     * Can be null if userGroup not null.
     * @param userGroup is the user group to assign it to. Can be either String or
     * WfRunvariable. Can be null if userId not null.
     * @return a NodeOutput.
     */
    UserTaskOutput assignUserTask(String userTaskDefName, Object userId, Object userGroup);

    /**
     * Schedule Reassignment of a UserTask to a userGroup upon reaching the Deadline. This method is
     * used to schedule the reassignment of a UserTask to a userGroup when the specified UserTask
     * user assignment reaches its deadline in seconds.
     *
     * @param userTaskOutput that is currently assigned to a UserGroup.
     * @param deadlineSeconds Time in seconds after which the UserTask will be automatically
     *     reassigned to the UserGroup. Can be either String or WfRunVariable.
     */
    void releaseToGroupOnDeadline(UserTaskOutput userTaskOutput, Object deadlineSeconds);

    /**
     * Schedules the reassignment of a User Task to a specified userId and/or userGroup after
     * a specified expiration.
     *
     * @param userTask is the userTask to reschedule.
     * @param userId is the userId to which the task should be assigned. Must be either WfRunVariable
     * or String. Can be null if userGroup not null.
     * @param userGroup is the userGroup to which the task should be reassigned. Must be either
     * WfRunVariable or String. Can be null if userId not null.
     * @param deadlineSeconds is the expiration time after which the UserTask should be reassigned.
     * Can be either WfRunVariable or String.
     */
    void reassignUserTask(UserTaskOutput userTask, Object userId, Object userGroup, Object deadlineSeconds);

    /**
     * Creates a formatted string using WfRunVariables as arguments.
     *
     * Example:
     * format("Hello there, {0}, today is {1}", name, dayOfWeek);
     *
     * @param format is the format string.
     * @param args are the format args.
     * @return an LHFormatString object which can be used as a variable assignment in a WfSpec.
     */
    LHFormatString format(String format, WfRunVariable... args);

    WfRunVariable declareInt(String name);

    WfRunVariable declareInt(String name, int defaultVal);

    WfRunVariable declareStr(String name);

    WfRunVariable declareStr(String name, String defaultVal);

    WfRunVariable declareDouble(String name);

    WfRunVariable declareDouble(String name, double defaultVal);

    WfRunVariable declareBytes(String name);

    WfRunVariable declareBytes(String name, byte[] defaultVal);

    WfRunVariable declareJsonArr(String name);

    WfRunVariable declareJsonArr(String name, List<Object> defaultVal);

    WfRunVariable declareJsonObj(String name);

    WfRunVariable declareJsonObj(String name, Map<String, Object> defaultVal);

    WfRunVariable declareBool(String name);

    WfRunVariable declareBool(String name, boolean defaultVal);

    /**
     * Defines a Variable in the `ThreadSpec` and returns a handle to it.
     *
     * @param name the name of the variable.
     * @param typeOrDefaultVal is either the type of the variable, from the `VariableType` enum,
     *     or an object representing the default value of the Variable. If an object (or primitive)
     *     is provided, the Task Worker Library casts the provided value to a VariableValue and sets
     *     that as the default.
     * @return a handle to the created WfRunVariable.
     */
    WfRunVariable addVariable(String name, Object typeOrDefaultVal);

    /**
     * Conditionally executes some workflow code; equivalent to an if() statement in programming.
     *
     * @param condition is the WorkflowCondition to be satisfied.
     * @param doIf is the block of ThreadSpec code to be executed if the provided WorkflowCondition
     *     is satisfied.
     */
    void doIf(WorkflowCondition condition, IfElseBody doIf);

    /**
     * Conditionally executes one of two workflow code branches; equivalent to an if/else statement
     * in programming.
     *
     * @param condition is the WorkflowCondition to be satisfied.
     * @param doIf is the block of ThreadSpec code to be executed if the provided WorkflowCondition
     *     is satisfied.
     * @param doElse is the block of ThreadSpec code to be executed if the provided
     *     WorkflowCondition is NOT satisfied.
     */
    void doIfElse(WorkflowCondition condition, IfElseBody doIf, IfElseBody doElse);

    /**
     * Adds a Reminder Task to a User Task Node.
     *
     * @param userTask is a reference to the UserTaskNode that we schedule the action after.
     * @param delaySeconds is the delay time after which the Task should be executed.
     * @param taskDefName The name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     */
    void scheduleReminderTask(UserTaskOutput userTask, int delaySeconds, String taskDefName, Serializable... args);

    /**
     * Adds a Reminder Task to a User Task Node.
     *
     * @param userTask is a reference to the UserTaskNode that we schedule the action after.
     * @param delaySeconds is the delay time after which the Task should be executed.
     * @param taskDefName The name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     */
    void scheduleReminderTask(
            UserTaskOutput userTask, WfRunVariable delaySeconds, String taskDefName, Serializable... args);

    /**
     * Adds a task reminder once a user is assigned to UserTask.
     *
     * @param userTask is a reference to the UserTaskNode that we schedule the action after.
     * @param delaySeconds is the delay time after which the Task should be executed.
     * @param taskDefName The name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     */
    void scheduleReminderTaskOnAssignment(
            UserTaskOutput userTask, WfRunVariable delaySeconds, String taskDefName, Serializable... args);

    /**
     * Cancels a User Task Run if it exceeds a specified deadline.
     * @param userTask is a reference to the UserTaskNode that will be canceled after the deadline
     * @param delaySeconds is the delay time after which the User Task Run should be canceled
     */
    void cancelUserTaskRunAfter(UserTaskOutput userTask, Serializable delaySeconds);

    /**
     * Cancels a User Task Run if it exceeds a specified deadline after it is assigned
     * @param userTask is a reference to the UserTaskNode that will be canceled after the deadline
     * @param delaySeconds is the delay time after which the User Task Run should be canceled
     */
    void cancelUserTaskRunAfterAssignment(UserTaskOutput userTask, Serializable delaySeconds);

    /**
     * Adds a task reminder once a user is assigned to the UserTask.
     *
     * @param userTask is a reference to the UserTaskNode that we schedule the action after.
     * @param delaySeconds is the delay time after which the Task should be executed.
     * @param taskDefName The name of the TaskDef to execute.
     * @param args The input parameters to pass into the Task Run. If the type of an arg is a
     *     `WfRunVariable`, then that WfRunVariable is passed in as the argument; otherwise, the
     *     library will attempt to cast the provided argument to a LittleHorse VariableValue and
     *     pass that literal value in.
     */
    void scheduleReminderTaskOnAssignment(
            UserTaskOutput userTask, int delaySeconds, String taskDefName, Serializable... args);

    /**
     * Conditionally executes some workflow code; equivalent to an while() statement in programming.
     *
     * @param condition is the WorkflowCondition to be satisfied.
     * @param whileBody is the block of ThreadFunc code to be executed while the provided
     *     WorkflowCondition is satisfied.
     */
    void doWhile(WorkflowCondition condition, ThreadFunc whileBody);

    /**
     * Adds a SPAWN_THREAD node to the ThreadSpec, which spawns a Child ThreadRun whose ThreadSpec
     * is determined by the provided ThreadFunc.
     *
     * @param threadFunc is a ThreadFunc (can be a lambda function) that defines the logic for the
     *     child ThreadRun to execute.
     * @param threadName is the name of the child thread spec.
     * @param inputVars is a Map of all of the input variables to set for the child ThreadRun. If
     *     you don't need to set any input variables, leave this null.
     * @return a handle to the resulting SpawnedThread, which can be used in
     *     ThreadBuilder::waitForThread()
     */
    SpawnedThread spawnThread(ThreadFunc threadFunc, String threadName, Map<String, Object> inputVars);

    /**
     * Adds a WAIT_FOR_THREAD node which waits for a Child ThreadRun to complete.
     *
     * @param threadsToWaitFor set of SpawnedThread objects returned one or more calls to
     *     spawnThread.
     * @return a NodeOutput that can be used for timeouts or exception handling.
     */
    WaitForThreadsNodeOutput waitForThreads(SpawnedThreads threadsToWaitFor);

    /**
     * Adds an EXTERNAL_EVENT node which blocks until an 'ExternalEvent' of the specified type
     * arrives.
     *
     * @param externalEventDefName is the type of ExternalEvent to wait for.
     * @return a NodeOutput for this event.
     */
    NodeOutput waitForEvent(String externalEventDefName);

    /**
     * Adds a WAIT_FOR_CONDITION node which blocks until the provided boolean condition
     * evaluates to true.
     * @param condition is the condition to wait for.
     * @return a handle to the NodeOutput, which may only be used for error handling since
     * the output of this node is empty.
     */
    WaitForConditionNodeOutput waitForCondition(WorkflowCondition condition);

    /**
     * Adds an EXIT node with a Failure defined. This causes a ThreadRun to fail, and the resulting
     * Failure has the specified value, name, and human-readable message.
     *
     * @param output is a literal value (cast to VariableValue by the Library) or a WfRunVariable.
     *     The assigned value is the payload of the resulting Failure, which can be accessed by any
     *     Failure Handler ThreadRuns.
     * @param failureName is the name of the failure to throw.
     * @param message is a human-readable message.
     */
    void fail(Object output, String failureName, String message);

    /**
     * Adds an EXIT node with no Failure defined. This causes the ThreadRun to complete gracefully.
     * It is equivalent to putting a call to `return;` early in your function.
     */
    void complete();

    /**
     * Adds an EXIT node with a Failure defined. This causes a ThreadRun to fail, and the resulting
     * Failure has the specified name and human-readable message.
     *
     * @param failureName is the name of the failure to throw.
     * @param message is a human-readable message.
     */
    void fail(String failureName, String message);

    /**
     * Registers an Interrupt Handler, such that when an ExternalEvent arrives with the specified
     * type, this ThreadRun is interrupted.
     *
     * @param interruptName The name of the ExternalEventDef to listen for.
     * @param handler A Thread Function defining a ThreadSpec to use to handle the Interrupt.
     */
    void registerInterruptHandler(String interruptName, ThreadFunc handler);

    /**
     * Adds a SLEEP node which makes the ThreadRun sleep for a specified number of seconds.
     *
     * @param seconds is either an integer representing the number of seconds to sleep for, or it is
     *     a WfRunVariable which evaluates to a VariableTypePb.INT specifying the number of seconds
     *     to sleep for.
     */
    void sleepSeconds(Object seconds);

    /**
     * Adds a SLEEP node which makes the ThreadRun sleep until a specified timestamp, provided as an
     * INT WfRunVariable (note that INT in LH is a 64-bit integer).
     *
     * @param timestamp a WfRunVariable which evaluates to a VariableTypePb.INT specifying the epoch
     *     timestamp (in milliseconds) to wait for.
     */
    void sleepUntil(WfRunVariable timestamp);

    /**
     * Attaches an Exception Handler to the specified NodeOutput, enabling it to handle specific
     * types of exceptions as defined by the 'exceptionName' parameter. If 'exceptionName' is null,
     * the handler will catch all exceptions.
     *
     * @param node         The NodeOutput instance to which the Exception Handler will be attached.
     * @param exceptionName The name of the specific exception to handle. If set to null, the handler
     *                     will catch all exceptions.
     * @param handler      A ThreadFunction defining a ThreadSpec that specifies how to handle the
     *                     exception.
     */
    void handleException(NodeOutput node, String exceptionName, ThreadFunc handler);

    /**
     * Attaches an Exception Handler to the specified NodeOutput, enabling it to handle any
     * types of exceptions.
     *
     * @param node         The NodeOutput instance to which the Exception Handler will be attached.
     * @param handler      A ThreadFunction defining a ThreadSpec that specifies how to handle the
     *                     exception.
     */
    void handleException(NodeOutput node, ThreadFunc handler);

    /**
     * Attaches an Error Handler to the specified NodeOutput, allowing it to manage specific types of errors
     * as defined by the 'error' parameter. If 'error' is set to null, the handler will catch all errors.
     *
     * @param node    The NodeOutput instance to which the Error Handler will be attached.
     * @param error   The type of error that the handler will manage.
     * @param handler A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
     */
    void handleError(NodeOutput node, LHErrorType error, ThreadFunc handler);

    /**
     * Attaches an Error Handler to the specified NodeOutput, allowing it to manage any types of errors.
     *
     * @param node    The NodeOutput instance to which the Error Handler will be attached.
     * @param handler A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
     */
    void handleError(NodeOutput node, ThreadFunc handler);

    /**
     * Attaches an Failure Handler to the specified NodeOutput, allowing it to manage any types of errors or exceptions.
     *
     * @param node    The NodeOutput instance to which the Error Handler will be attached.
     * @param handler A ThreadFunction defining a ThreadSpec that specifies how to handle the error.
     */
    void handleAnyFailure(NodeOutput node, ThreadFunc handler);

    /**
     * Returns a WorkflowCondition that can be used in `WorkflowThread::doIf()` or
     * `WorkflowThread::doIfElse()`.
     *
     * @param lhs is either a literal value (which the Library casts to a Variable Value) or a
     *     `WfRunVariable` representing the LHS of the expression.
     * @param comparator is a Comparator defining the comparator, for example,
     *     `ComparatorTypePb.EQUALS`.
     * @param rhs is either a literal value (which the Library casts to a Variable Value) or a
     *     `WfRunVariable` representing the RHS of the expression.
     * @return a WorkflowCondition.
     */
    WorkflowCondition condition(Object lhs, Comparator comparator, Object rhs);

    /**
     * Adds a VariableMutation to the last Node
     *
     * @param lhs is a handle to the WfRunVariable to mutate.
     * @param type is the mutation type to use, for example, `VariableMutationType.ASSIGN`.
     * @param rhs is either a literal value (which the Library casts to a Variable Value), a
     *     `WfRunVariable` which determines the right hand side of the expression, or a `NodeOutput`
     *     (which allows you to use the output of a Node Run to mutate variables).
     */
    void mutate(WfRunVariable lhs, VariableMutationType type, Object rhs);

    /**
     * EXPERIMENTAL: Makes the active ThreadSpec throw a WorkflowEvent with a specific WorkflowEventDef
     * and provided content.
     * @param workflowEventDefName is the name of the WorkflowEvent to throw.
     * @param content is the content of the WorkflowEvent that is thrown.
     */
    void throwEvent(String workflowEventDefName, Serializable content);

    /**
     * Given a WfRunVariable of type JSON_ARR, this function iterates over each object in that list
     * and creates a Child ThreadRun for each item. The list item is provided as an input variable
     * to the Child ThreadRun with the name `INPUT`.
     * @param arrVar is a WfRunVariable of type JSON_ARR that we iterate over.
     * @param threadName is the name to assign to the created ThreadSpec.
     * @param threadFunc is the function that defnes the ThreadSpec.
     * @return a SpawnedThreads handle which we can use to wait for all child threads.
     */
    SpawnedThreads spawnThreadForEach(WfRunVariable arrVar, String threadName, ThreadFunc threadFunc);

    /**
     * Given a WfRunVariable of type JSON_ARR, this function iterates over each object in that list
     * and creates a Child ThreadRun for each item. The list item is provided as an input variable
     * to the Child ThreadRun with the name `INPUT`.
     * @param arrVar is a WfRunVariable of type JSON_ARR that we iterate over.
     * @param threadName is the name to assign to the created ThreadSpec.
     * @param threadFunc is the function that defnes the ThreadSpec.
     * @param inputVars is a map of input variables to pass to each child ThreadRun in addition
     *   to the list item.
     * @return a SpawnedThreads handle which we can use to wait for all child threads.
     */
    SpawnedThreads spawnThreadForEach(
            WfRunVariable arrVar, String threadName, ThreadFunc threadFunc, Map<String, Object> inputVars);
}
