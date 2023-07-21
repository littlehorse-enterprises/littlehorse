// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface NodePbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.NodePb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .littlehorse.EdgePb outgoing_edges = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.EdgePb> 
      getOutgoingEdgesList();
  /**
   * <code>repeated .littlehorse.EdgePb outgoing_edges = 1;</code>
   */
  io.littlehorse.sdk.common.proto.EdgePb getOutgoingEdges(int index);
  /**
   * <code>repeated .littlehorse.EdgePb outgoing_edges = 1;</code>
   */
  int getOutgoingEdgesCount();
  /**
   * <code>repeated .littlehorse.EdgePb outgoing_edges = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.EdgePbOrBuilder> 
      getOutgoingEdgesOrBuilderList();
  /**
   * <code>repeated .littlehorse.EdgePb outgoing_edges = 1;</code>
   */
  io.littlehorse.sdk.common.proto.EdgePbOrBuilder getOutgoingEdgesOrBuilder(
      int index);

  /**
   * <code>repeated .littlehorse.VariableMutationPb variable_mutations = 2;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.VariableMutationPb> 
      getVariableMutationsList();
  /**
   * <code>repeated .littlehorse.VariableMutationPb variable_mutations = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutationPb getVariableMutations(int index);
  /**
   * <code>repeated .littlehorse.VariableMutationPb variable_mutations = 2;</code>
   */
  int getVariableMutationsCount();
  /**
   * <code>repeated .littlehorse.VariableMutationPb variable_mutations = 2;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.VariableMutationPbOrBuilder> 
      getVariableMutationsOrBuilderList();
  /**
   * <code>repeated .littlehorse.VariableMutationPb variable_mutations = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutationPbOrBuilder getVariableMutationsOrBuilder(
      int index);

  /**
   * <code>repeated .littlehorse.FailureHandlerDefPb failure_handlers = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.FailureHandlerDefPb> 
      getFailureHandlersList();
  /**
   * <code>repeated .littlehorse.FailureHandlerDefPb failure_handlers = 4;</code>
   */
  io.littlehorse.sdk.common.proto.FailureHandlerDefPb getFailureHandlers(int index);
  /**
   * <code>repeated .littlehorse.FailureHandlerDefPb failure_handlers = 4;</code>
   */
  int getFailureHandlersCount();
  /**
   * <code>repeated .littlehorse.FailureHandlerDefPb failure_handlers = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.FailureHandlerDefPbOrBuilder> 
      getFailureHandlersOrBuilderList();
  /**
   * <code>repeated .littlehorse.FailureHandlerDefPb failure_handlers = 4;</code>
   */
  io.littlehorse.sdk.common.proto.FailureHandlerDefPbOrBuilder getFailureHandlersOrBuilder(
      int index);

  /**
   * <code>.littlehorse.EntrypointNodePb entrypoint = 5;</code>
   * @return Whether the entrypoint field is set.
   */
  boolean hasEntrypoint();
  /**
   * <code>.littlehorse.EntrypointNodePb entrypoint = 5;</code>
   * @return The entrypoint.
   */
  io.littlehorse.sdk.common.proto.EntrypointNodePb getEntrypoint();
  /**
   * <code>.littlehorse.EntrypointNodePb entrypoint = 5;</code>
   */
  io.littlehorse.sdk.common.proto.EntrypointNodePbOrBuilder getEntrypointOrBuilder();

  /**
   * <code>.littlehorse.ExitNodePb exit = 6;</code>
   * @return Whether the exit field is set.
   */
  boolean hasExit();
  /**
   * <code>.littlehorse.ExitNodePb exit = 6;</code>
   * @return The exit.
   */
  io.littlehorse.sdk.common.proto.ExitNodePb getExit();
  /**
   * <code>.littlehorse.ExitNodePb exit = 6;</code>
   */
  io.littlehorse.sdk.common.proto.ExitNodePbOrBuilder getExitOrBuilder();

  /**
   * <code>.littlehorse.TaskNodePb task = 7;</code>
   * @return Whether the task field is set.
   */
  boolean hasTask();
  /**
   * <code>.littlehorse.TaskNodePb task = 7;</code>
   * @return The task.
   */
  io.littlehorse.sdk.common.proto.TaskNodePb getTask();
  /**
   * <code>.littlehorse.TaskNodePb task = 7;</code>
   */
  io.littlehorse.sdk.common.proto.TaskNodePbOrBuilder getTaskOrBuilder();

  /**
   * <code>.littlehorse.ExternalEventNodePb external_event = 8;</code>
   * @return Whether the externalEvent field is set.
   */
  boolean hasExternalEvent();
  /**
   * <code>.littlehorse.ExternalEventNodePb external_event = 8;</code>
   * @return The externalEvent.
   */
  io.littlehorse.sdk.common.proto.ExternalEventNodePb getExternalEvent();
  /**
   * <code>.littlehorse.ExternalEventNodePb external_event = 8;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventNodePbOrBuilder getExternalEventOrBuilder();

  /**
   * <code>.littlehorse.StartThreadNodePb start_thread = 9;</code>
   * @return Whether the startThread field is set.
   */
  boolean hasStartThread();
  /**
   * <code>.littlehorse.StartThreadNodePb start_thread = 9;</code>
   * @return The startThread.
   */
  io.littlehorse.sdk.common.proto.StartThreadNodePb getStartThread();
  /**
   * <code>.littlehorse.StartThreadNodePb start_thread = 9;</code>
   */
  io.littlehorse.sdk.common.proto.StartThreadNodePbOrBuilder getStartThreadOrBuilder();

  /**
   * <code>.littlehorse.WaitForThreadsNodePb wait_for_threads = 10;</code>
   * @return Whether the waitForThreads field is set.
   */
  boolean hasWaitForThreads();
  /**
   * <code>.littlehorse.WaitForThreadsNodePb wait_for_threads = 10;</code>
   * @return The waitForThreads.
   */
  io.littlehorse.sdk.common.proto.WaitForThreadsNodePb getWaitForThreads();
  /**
   * <code>.littlehorse.WaitForThreadsNodePb wait_for_threads = 10;</code>
   */
  io.littlehorse.sdk.common.proto.WaitForThreadsNodePbOrBuilder getWaitForThreadsOrBuilder();

  /**
   * <code>.littlehorse.NopNodePb nop = 11;</code>
   * @return Whether the nop field is set.
   */
  boolean hasNop();
  /**
   * <code>.littlehorse.NopNodePb nop = 11;</code>
   * @return The nop.
   */
  io.littlehorse.sdk.common.proto.NopNodePb getNop();
  /**
   * <code>.littlehorse.NopNodePb nop = 11;</code>
   */
  io.littlehorse.sdk.common.proto.NopNodePbOrBuilder getNopOrBuilder();

  /**
   * <code>.littlehorse.SleepNodePb sleep = 12;</code>
   * @return Whether the sleep field is set.
   */
  boolean hasSleep();
  /**
   * <code>.littlehorse.SleepNodePb sleep = 12;</code>
   * @return The sleep.
   */
  io.littlehorse.sdk.common.proto.SleepNodePb getSleep();
  /**
   * <code>.littlehorse.SleepNodePb sleep = 12;</code>
   */
  io.littlehorse.sdk.common.proto.SleepNodePbOrBuilder getSleepOrBuilder();

  /**
   * <code>.littlehorse.UserTaskNodePb user_task = 13;</code>
   * @return Whether the userTask field is set.
   */
  boolean hasUserTask();
  /**
   * <code>.littlehorse.UserTaskNodePb user_task = 13;</code>
   * @return The userTask.
   */
  io.littlehorse.sdk.common.proto.UserTaskNodePb getUserTask();
  /**
   * <code>.littlehorse.UserTaskNodePb user_task = 13;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskNodePbOrBuilder getUserTaskOrBuilder();

  public io.littlehorse.sdk.common.proto.NodePb.NodeCase getNodeCase();
}
