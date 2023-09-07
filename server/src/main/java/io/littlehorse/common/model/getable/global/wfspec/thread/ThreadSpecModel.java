package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.InterruptDef;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Setter
@Getter
public class ThreadSpecModel extends LHSerializable<ThreadSpec> {

    public String name;

    public Map<String, NodeModel> nodes;
    public List<VariableDefModel> variableDefs;
    public List<InterruptDefModel> interruptDefs;

    public ThreadSpecModel() {
        nodes = new HashMap<>();
        variableDefs = new ArrayList<>();
        interruptDefs = new ArrayList<>();
    }

    public Class<ThreadSpec> getProtoBaseClass() {
        return ThreadSpec.class;
    }

    // Below is Serde

    public ThreadSpec.Builder toProto() {
        ThreadSpec.Builder out = ThreadSpec.newBuilder();

        for (Map.Entry<String, NodeModel> e : nodes.entrySet()) {
            out.putNodes(e.getKey(), e.getValue().toProto().build());
        }
        for (VariableDefModel vd : variableDefs) {
            out.addVariableDefs(vd.toProto());
        }
        for (InterruptDefModel idef : interruptDefs) {
            out.addInterruptDefs(idef.toProto());
        }
        return out;
    }

    public void initFrom(Message pr) {
        ThreadSpec proto = (ThreadSpec) pr;
        for (Map.Entry<String, Node> p : proto.getNodesMap().entrySet()) {
            NodeModel n = new NodeModel();
            n.name = p.getKey();
            n.threadSpecModel = this;
            n.initFrom(p.getValue());
            this.nodes.put(p.getKey(), n);
            if (n.type == NodeCase.ENTRYPOINT) {
                this.entrypointNodeName = n.name;
            }
        }

        for (VariableDef vd : proto.getVariableDefsList()) {
            VariableDefModel v = new VariableDefModel();
            v.initFrom(vd);
            v.threadSpecModel = this;
            variableDefs.add(v);
        }

        for (InterruptDef idefpb : proto.getInterruptDefsList()) {
            InterruptDefModel idef = InterruptDefModel.fromProto(idefpb);
            idef.ownerThreadSpecModel = this;
            interruptDefs.add(idef);
        }
    }

    // Below is Implementation

    public String entrypointNodeName;

    public WfSpecModel wfSpecModel;

    /*
     * Returns a Map containing info for all of the variables required as parameters
     * to *start* this thread.
     */

    public Map<String, VariableDefModel> getInputVariableDefs() {
        HashMap<String, VariableDefModel> out = new HashMap<>();
        for (VariableDefModel vd : variableDefs) {
            out.put(vd.name, vd);
        }

        return out;
    }

    /*
     * Returns a set of all variable names *used* during thread execution.
     */
    public Set<String> getRequiredVariableNames() {
        HashSet<String> out = new HashSet<>();
        for (NodeModel n : nodes.values()) {
            out.addAll(n.getRequiredVariableNames());
        }
        return out;
    }

    // Returns all the external event def names used for **interrupts**

    private Set<String> interruptExternalEventDefs;

    public Set<String> getInterruptExternalEventDefs() {
        if (interruptExternalEventDefs != null) {
            return interruptExternalEventDefs;
        }

        interruptExternalEventDefs = new HashSet<>();
        for (InterruptDefModel idef : interruptDefs) {
            interruptExternalEventDefs.add(idef.externalEventDefName);
        }
        return interruptExternalEventDefs;
    }

    // Returns all the external event def names used for **EXTERNAL_EVENT nodes**

    public Set<String> getNodeExternalEventDefs() {
        Set<String> out = new HashSet<>();
        for (NodeModel n : nodes.values()) {
            if (n.type == NodeCase.EXTERNAL_EVENT) {
                out.add(n.externalEventNode.externalEventDefName);
            }
        }
        return out;
    }

    public Set<String> getChildThreadNames() {
        Set<String> out = new HashSet<>();
        for (NodeModel node : nodes.values()) {
            if (node.type == NodeCase.START_THREAD) {
                out.add(node.startThreadNode.threadSpecName);
            }
        }
        // TODO: Add interrupts here.
        return out;
    }

    private VariableDefModel getVd(String name) {
        for (VariableDefModel vd : variableDefs) {
            if (vd.name.equals(name)) {
                return vd;
            }
        }
        return null;
    }

    public Pair<String, VariableDefModel> lookupVarDef(String name) {
        VariableDefModel varDef = getVd(name);
        if (varDef != null) {
            return Pair.of(name, varDef);
        }

        return wfSpecModel.lookupVarDef(name);
    }

    public void validate(ReadOnlyMetadataStore dbClient, LHServerConfig config) throws LHApiException {
        if (entrypointNodeName == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "missing ENTRYPOITNT node!");
        }

