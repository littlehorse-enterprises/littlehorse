// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface NodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Node)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Defines the flow of execution and determines where the ThreadRun goes next.
   * </pre>
   *
   * <code>repeated .littlehorse.Edge outgoing_edges = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.Edge> 
      getOutgoingEdgesList();
  /**
   * <pre>
   * Defines the flow of execution and determines where the ThreadRun goes next.
   * </pre>
   *
   * <code>repeated .littlehorse.Edge outgoing_edges = 1;</code>
   */
  io.littlehorse.sdk.common.proto.Edge getOutgoingEdges(int index);
  /**
   * <pre>
   * Defines the flow of execution and determines where the ThreadRun goes next.
   * </pre>
   *
   * <code>repeated .littlehorse.Edge outgoing_edges = 1;</code>
   */
  int getOutgoingEdgesCount();
  /**
   * <pre>
   * Defines the flow of execution and determines where the ThreadRun goes next.
   * </pre>
   *
   * <code>repeated .littlehorse.Edge outgoing_edges = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.EdgeOrBuilder> 
      getOutgoingEdgesOrBuilderList();
  /**
   * <pre>
   * Defines the flow of execution and determines where the ThreadRun goes next.
   * </pre>
   *
   * <code>repeated .littlehorse.Edge outgoing_edges = 1;</code>
   */
  io.littlehorse.sdk.common.proto.EdgeOrBuilder getOutgoingEdgesOrBuilder(
      int index);

  /**
   * <pre>
   * Specifies handlers for failures (EXCEPTION or ERROR or both) which might be thrown
   * by the NodeRun. If a Failure is thrown by the Node execution, then the first
   * matching Failure Handler (if present) is run. If there is a matching Failure Handler
   * and it runs to completion, then the ThreadRun advances from the Node; else, it
   * fails.
   * </pre>
   *
   * <code>repeated .littlehorse.FailureHandlerDef failure_handlers = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.FailureHandlerDef> 
      getFailureHandlersList();
  /**
   * <pre>
   * Specifies handlers for failures (EXCEPTION or ERROR or both) which might be thrown
   * by the NodeRun. If a Failure is thrown by the Node execution, then the first
   * matching Failure Handler (if present) is run. If there is a matching Failure Handler
   * and it runs to completion, then the ThreadRun advances from the Node; else, it
   * fails.
   * </pre>
   *
   * <code>repeated .littlehorse.FailureHandlerDef failure_handlers = 4;</code>
   */
  io.littlehorse.sdk.common.proto.FailureHandlerDef getFailureHandlers(int index);
  /**
   * <pre>
   * Specifies handlers for failures (EXCEPTION or ERROR or both) which might be thrown
   * by the NodeRun. If a Failure is thrown by the Node execution, then the first
   * matching Failure Handler (if present) is run. If there is a matching Failure Handler
   * and it runs to completion, then the ThreadRun advances from the Node; else, it
   * fails.
   * </pre>
   *
   * <code>repeated .littlehorse.FailureHandlerDef failure_handlers = 4;</code>
   */
  int getFailureHandlersCount();
  /**
   * <pre>
   * Specifies handlers for failures (EXCEPTION or ERROR or both) which might be thrown
   * by the NodeRun. If a Failure is thrown by the Node execution, then the first
   * matching Failure Handler (if present) is run. If there is a matching Failure Handler
   * and it runs to completion, then the ThreadRun advances from the Node; else, it
   * fails.
   * </pre>
   *
   * <code>repeated .littlehorse.FailureHandlerDef failure_handlers = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.FailureHandlerDefOrBuilder> 
      getFailureHandlersOrBuilderList();
  /**
   * <pre>
   * Specifies handlers for failures (EXCEPTION or ERROR or both) which might be thrown
   * by the NodeRun. If a Failure is thrown by the Node execution, then the first
   * matching Failure Handler (if present) is run. If there is a matching Failure Handler
   * and it runs to completion, then the ThreadRun advances from the Node; else, it
   * fails.
   * </pre>
   *
   * <code>repeated .littlehorse.FailureHandlerDef failure_handlers = 4;</code>
   */
  io.littlehorse.sdk.common.proto.FailureHandlerDefOrBuilder getFailureHandlersOrBuilder(
      int index);

  /**
   * <pre>
   * Creates an EntrypointRun. Every ThreadRun has one Entrypoint node.
   * </pre>
   *
   * <code>.littlehorse.EntrypointNode entrypoint = 5;</code>
   * @return Whether the entrypoint field is set.
   */
  boolean hasEntrypoint();
  /**
   * <pre>
   * Creates an EntrypointRun. Every ThreadRun has one Entrypoint node.
   * </pre>
   *
   * <code>.littlehorse.EntrypointNode entrypoint = 5;</code>
   * @return The entrypoint.
   */
  io.littlehorse.sdk.common.proto.EntrypointNode getEntrypoint();
  /**
   * <pre>
   * Creates an EntrypointRun. Every ThreadRun has one Entrypoint node.
   * </pre>
   *
   * <code>.littlehorse.EntrypointNode entrypoint = 5;</code>
   */
  io.littlehorse.sdk.common.proto.EntrypointNodeOrBuilder getEntrypointOrBuilder();

  /**
   * <pre>
   * Creates an `ExitRun``. Every ThreadSpec has at least one Exit Node.
   * </pre>
   *
   * <code>.littlehorse.ExitNode exit = 6;</code>
   * @return Whether the exit field is set.
   */
  boolean hasExit();
  /**
   * <pre>
   * Creates an `ExitRun``. Every ThreadSpec has at least one Exit Node.
   * </pre>
   *
   * <code>.littlehorse.ExitNode exit = 6;</code>
   * @return The exit.
   */
  io.littlehorse.sdk.common.proto.ExitNode getExit();
  /**
   * <pre>
   * Creates an `ExitRun``. Every ThreadSpec has at least one Exit Node.
   * </pre>
   *
   * <code>.littlehorse.ExitNode exit = 6;</code>
   */
  io.littlehorse.sdk.common.proto.ExitNodeOrBuilder getExitOrBuilder();

  /**
   * <pre>
   * Creates a TaskNodeRUn
   * </pre>
   *
   * <code>.littlehorse.TaskNode task = 7;</code>
   * @return Whether the task field is set.
   */
  boolean hasTask();
  /**
   * <pre>
   * Creates a TaskNodeRUn
   * </pre>
   *
   * <code>.littlehorse.TaskNode task = 7;</code>
   * @return The task.
   */
  io.littlehorse.sdk.common.proto.TaskNode getTask();
  /**
   * <pre>
   * Creates a TaskNodeRUn
   * </pre>
   *
   * <code>.littlehorse.TaskNode task = 7;</code>
   */
  io.littlehorse.sdk.common.proto.TaskNodeOrBuilder getTaskOrBuilder();

  /**
   * <pre>
   * Creates an ExternalEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ExternalEventNode external_event = 8;</code>
   * @return Whether the externalEvent field is set.
   */
  boolean hasExternalEvent();
  /**
   * <pre>
   * Creates an ExternalEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ExternalEventNode external_event = 8;</code>
   * @return The externalEvent.
   */
  io.littlehorse.sdk.common.proto.ExternalEventNode getExternalEvent();
  /**
   * <pre>
   * Creates an ExternalEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ExternalEventNode external_event = 8;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventNodeOrBuilder getExternalEventOrBuilder();

  /**
   * <pre>
   * Creates a StartThreadNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartThreadNode start_thread = 9;</code>
   * @return Whether the startThread field is set.
   */
  boolean hasStartThread();
  /**
   * <pre>
   * Creates a StartThreadNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartThreadNode start_thread = 9;</code>
   * @return The startThread.
   */
  io.littlehorse.sdk.common.proto.StartThreadNode getStartThread();
  /**
   * <pre>
   * Creates a StartThreadNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartThreadNode start_thread = 9;</code>
   */
  io.littlehorse.sdk.common.proto.StartThreadNodeOrBuilder getStartThreadOrBuilder();

  /**
   * <pre>
   * Creates a WaitForThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.WaitForThreadsNode wait_for_threads = 10;</code>
   * @return Whether the waitForThreads field is set.
   */
  boolean hasWaitForThreads();
  /**
   * <pre>
   * Creates a WaitForThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.WaitForThreadsNode wait_for_threads = 10;</code>
   * @return The waitForThreads.
   */
  io.littlehorse.sdk.common.proto.WaitForThreadsNode getWaitForThreads();
  /**
   * <pre>
   * Creates a WaitForThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.WaitForThreadsNode wait_for_threads = 10;</code>
   */
  io.littlehorse.sdk.common.proto.WaitForThreadsNodeOrBuilder getWaitForThreadsOrBuilder();

  /**
   * <pre>
   * Creates a NopNodeRun
   * </pre>
   *
   * <code>.littlehorse.NopNode nop = 11;</code>
   * @return Whether the nop field is set.
   */
  boolean hasNop();
  /**
   * <pre>
   * Creates a NopNodeRun
   * </pre>
   *
   * <code>.littlehorse.NopNode nop = 11;</code>
   * @return The nop.
   */
  io.littlehorse.sdk.common.proto.NopNode getNop();
  /**
   * <pre>
   * Creates a NopNodeRun
   * </pre>
   *
   * <code>.littlehorse.NopNode nop = 11;</code>
   */
  io.littlehorse.sdk.common.proto.NopNodeOrBuilder getNopOrBuilder();

  /**
   * <pre>
   * Creates a SleepNodeRun
   * </pre>
   *
   * <code>.littlehorse.SleepNode sleep = 12;</code>
   * @return Whether the sleep field is set.
   */
  boolean hasSleep();
  /**
   * <pre>
   * Creates a SleepNodeRun
   * </pre>
   *
   * <code>.littlehorse.SleepNode sleep = 12;</code>
   * @return The sleep.
   */
  io.littlehorse.sdk.common.proto.SleepNode getSleep();
  /**
   * <pre>
   * Creates a SleepNodeRun
   * </pre>
   *
   * <code>.littlehorse.SleepNode sleep = 12;</code>
   */
  io.littlehorse.sdk.common.proto.SleepNodeOrBuilder getSleepOrBuilder();

  /**
   * <pre>
   * Creates a UserTaskNodeRun
   * </pre>
   *
   * <code>.littlehorse.UserTaskNode user_task = 13;</code>
   * @return Whether the userTask field is set.
   */
  boolean hasUserTask();
  /**
   * <pre>
   * Creates a UserTaskNodeRun
   * </pre>
   *
   * <code>.littlehorse.UserTaskNode user_task = 13;</code>
   * @return The userTask.
   */
  io.littlehorse.sdk.common.proto.UserTaskNode getUserTask();
  /**
   * <pre>
   * Creates a UserTaskNodeRun
   * </pre>
   *
   * <code>.littlehorse.UserTaskNode user_task = 13;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskNodeOrBuilder getUserTaskOrBuilder();

  /**
   * <pre>
   * Creates a StartMultipleThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartMultipleThreadsNode start_multiple_threads = 15;</code>
   * @return Whether the startMultipleThreads field is set.
   */
  boolean hasStartMultipleThreads();
  /**
   * <pre>
   * Creates a StartMultipleThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartMultipleThreadsNode start_multiple_threads = 15;</code>
   * @return The startMultipleThreads.
   */
  io.littlehorse.sdk.common.proto.StartMultipleThreadsNode getStartMultipleThreads();
  /**
   * <pre>
   * Creates a StartMultipleThreadsNodeRun
   * </pre>
   *
   * <code>.littlehorse.StartMultipleThreadsNode start_multiple_threads = 15;</code>
   */
  io.littlehorse.sdk.common.proto.StartMultipleThreadsNodeOrBuilder getStartMultipleThreadsOrBuilder();

  /**
   * <pre>
   * Creates a ThrowEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ThrowEventNode throw_event = 16;</code>
   * @return Whether the throwEvent field is set.
   */
  boolean hasThrowEvent();
  /**
   * <pre>
   * Creates a ThrowEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ThrowEventNode throw_event = 16;</code>
   * @return The throwEvent.
   */
  io.littlehorse.sdk.common.proto.ThrowEventNode getThrowEvent();
  /**
   * <pre>
   * Creates a ThrowEventNodeRun
   * </pre>
   *
   * <code>.littlehorse.ThrowEventNode throw_event = 16;</code>
   */
  io.littlehorse.sdk.common.proto.ThrowEventNodeOrBuilder getThrowEventOrBuilder();

  /**
   * <pre>
   * Creates a WaitForConditionRun
   * </pre>
   *
   * <code>.littlehorse.WaitForConditionNode wait_for_condition = 17;</code>
   * @return Whether the waitForCondition field is set.
   */
  boolean hasWaitForCondition();
  /**
   * <pre>
   * Creates a WaitForConditionRun
   * </pre>
   *
   * <code>.littlehorse.WaitForConditionNode wait_for_condition = 17;</code>
   * @return The waitForCondition.
   */
  io.littlehorse.sdk.common.proto.WaitForConditionNode getWaitForCondition();
  /**
   * <pre>
   * Creates a WaitForConditionRun
   * </pre>
   *
   * <code>.littlehorse.WaitForConditionNode wait_for_condition = 17;</code>
   */
  io.littlehorse.sdk.common.proto.WaitForConditionNodeOrBuilder getWaitForConditionOrBuilder();

  io.littlehorse.sdk.common.proto.Node.NodeCase getNodeCase();
}
