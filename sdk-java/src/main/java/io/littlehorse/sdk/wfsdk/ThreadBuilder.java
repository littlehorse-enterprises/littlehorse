package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import java.util.Map;

/** This interface is what is used to define the logic of a ThreaSpec in a ThreadFunc. */
public interface ThreadBuilder {
    /**
     * This is the reserved Variable Name that can be used as a WfRunVariable in an Interrupt
     * Handler or Exception Handler thread.
     */
    public static final String HANDLER_INPUT_VAR = "INPUT";

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
    public NodeOutput execute(String taskName, Object... args);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to.
     * @return a NodeOutput.
     */
    UserTaskOutput assignTaskToUser(String userTaskDefName, String userId);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to.
     * @param userGroup is the User's group
     * @return a NodeOutput.
     */
    UserTaskOutput assignTaskToUser(String userTaskDefName, String userId, String userGroup);

    /**
     * Schedule Reassignment of a UserTask to a userGroup upon reaching the Deadline. This method is
     * used to schedule the reassignment of a UserTask to a userGroup when the specified UserTask
     * user assignment reaches its deadline in seconds.
     *
     * @param userTaskOutput that is currently assigned to a UserGroup.
     * @param deadlineSeconds Time in seconds after which the UserTask will be automatically
     *     reassigned to the UserGroup.
     */
    void reassignToGroupOnDeadline(UserTaskOutput userTaskOutput, int deadlineSeconds);

    /**
     * Schedule Reassignment of a UserTask to a userId upon reaching the Deadline. This method is
     * used to schedule the reassignment of a UserTask to a userId when the specified UserTask user
     * assignment reaches its deadline in seconds.
     *
     * @param userTaskOutput that is currently assigned to a UserGroup.
     * @param userId that will be assigned to the UserTask after reaching the Deadline
     * @param deadlineSeconds Time in seconds after which the UserTask will be automatically
     *     reassigned to the UserGroup.
     */
    void reassignToUserOnDeadline(UserTaskOutput userTaskOutput, String userId, int deadlineSeconds);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to.
     * @return a NodeOutput.
     */
    UserTaskOutput assignTaskToUser(String userTaskDefName, WfRunVariable userId);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to.
     * @param userGroup is the User's group
     * @return a NodeOutput.
     */
    UserTaskOutput assignTaskToUser(String userTaskDefName, WfRunVariable userId, String userGroup);

    /**
     * Adds a User Task Node, and assigns it to a specific user
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userId is the user id to assign it to.
     * @param userGroup is the User's group
     * @return a NodeOutput.
     */
    UserTaskOutput assignTaskToUser(String userTaskDefName, WfRunVariable userId, WfRunVariable userGroup);

    /**
     * Adds a User Task Node, and assigns it to a group of users.
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userGroup is the User Group to assign the task to.
     * @return a UserTaskOutput.
     */
    public UserTaskOutput assignTaskToUserGroup(String userTaskDefName, String userGroup);

    /**
     * Creates a formatted string using WfRunVariables as arguments. Example:
     *
     *  -> format("Hello there, {0}, today is {1}", name, dayOfWeek);
     *
     * @param format is the format string.
     * @param args are the format args.
     * @return an LHFormatString object which can be used as a variable assignment in a WfSpec.
     */
    public LHFormatString format(String format, WfRunVariable... args);

    /**
     * Adds a User Task Node, and assigns it to a group of users.
     *
     * @param userTaskDefName is the UserTaskDef to assign.
     * @param userGroup is the User Group (either WfRunVariable or String) to assign the task to.
     * @return a UserTaskOutput.
     */
    public UserTaskOutput assignTaskToUserGroup(String userTaskDefName, WfRunVariable userGroup);

    // TODO: Allow assigning User Tasks via `WfRunVariable`

    // TODO: Allow assigning User Tasks to Groups of people (via String and
    // WfRunVariable)

    /**
     * Defines a Variable in the `ThreadSpec` and returns a handle to it.
     *
     * @param name the name of the variable.
     * @param typeOrDefaultVal is either the type of the variable, from the `VariableTypePb` enum,
     *     or an object representing the default value of the Variable. If an object (or primitive)
     *     is provided, the Task Worker Library casts the provided value to a VariableValue and sets
     *     that as the default.
     * @return a handle to the created WfRunVariable.
     */
    public WfRunVariable addVariable(String name, Object typeOrDefaultVal);

    /**
     * Conditionally executes some workflow code; equivalent to an if() statement in programming.
     *
     * @param condition is the WorkflowCondition to be satisfied.
     * @param doIf is the block of ThreadSpec code to be executed if the provided WorkflowCondition
     *     is satisfied.
     */
    public void doIf(WorkflowCondition condition, IfElseBody doIf);

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
    public void doIfElse(WorkflowCondition condition, IfElseBody doIf, IfElseBody doElse);

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
    public void scheduleReminderTask(UserTaskOutput userTask, int delaySeconds, String taskDefName, Object... args);

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
    public void scheduleReminderTask(
            UserTaskOutput userTask, WfRunVariable delaySeconds, String taskDefName, Object... args);