        boolean seenEntrypoint = false;
        for (NodeModel node : nodes.values()) {
            for (String varName : node.getRequiredVariableNames()) {
                Pair<String, VariableDefModel> result = lookupVarDef(varName);
                if (result == null) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            " node " + node.name + " refers to unknown or out-of-scope variable " + varName);
                }
            }
            if (node.type == NodeCase.ENTRYPOINT) {
                if (seenEntrypoint) {
                    throw new LHApiException(Status.INVALID_ARGUMENT, "Multiple ENTRYPOINT nodes!");
                }
                seenEntrypoint = true;
            }
            try {
                node.validate(dbClient, config);
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix("Node " + node.name);
            }
        }

        for (InterruptDefModel idef : interruptDefs) {
            try {
                idef.validate(dbClient, config);
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix("Interrupt Def for " + idef.externalEventDefName);
            }
        }
        validateExternalEventDefUse();
    }

    /*
     * Rules for ExternalEventDef usage:
     * 1. An ExternalEventDef may only be used for an EXTERNAL_EVENT node OR
     * as an Interrupt trigger, but NOT both.
     * 2. An ExternalEventDef CAN be used as an Interrupt trigger in more
     * than one ThreadSpec.
     * 3. An ExternalEventDef CAN be used for multiple EXTERNAL_EVENT nodes in
     * multiple ThreadSpecs.
     * 4. An ExternalEventDef CAN be used for multiple EXTERNAL_EVENT nodes in
     * the *same* ThreadSpec.
     *
     * If an ExternalEvent comes in and multiple live threads have
     */
    private void validateExternalEventDefUse() throws LHApiException {
        // Check that interrupts aren't used anywhere else
        for (InterruptDefModel idef : interruptDefs) {
            String eedn = idef.externalEventDefName;
            if (wfSpecModel.getNodeExternalEventDefs().contains(eedn)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "ExternalEventDef " + eedn + " used for Node and Interrupt!");
            }

            for (ThreadSpecModel tspec : wfSpecModel.threadSpecs.values()) {
                if (tspec.name.equals(name)) continue;

                if (tspec.getInterruptExternalEventDefs().contains(eedn)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ExternalEventDef " + eedn + " used by multiple threads as interrupt!");
                }
            }
        }
    }

    public void validateStartVariables(Map<String, VariableValueModel> vars) throws LHValidationError {
        Map<String, VariableDefModel> required = getInputVariableDefs();

        for (Map.Entry<String, VariableDefModel> e : required.entrySet()) {
            VariableValueModel val = vars.get(e.getKey());
            if (val == null) {
                log.debug("Variable {} not provided, defaulting to null", e.getKey());
                continue;
            }

            if (val.type != e.getValue().type && val.type != VariableType.NULL) {
                throw new LHValidationError(
                        null, "Var " + e.getKey() + " should be " + e.getValue().type + " but is " + val.type);
            }
        }

        for (Map.Entry<String, VariableValueModel> e : vars.entrySet()) {
            if (getVd(e.getKey()) == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Var " + e.getKey() + " provided but not needed for thread " + name);
            }
        }
    }

    public void validateTimeoutAssignment(String nodeName, VariableAssignmentModel timeoutSeconds)
            throws LHValidationError {
        if (timeoutSeconds.getRhsSourceType() == SourceCase.VARIABLE_NAME) {
            Pair<String, VariableDefModel> defPair = lookupVarDef(timeoutSeconds.getVariableName());
            if (defPair == null) {
                throw new LHValidationError(
                        null,
                        "Timeout on node "
                                + nodeName
                                + " refers to missing variable "
                                + timeoutSeconds.getVariableName());
            }
        }
        if (!timeoutSeconds.canBeType(VariableType.INT, this)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Timeout on node " + nodeName + " refers to non INT variable.");
        }
    }

    public void validateStartVariablesByType(Map<String, VariableAssignmentModel> vars) throws LHApiException {
        Map<String, VariableDefModel> inputVarDefs = getInputVariableDefs();

        for (Map.Entry<String, VariableDefModel> e : inputVarDefs.entrySet()) {
            VariableAssignmentModel assn = vars.get(e.getKey());
            if (assn == null) {
                // It will be created as NULL for the input.
                continue;
            }

            if (!assn.canBeType(e.getValue().type, this)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Var " + e.getKey() + " should be " + e.getValue().type);
            }
        }

        for (Map.Entry<String, VariableAssignmentModel> e : vars.entrySet()) {
            if (localGetVarDef(e.getKey()) == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Var " + e.getKey() + " provided but not needed for thread " + name);
            }
        }
    }

    public InterruptDefModel getInterruptDefFor(String externalEventDefName) {
        for (InterruptDefModel idef : interruptDefs) {
            if (idef.externalEventDefName.equals(externalEventDefName)) {
                return idef;
            }
        }
        return null;
    }

    public VariableDefModel localGetVarDef(String name) {
        for (VariableDefModel vd : variableDefs) {
            if (vd.name.equals(name)) {
                return vd;
            }
        }
        return null;
    }

    public VariableDefModel getVarDef(String varName) {
        // This is tricky...
        VariableDefModel out = localGetVarDef(varName);
        if (out != null) return out;

        Pair<String, VariableDefModel> result = wfSpecModel.lookupVarDef(varName);
        if (result != null) {
            return result.getRight();
        } else {
            return null;
        }
    }

    public static ThreadSpecModel fromProto(ThreadSpec p) {
        ThreadSpecModel out = new ThreadSpecModel();
        out.initFrom(p);
        return out;
    }
}
