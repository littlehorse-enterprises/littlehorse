package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.ThreadSpecPbOrBuilder;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.HashMap;
import java.util.Map;

public class ThreadSpec extends LHSerializable<ThreadSpecPbOrBuilder> {

  public String name;

  public Map<String, Node> nodes;
  public Map<String, VariableDef> variableDefs;

  public ThreadSpec() {
    nodes = new HashMap<>();
    variableDefs = new HashMap<>();
  }

  @JsonIgnore
  public Class<ThreadSpecPb> getProtoBaseClass() {
    return ThreadSpecPb.class;
  }

  // Below is Serde
  @JsonIgnore
  public ThreadSpecPb.Builder toProto() {
    ThreadSpecPb.Builder out = ThreadSpecPb.newBuilder();

    for (Map.Entry<String, Node> e : nodes.entrySet()) {
      out.putNodes(e.getKey(), e.getValue().toProto().build());
    }
    for (Map.Entry<String, VariableDef> e : variableDefs.entrySet()) {
      out.putVariableDefs(e.getKey(), e.getValue().toProto().build());
    }
    return out;
  }

  public void initFrom(MessageOrBuilder pr) throws LHSerdeError {
    ThreadSpecPbOrBuilder proto = (ThreadSpecPbOrBuilder) pr;
    for (Map.Entry<String, NodePb> p : proto.getNodesMap().entrySet()) {
      Node n = new Node();
      n.name = p.getKey();
      n.threadSpec = this;
      n.initFrom(p.getValue());
      this.nodes.put(p.getKey(), n);
      if (n.type == NodeCase.ENTRYPOINT) {
        this.entrypointNodeName = n.name;
      }
    }

    for (Map.Entry<String, VariableDefPb> p : proto
      .getVariableDefsMap()
      .entrySet()) {
      VariableDef v = new VariableDef();
      v.initFrom(p.getValue());
      v.name = p.getKey();
      v.threadSpec = this;
      variableDefs.put(p.getKey(), v);
    }
  }

  // Below is Implementation
  @JsonIgnore
  public String entrypointNodeName;

  @JsonIgnore
  public WfSpec wfSpec;

  @JsonIgnore
  public Map<String, VariableDef> getRequiredVariables() {
    HashMap<String, VariableDef> out = new HashMap<>();
    for (Map.Entry<String, VariableDef> entry : variableDefs.entrySet()) {
      if (entry.getValue().required) {
        out.put(entry.getKey(), entry.getValue());
      }
    }

    return out;
  }

  public void validate(LHGlobalMetaStores dbClient, LHConfig config)
    throws LHValidationError {
    if (entrypointNodeName == null) {
      throw new LHValidationError(
        null,
        "thread " + name + " missing ENTRYPOITNT node!"
      );
    }

    boolean seenEntrypoint = false;
    for (Node node : nodes.values()) {
      if (node.type == NodeCase.ENTRYPOINT) {
        if (seenEntrypoint) {
          throw new LHValidationError(
            null,
            "Thread " + name + " has multiple ENTRYPOINT nodes!"
          );
        }
        seenEntrypoint = true;
      }
      node.validate(dbClient, config);
    }
  }

  public void validateStartVariables(Map<String, VariableValue> vars)
    throws LHValidationError {
    Map<String, VariableDef> required = getRequiredVariables();

    for (Map.Entry<String, VariableDef> e : required.entrySet()) {
      VariableValue val = vars.get(e.getKey());
      if (val == null) {
        throw new LHValidationError(
          null,
          "Thread " + name + " requires variable " + e.getKey()
        );
      }

      if (val.type != e.getValue().type) {
        throw new LHValidationError(
          null,
          "Var " +
          e.getKey() +
          " should be " +
          e.getValue().type +
          " but is " +
          val.type
        );
      }
    }

    for (Map.Entry<String, VariableValue> e : vars.entrySet()) {
      if (!required.containsKey(e.getKey())) {
        throw new LHValidationError(
          null,
          "Var " + e.getKey() + " provided but not needed for thread " + name
        );
      }
    }
  }
}