    /**
     * Conditionally executes some workflow code; equivalent to an while() statement in programming.
     *
     * @param condition is the WorkflowCondition to be satisfied.
     * @param whileBody is the block of ThreadFunc code to be executed while the provided
     *     WorkflowCondition is satisfied.
     */
    public void doWhile(WorkflowCondition condition, ThreadFunc whileBody);

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
    public SpawnedThread spawnThread(ThreadFunc threadFunc, String threadName, Map<String, Object> inputVars);

    /**
     * Adds a WAIT_FOR_THREAD node which waits for a Child ThreadRun to complete.
     *
     * @param threadsToWaitFor is an array of SpawnedThread objects returned one or more calls to
     *     spawnThread.
     * @return a NodeOutput that can be used for timeouts or exception handling.
     */
    public NodeOutput waitForThreads(SpawnedThread... threadsToWaitFor);

    /**
     * Adds an EXTERNAL_EVENT node which blocks until an 'ExternalEvent' of the specified type
     * arrives.
     *
     * @param externalEventDefName is the type of ExternalEvent to wait for.
     * @return a NodeOutput for this event.
     */
    public NodeOutput waitForEvent(String externalEventDefName);

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
    public void fail(Object output, String failureName, String message);

    /**
     * Adds an EXIT node with no Failure defined. This causes the ThreadRun to complete gracefully.
     * It is equivalent to putting a call to `return;` early in your function.
     */
    public void complete();

    /**
     * Adds an EXIT node with a Failure defined. This causes a ThreadRun to fail, and the resulting
     * Failure has the specified name and human-readable message.
     *
     * @param failureName is the name of the failure to throw.
     * @param message is a human-readable message.
     */
    public void fail(String failureName, String message);

    /**
     * Registers an Interrupt Handler, such that when an ExternalEvent arrives with the specified
     * type, this ThreadRun is interrupted.
     *
     * @param interruptName The name of the ExternalEventDef to listen for.
     * @param handler A Thread Function defining a ThreadSpec to use to handle the Interrupt.
     */
    public void registerInterruptHandler(String interruptName, ThreadFunc handler);

    /**
     * Adds a SLEEP node which makes the ThreadRun sleep for a specified number of seconds.
     *
     * @param seconds is either an integer representing the number of seconds to sleep for, or it is
     *     a WfRunVariable which evaluates to a VariableTypePb.INT specifying the number of seconds
     *     to sleep for.
     */
    public void sleepSeconds(Object seconds);

    /**
     * Adds a SLEEP node which makes the ThreadRun sleep until a specified timestamp, provided as an
     * INT WfRunVariable (note that INT in LH is a 64-bit integer).
     *
     * @param timestamp a WfRunVariable which evaluates to a VariableTypePb.INT specifying the epoch
     *     timestamp (in milliseconds) to wait for.
     */
    public void sleepUntil(WfRunVariable timestamp);

    /**
     * Adds a Failure Handler to the Node specified by the provided NodeOutput.
     *
     * @param node is a handle to the Node for which we want to catch a failure.
     * @param exceptionName is the name of the specific exception to handle. If left null, then this
     *     Failure Handler catches all failures.
     * @param handler is a ThreadFunction defining a ThreadSpec that should be used to handle the
     *     failure.
     */
    public void handleException(NodeOutput node, String exceptionName, ThreadFunc handler);

    /**
     * Returns a WorkflowCondition that can be used in `ThreadBuilder::doIf()` or
     * `ThreadBuilder::doElse()`.
     *
     * @param lhs is either a literal value (which the Library casts to a Variable Value) or a
     *     `WfRunVariable` representing the LHS of the expression.
     * @param comparator is a Comparator defining the comparator, for example,
     *     `ComparatorTypePb.EQUALS`.
     * @param rhs is either a literal value (which the Library casts to a Variable Value) or a
     *     `WfRunVariable` representing the RHS of the expression.
     * @return a WorkflowCondition.
     */
    public WorkflowCondition condition(Object lhs, Comparator comparator, Object rhs);

    /**
     * Adds a VariableMutation to the last Node
     *
     * @param lhs is a handle to the WfRunVariable to mutate.
     * @param type is the mutation type to use, for example, `VariableMutationTypePb.ASSIGN`.
     * @param rhs is either a literal value (which the Library casts to a Variable Value), a
     *     `WfRunVariable` which determines the right hand side of the expression, or a `NodeOutput`
     *     (which allows you to use the output of a Node Run to mutate variables).
     */
    public void mutate(WfRunVariable lhs, VariableMutationType type, Object rhs);
}
