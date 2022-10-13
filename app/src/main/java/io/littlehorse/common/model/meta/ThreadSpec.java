package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.InterruptDefPb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.ThreadSpecPbOrBuilder;
import io.littlehorse.common.proto.VariableAssignmentPb.SourceCase;
import io.littlehorse.common.proto.VariableDefPb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class ThreadSpec extends LHSerializable<ThreadSpecPbOrBuilder> {

    public String name;

    public Map<String, Node> nodes;
    public Map<String, VariableDef> variableDefs;
    public List<InterruptDef> interruptDefs;

    public ThreadSpec() {
        nodes = new HashMap<>();
        variableDefs = new HashMap<>();
        interruptDefs = new ArrayList<>();
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
        for (InterruptDef idef : interruptDefs) {
            out.addInterruptDefs(idef.toProto());
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

        for (InterruptDefPb idefpb : proto.getInterruptDefsList()) {
            InterruptDef idef = InterruptDef.fromProto(idefpb);
            idef.ownerThreadSpec = this;
            interruptDefs.add(idef);
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

    @JsonIgnore
    private ThreadSpec parentThread;

    // This method has a problem--it's possible that a ThreadSpec may have
    // multiple parents, but this only returns the first.
    @JsonIgnore
    public ThreadSpec getParentThread() {
        if (parentThread != null) return parentThread;

        for (ThreadSpec thread : wfSpec.threadSpecs.values()) {
            if (thread.getChildThreadNames().contains(name)) {
                parentThread = thread;
                return thread;
            }
        }
        return null;
    }

    public Set<String> getChildThreadNames() {
        Set<String> out = new HashSet<>();
        for (Node node : nodes.values()) {
            if (node.type == NodeCase.START_THREAD) {
                out.add(node.startThreadNode.threadSpecName);
            }
        }
        // TODO: Add interrupts here.
        return out;
    }

    public Pair<String, VariableDef> lookupVarDef(String name) {
        VariableDef varDef = variableDefs.get(name);
        if (varDef != null) {
            return Pair.of(name, varDef);
        }

        if (getParentThread() != null) {
            return getParentThread().lookupVarDef(name);
        }

        return null;
    }

    public void validate(LHGlobalMetaStores dbClient, LHConfig config)
        throws LHValidationError {
        if (entrypointNodeName == null) {
            throw new LHValidationError(null, "missing ENTRYPOITNT node!");
        }

        boolean seenEntrypoint = false;
        for (Node node : nodes.values()) {
            for (String varName : node.getRequiredVariableNames()) {
                Pair<String, VariableDef> result = lookupVarDef(varName);
                if (result == null) {
                    throw new LHValidationError(
                        null,
                        " node " +
                        node.name +
                        " refers to unknown or out-of-scope variable " +
                        varName
                    );
                }
            }
            if (node.type == NodeCase.ENTRYPOINT) {
                if (seenEntrypoint) {
                    throw new LHValidationError(null, "Multiple ENTRYPOINT nodes!");
                }
                seenEntrypoint = true;
            }
            try {
                node.validate(dbClient, config);
            } catch (LHValidationError exn) {
                exn.addPrefix("Node " + node.name);
                throw exn;
            }
        }

        for (InterruptDef idef : interruptDefs) {
            try {
                idef.validate(dbClient, config);
            } catch (LHValidationError exn) {
                exn.addPrefix("Interrupt Def for " + idef.externalEventDefName);
                throw exn;
            }
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
            if (!variableDefs.containsKey(e.getKey())) {
                throw new LHValidationError(
                    null,
                    "Var " +
                    e.getKey() +
                    " provided but not needed for thread " +
                    name
                );
            }
        }
    }

    public void validateTimeoutAssignment(
        String nodeName,
        VariableAssignment timeoutSeconds
    ) throws LHValidationError {
        if (timeoutSeconds.rhsSourceType == SourceCase.VARIABLE_NAME) {
            Pair<String, VariableDef> defPair = lookupVarDef(
                timeoutSeconds.rhsVariableName
            );
            if (defPair == null) {
                throw new LHValidationError(
                    null,
                    "Timeout on node " +
                    nodeName +
                    " refers to missing variable " +
                    timeoutSeconds.rhsVariableName
                );
            }
        }
        if (!timeoutSeconds.canBeType(VariableTypePb.INT, this)) {
            throw new LHValidationError(
                null,
                "Timeout on node " + nodeName + " refers to non INT variable."
            );
        }
    }

    public void validateStartVariablesByType(Map<String, VariableAssignment> vars)
        throws LHValidationError {
        Map<String, VariableDef> required = getRequiredVariables();

        for (Map.Entry<String, VariableDef> e : required.entrySet()) {
            VariableAssignment assn = vars.get(e.getKey());
            if (assn == null) {
                throw new LHValidationError(
                    null,
                    "Thread " + name + " requires variable " + e.getKey()
                );
            }

            if (!assn.canBeType(e.getValue().type, this)) {
                throw new LHValidationError(
                    null,
                    "Var " + e.getKey() + " should be " + e.getValue().type
                );
            }
        }

        for (Map.Entry<String, VariableAssignment> e : vars.entrySet()) {
            if (!variableDefs.containsKey(e.getKey())) {
                throw new LHValidationError(
                    null,
                    "Var " +
                    e.getKey() +
                    " provided but not needed for thread " +
                    name
                );
            }
        }
    }

    public VariableDef getVarDef(String varName) {
        // This is tricky...
        VariableDef out = variableDefs.get(varName);
        if (out != null) return out;

        ThreadSpec parent = getParentThread();
        if (parent != null) {
            return parent.getVarDef(varName);
        }

        return null;
    }
}
