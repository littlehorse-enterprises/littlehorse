package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
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
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    public List<ThreadVarDefModel> variableDefs;
    public List<InterruptDefModel> interruptDefs;

    private ThreadRetentionPolicyModel retentionPolicy;
    private ExecutionContext executionContext;

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
        for (ThreadVarDefModel tvd : variableDefs) {
            out.addVariableDefs(tvd.toProto());
        }
        for (InterruptDefModel idef : interruptDefs) {
            out.addInterruptDefs(idef.toProto());
        }

        if (retentionPolicy != null) {
            out.setRetentionPolicy(retentionPolicy.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message pr, ExecutionContext context) {
        ThreadSpec proto = (ThreadSpec) pr;
        for (Map.Entry<String, Node> p : proto.getNodesMap().entrySet()) {
            NodeModel n = new NodeModel();
            n.name = p.getKey();
            n.threadSpec = this;
            n.initFrom(p.getValue(), context);
            this.nodes.put(p.getKey(), n);
            if (n.type == NodeCase.ENTRYPOINT) {
                this.entrypointNodeName = n.name;
            }
        }

        for (ThreadVarDef tvd : proto.getVariableDefsList()) {
            ThreadVarDefModel tvdm = LHSerializable.fromProto(tvd, ThreadVarDefModel.class, context);
            variableDefs.add(tvdm);
        }

        for (InterruptDef idefpb : proto.getInterruptDefsList()) {
            InterruptDefModel idef = InterruptDefModel.fromProto(idefpb, context);
            idef.ownerThreadSpecModel = this;
            interruptDefs.add(idef);
        }

        if (proto.hasRetentionPolicy()) {
            retentionPolicy =
                    LHSerializable.fromProto(proto.getRetentionPolicy(), ThreadRetentionPolicyModel.class, context);
        }
        this.executionContext = context;
    }

    // Below is Implementation

    public String entrypointNodeName;

    public WfSpecModel wfSpec;

    /*
     * Returns a Map containing info for all of the variables required as parameters
     * to *start* this thread.
     */
    public Map<String, ThreadVarDefModel> getInputVariableDefs() {
        HashMap<String, ThreadVarDefModel> out = new HashMap<>();
        for (ThreadVarDefModel vd : variableDefs) {
            out.put(vd.getVarDef().getName(), vd);
        }

        return out;
    }

    /*
     * Returns a set of all variable names *used* during thread execution.
     */
    public Set<String> getNamesOfVariablesUsed() {
        HashSet<String> out = new HashSet<>();
        for (NodeModel n : nodes.values()) {
            out.addAll(n.getRequiredVariableNames());
        }
        return out;
    }

    /**
     * Returns a set of all ThreadVarDef's for variables that are required as
     * input to start a ThreadRun of this ThreadSpec.
     * @return all required ThreadVarDefs.
     */
    public Set<ThreadVarDefModel> getRequiredVarDefs() {
        return variableDefs.stream().filter(varDef -> varDef.isRequired()).collect(Collectors.toSet());
    }

    /**
     * Returns a set of all ThreadVarDef's for searchable variables.
     * @return all searchable ThreadVarDefs.
     */
    public Set<ThreadVarDefModel> getSearchableVarDefs() {
        return variableDefs.stream().filter(varDef -> varDef.isSearchable()).collect(Collectors.toSet());
    }

    // Returns all the external event def names used for **interrupts**

    private Set<String> interruptExternalEventDefs;

    public Set<String> getInterruptExternalEventDefs() {
        if (interruptExternalEventDefs != null) {
            return interruptExternalEventDefs;
        }

        interruptExternalEventDefs = new HashSet<>();
        for (InterruptDefModel idef : interruptDefs) {
            interruptExternalEventDefs.add(idef.getExternalEventDefId().getName());
        }
        return interruptExternalEventDefs;
    }

    // Returns all the external event def names used for **EXTERNAL_EVENT nodes**

    public Set<String> getNodeExternalEventDefs() {
        Set<String> out = new HashSet<>();
        for (NodeModel n : nodes.values()) {
            if (n.type == NodeCase.EXTERNAL_EVENT) {
                out.add(n.externalEventNode.getExternalEventDefId().getName());
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

    private ThreadVarDefModel getVd(String name) {
        for (ThreadVarDefModel vd : variableDefs) {
            if (vd.getVarDef().getName().equals(name)) {
                return vd;
            }
        }
        return null;
    }

    /**
     * Accepts a Variable name and returns a pair containing:
     * - the name of the ThreadSpec that the Variable is defined in
     * - the VariableDefModel that defines the variable.
     * @param name is the name of the Variable to look up.
     * @return the VariableDefModel and the name of the ThreadSpec it comes from.
     */
    public Pair<String, ThreadVarDefModel> lookupVarDef(String name) {
        ThreadVarDefModel varDef = getVd(name);
        if (varDef != null) {
            return Pair.of(this.name, varDef);
        }

        return wfSpec.lookupVarDef(name);
    }

    public void validate(MetadataCommandExecution ctx) throws LHApiException {
        if (entrypointNodeName == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "missing ENTRYPOITNT node!");
        }

        boolean seenEntrypoint = false;
        for (NodeModel node : nodes.values()) {
            for (String varName : node.getRequiredVariableNames()) {
                // TODO: as per popular demand, we will relax this constraint.
                Pair<String, ThreadVarDefModel> result = lookupVarDef(varName);
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
                node.validate(ctx);
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix("Node " + node.name);
            }
        }

        for (InterruptDefModel idef : interruptDefs) {
            try {
                idef.validate();
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix(
                        "Interrupt Def for " + idef.getExternalEventDefId().getName());
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
            String eedn = idef.getExternalEventDefId().getName();
            if (wfSpec.getNodeExternalEventDefs().contains(eedn)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "ExternalEventDef " + eedn + " used for Node and Interrupt!");
            }

            for (ThreadSpecModel tspec : wfSpec.threadSpecs.values()) {
                if (tspec.name.equals(name)) continue;

                if (tspec.getInterruptExternalEventDefs().contains(eedn)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "ExternalEventDef " + eedn + " used by multiple threads as interrupt!");
                }
            }
        }
    }

    // TODO: check input variables.
    public void validateStartVariables(Map<String, VariableValueModel> vars) throws LHValidationError {
        for (Map.Entry<String, ThreadVarDefModel> e : getInputVariableDefs().entrySet()) {
            String varName = e.getKey();
            ThreadVarDefModel threadVarDef = e.getValue();
            VariableValueModel val = vars.get(varName);
            VariableDefModel varDef = threadVarDef.getVarDef();
            if (val == null) {
                if (threadVarDef.isRequired()) {
                    throw new LHValidationError(
                            "Must provide required input variable %s of type %s".formatted(varName, varDef.getType()));
                }
                log.debug("Variable {} not provided, defaulting to null", varName);
                continue;
            }

            if (val.getType() != varDef.getType() && val.getType() != null) {
                throw new LHValidationError(
                        "Var " + varName + " should be " + varDef.getType() + " but is " + val.getType());
            }

            if (threadVarDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) {
                if (vars.containsKey(varName)) {
                    throw new LHValidationError(
                            "Variable %s is an inherited var but it was provided as input".formatted(varName));
                }
            }
        }

        for (Map.Entry<String, VariableValueModel> e : vars.entrySet()) {
            String varName = e.getKey();
            if (getVd(varName) == null) {
                throw new LHValidationError("Var " + varName + " provided but not needed for thread " + name);
            }
        }
    }

    public void validateTimeoutAssignment(String nodeName, VariableAssignmentModel timeoutSeconds)
            throws LHValidationError {
        if (timeoutSeconds.getRhsSourceType() == SourceCase.VARIABLE_NAME) {
            Pair<String, ThreadVarDefModel> defPair = lookupVarDef(timeoutSeconds.getVariableName());
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
        Map<String, ThreadVarDefModel> inputVarDefs = getInputVariableDefs();

        for (Map.Entry<String, ThreadVarDefModel> e : inputVarDefs.entrySet()) {
            VariableAssignmentModel assn = vars.get(e.getKey());
            if (assn == null) {
                // It will be created as NULL for the input.
                continue;
            }

            if (!assn.canBeType(e.getValue().getVarDef().getType(), this)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Var " + e.getKey() + " should be "
                                + e.getValue().getVarDef().getType());
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
            if (idef.getExternalEventDefId().getName().equals(externalEventDefName)) {
                return idef;
            }
        }
        return null;
    }

    public ThreadVarDefModel localGetVarDef(String name) {
        for (ThreadVarDefModel vd : variableDefs) {
            if (vd.getVarDef().getName().equals(name)) {
                return vd;
            }
        }
        return null;
    }

    public ThreadVarDefModel getVarDef(String varName) {
        // This is tricky...
        ThreadVarDefModel out = localGetVarDef(varName);
        if (out != null) return out;

        Pair<String, ThreadVarDefModel> result = wfSpec.lookupVarDef(varName);
        if (result != null) {
            return result.getRight();
        } else {
            return null;
        }
    }

    public static ThreadSpecModel fromProto(ThreadSpec p, ExecutionContext context) {
        ThreadSpecModel out = new ThreadSpecModel();
        out.initFrom(p, context);
        return out;
    }
}
