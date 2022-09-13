package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.node.EntrypointNode;
import io.littlehorse.common.model.meta.node.ExitNode;
import io.littlehorse.common.model.meta.node.TaskNode;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.ArrayList;
import java.util.List;

public class Node extends LHSerializable<NodePbOrBuilder> {

  public NodeCase type;
  public TaskNode taskNode;
  public EntrypointNode entrypointNode;
  public ExitNode exitNode;

  @JsonIgnore
  public Class<NodePb> getProtoBaseClass() {
    return NodePb.class;
  }

  @JsonIgnore
  public NodePb.Builder toProto() {
    NodePb.Builder out = NodePb.newBuilder();

    for (Edge o : outgoingEdges) {
      out.addOutgoingEdges(o.toProto());
    }

    switch (type) {
      case TASK:
        out.setTask(taskNode.toProto());
        break;
      case ENTRYPOINT:
        out.setEntrypoint(entrypointNode.toProto());
        break;
      case EXIT:
        out.setExit(exitNode.toProto());
        break;
      case NODE_NOT_SET:
      // nothing to do.
    }

    return out;
  }

  public void initFrom(MessageOrBuilder p) throws LHSerdeError {
    NodePbOrBuilder proto = (NodePbOrBuilder) p;
    type = proto.getNodeCase();

    for (EdgePb epb : proto.getOutgoingEdgesList()) {
      Edge edge = Edge.fromProto(epb);
      edge.threadSpec = threadSpec;
      outgoingEdges.add(edge);
    }

    switch (type) {
      case TASK:
        taskNode = new TaskNode();
        taskNode.initFrom(proto.getTask());
        break;
      case ENTRYPOINT:
        entrypointNode = new EntrypointNode();
        entrypointNode.initFrom(proto.getEntrypoint());
        break;
      case EXIT:
        exitNode = new ExitNode();
        exitNode.initFrom(proto.getExit());
        break;
      case NODE_NOT_SET:
        throw new LHSerdeError(
          null,
          "Node " + name + " on thread " + threadSpec.name + " is unset!"
        );
    }
  }

  // Implementation details below

  public Node() {
    outgoingEdges = new ArrayList<>();
  }

  public List<Edge> outgoingEdges;
  public String name;

  @JsonIgnore
  public ThreadSpec threadSpec;

  public void validate(LHGlobalMetaStores client, LHConfig config)
    throws LHValidationError {
    for (Edge e : outgoingEdges) {
      Node sink = threadSpec.nodes.get(e.sinkNodeName);
      if (sink == null) {
        throw new LHValidationError(
          null,
          "Node " +
          name +
          " on thread " +
          threadSpec.name +
          " has edge" +
          " referring to nonexistent node " +
          e.sinkNodeName
        );
      }
      if (sink.type == NodeCase.ENTRYPOINT) {
        throw new LHValidationError(
          null,
          "Thread " +
          threadSpec.name +
          " has entrypoint node with " +
          " incoming edge from node " +
          name +
          "."
        );
      }
    }

    if (type == NodeCase.TASK) {
      validateTask(client, config);
    }
  }

  private void validateTask(LHGlobalMetaStores stores, LHConfig config)
    throws LHValidationError {
    TaskDef task = stores.getTaskDef(taskNode.taskDefName);
    if (task == null) {
      throw new LHValidationError(
        null,
        "Node " +
        name +
        " on thread " +
        threadSpec.name +
        " refers to " +
        "nonexistent TaskDef " +
        taskNode.taskDefName
      );
    }
    if (taskNode.timeoutSeconds == null) {
      taskNode.timeoutSeconds = config.getDefaultTaskTimeout();
    }
    if (taskNode.retries < 0) {
      throw new LHValidationError(
        null,
        "Node " +
        name +
        " on thread " +
        threadSpec.name +
        "has negative " +
        "number of retries!"
      );
    }
    if (taskNode.timeoutSeconds < 1) {
      throw new LHValidationError(
        null,
        "Task Timeout must be > 1s for node " + name + " on thread " + threadSpec.name
      );
    }
  }
}
