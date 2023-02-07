package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
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
import io.littlehorse.common.util.LHUtil;
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
    public List<VariableDef> variableDefs;
    public List<InterruptDef> interruptDefs;

    public ThreadSpec() {
        nodes = new HashMap<>();
        variableDefs = new ArrayList<>();
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
        for (VariableDef vd : variableDefs) {
            out.addVariableDefs(vd.toProto());
        }
        for (InterruptDef idef : interruptDefs) {
            out.addInterruptDefs(idef.toProto());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder pr) {
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

        for (VariableDefPb vd : proto.getVariableDefsList()) {
            VariableDef v = new VariableDef();
            v.initFrom(vd);
            v.threadSpec = this;
            variableDefs.add(v);
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

    /*
     * Returns a Map containing info for all of the variables required as parameters
     * to *start* this thread.
     */
    @JsonIgnore
    public Map<String, VariableDef> getRequiredInputVariables() {
        HashMap<String, VariableDef> out = new HashMap<>();
        for (VariableDef vd : variableDefs) {
            out.put(vd.name, vd);
        }

        return out;
    }

    /*
     * Returns a set of all variable names *used* during thread execution.
     */
    public Set<String> getRequiredVariableNames() {
        HashSet<String> out = new HashSet<>();
        for (Node n : nodes.values()) {
            out.addAll(n.getRequiredVariableNames());
        }
        return out;
    }

    @JsonIgnore
    private ThreadSpec parentThread;

    // Returns all the external event def names used for **interrupts**
    @JsonIgnore
    private Set<String> interruptExternalEventDefs;

    @JsonIgnore
    public Set<String> getInterruptExternalEventDefs() {
        if (interruptExternalEventDefs != null) {
            return interruptExternalEventDefs;
        }

        interruptExternalEventDefs = new HashSet<>();
        for (InterruptDef idef : interruptDefs) {
            interruptExternalEventDefs.add(idef.externalEventDefName);
        }
        return interruptExternalEventDefs;
    }

    // Returns all the external event def names used for **EXTERNAL_EVENT nodes**
    @JsonIgnore
    public Set<String> getNodeExternalEventDefs() {
        Set<String> out = new HashSet<>();
        for (Node n : nodes.values()) {
            if (n.type == NodeCase.EXTERNAL_EVENT) {
                out.add(n.externalEventNode.externalEventDefName);
            }
        }
        return out;
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

    private VariableDef getVd(String name) {
        for (VariableDef vd : variableDefs) {
            if (vd.name.equals(name)) {
                return vd;
            }
        }
        return null;
    }

    public Pair<String, VariableDef> lookupVarDef(String name) {
        VariableDef varDef = getVd(name);
        if (varDef != null) {
            return Pair.of(name, varDef);
        }

        return wfSpec.lookupVarDef(name);
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
        validateExternalEventDefUse();
    }

    /*
     * Rules for ExternalEventDef usage:
     * 1. An ExternalEventDef may only be used for an EXTERNAL_EVENT node OR
     *    as an Interrupt trigger, but NOT both.
     * 2. An ExternalEventDef CAN be used as an Interrupt trigger in more
     *    than one ThreadSpec.
     * 3. An ExternalEventDef CAN be used for multiple EXTERNAL_EVENT nodes in
     *    multiple ThreadSpecs.
     * 4. An ExternalEventDef CAN be used for multiple EXTERNAL_EVENT nodes in
     *    the *same* ThreadSpec.
     *
     * If an ExternalEvent comes in and multiple live threads have
     */
    private void validateExternalEventDefUse() throws LHValidationError {
        // Check that interrupts aren't used anywhere else
        for (InterruptDef idef : interruptDefs) {
            String eedn = idef.externalEventDefName;
            if (wfSpec.getNodeExternalEventDefs().contains(eedn)) {
                throw new LHValidationError(
                    null,
                    "ExternalEventDef " + eedn + " used for Node and Interrupt!"
                );
            }

            for (ThreadSpec tspec : wfSpec.threadSpecs.values()) {
                if (tspec.name.equals(name)) continue;

                if (tspec.getInterruptExternalEventDefs().contains(eedn)) {
                    throw new LHValidationError(
                        null,
                        "ExternalEventDef " +
                        eedn +
                        " used by multiple threads as interrupt!"
                    );
                }
            }
        }
    }

    public void validateStartVariables(Map<String, VariableValue> vars)
        throws LHValidationError {
        Map<String, VariableDef> required = getRequiredInputVariables();

        for (Map.Entry<String, VariableDef> e : required.entrySet()) {
            VariableValue val = vars.get(e.getKey());
            if (val == null) {
                LHUtil.log(
                    "Variable",
                    e.getKey(),
                    "not provided, defaulting to null"
                );
                continue;
            }

            if (val.type != e.getValue().type && val.type != VariableTypePb.NULL) {
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
            if (getVd(e.getKey()) == null) {
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
        Map<String, VariableDef> required = getRequiredInputVariables();

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
            if (localGetVarDef(e.getKey()) == null) {
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

    public InterruptDef getInterruptDefFor(String externalEventDefName) {
        for (InterruptDef idef : interruptDefs) {
            if (idef.externalEventDefName.equals(externalEventDefName)) {
                return idef;
            }
        }
        return null;
    }

    public VariableDef localGetVarDef(String name) {
        for (VariableDef vd : variableDefs) {
            if (vd.name.equals(name)) {
                return vd;
            }
        }
        return null;
    }

    public VariableDef getVarDef(String varName) {
        // This is tricky...
        VariableDef out = localGetVarDef(varName);
        if (out != null) return out;

        Pair<String, VariableDef> result = wfSpec.lookupVarDef(varName);
        if (result != null) {
            return result.getRight();
        } else {
            return null;
        }
    }

    public static ThreadSpec fromProto(ThreadSpecPbOrBuilder p) {
        ThreadSpec out = new ThreadSpec();
        out.initFrom(p);
        return out;
    }
}
