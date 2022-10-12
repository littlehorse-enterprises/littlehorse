package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.VariableMutation;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

// TODO: I don't think this should be GETable. Maybe just LHSerializable.
public class ThreadRun extends LHSerializable<ThreadRunPb> {

    public String wfRunId;
    public int number;

    public LHStatusPb status;
    public String wfSpecId;
    public String threadSpecName;
    public int currentNodePosition;

    public Date startTime;
    public Date endTime;

    public String errorMessage;
    public TaskResultCodePb resultCode;

    public ThreadRun() {
        variables = new HashMap<>();
    }

    public void initFrom(MessageOrBuilder p) {
        ThreadRunPb proto = (ThreadRunPb) p;
        wfRunId = proto.getWfRunId();
        number = proto.getNumber();
        status = proto.getStatus();
        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        currentNodePosition = proto.getCurrentNodePosition();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
        if (proto.hasErrorMessage()) {
            errorMessage = proto.getErrorMessage();
        }
        if (proto.hasResultCode()) {
            resultCode = proto.getResultCode();
        }
    }

    public ThreadRunPb.Builder toProto() {
        ThreadRunPb.Builder out = ThreadRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setNumber(number)
            .setStatus(status)
            .setWfSpecId(wfSpecId)
            .setThreadSpecName(threadSpecName)
            .setCurrentNodePosition(currentNodePosition)
            .setStartTime(LHUtil.fromDate(startTime));

        if (resultCode != null) {
            out.setResultCode(resultCode);
        }

        if (errorMessage != null) {
            out.setErrorMessage(errorMessage);
        }

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        return out;
    }

    public static ThreadRun fromProto(MessageOrBuilder p) {
        ThreadRun out = new ThreadRun();
        out.initFrom(p);
        return out;
    }

    public Class<ThreadRunPb> getProtoBaseClass() {
        return ThreadRunPb.class;
    }

    // For Scheduler
    @JsonIgnore
    public WfRun wfRun;

    @JsonIgnore
    public Map<String, Variable> variables;

    @JsonIgnore
    private ThreadSpec threadSpec;

    @JsonIgnore
    private Variable getVariable(
        String varName,
        ReadOnlyKeyValueStore<String, Variable> store
    ) {
        Variable out = variables.get(varName);
        if (out == null) {
            out = store.get(varName);
            variables.put(varName, out);
        }
        return out;
    }

    @JsonIgnore
    public ThreadSpec getThreadSpec() {
        if (threadSpec == null) {
            threadSpec = wfRun.wfSpec.threadSpecs.get(threadSpecName);
        }
        return threadSpec;
    }

    @JsonIgnore
    public Node getCurrentNode() {
        NodeRun currRun = getCurrentNodeRun();
        ThreadSpec t = getThreadSpec();
        if (currRun == null) {
            return t.nodes.get(t.entrypointNodeName);
        }

        return t.nodes.get(currRun.nodeName);
    }

    @JsonIgnore
    public NodeRun getCurrentNodeRun() {
        return getNodeRun(currentNodePosition);
    }

    public void processEvent(WfRunEvent e) {
        if (e.getThreadRunNumber() == null || e.getThreadRunNumber() != number) {
            LHUtil.log("Ignoring event for different thread");
            return;
        }

        /*
         * WfRunEvents can be any of the following:
         * - WF_RUN_REQUEST
         * - TASK_STARTED
         * - TASK_RESULT
         * - EXTERNAL_EVENT
         */

        Integer nrpos = e.getNodeRunPosition();
        if (nrpos != null) {
            if (nrpos > currentNodePosition) {
                LHUtil.log("Got event for unknown future node. Skipping.");
                return;
            }
            getNodeRun(nrpos).processEvent(e);
        } else {
            // In the future, we may have things like Interrupts or STOP_REQUEST
            // in which it's assigned to a Thread but not a Node.
            LHUtil.log("Got an event without assigned to node. Skipping.");
        }
    }

    @JsonIgnore
    public void advance(Date eventTime) {
        NodeRun currentNodeRun = getCurrentNodeRun();

        if (status == LHStatusPb.RUNNING) {
            // Just advance the node. Not fancy.
            currentNodeRun.advanceIfPossible(eventTime);
        } else if (status == LHStatusPb.HALTED) {
            // This means we just need to wait until advance() is called again
            // after Thread Resumption

            LHUtil.log("Tried to advance HALTED thread. Doing nothing.");
        } else if (status == LHStatusPb.HALTING) {
            // TODO: Decide whether we want to pull this out into a method
            // like `ThreadRun::updateStatus()` or keep it here

            LHUtil.log("Tried to advance HALTING thread, checking if halted yet.");

            if (!currentNodeRun.isInProgress()) {
                status = LHStatusPb.HALTED;
                LHUtil.log("Moving thread to HALTED");
            }
        } else if (status == LHStatusPb.COMPLETED) {
            // Nothing to do, this is likely an innocuous event.
        } else if (status == LHStatusPb.ERROR) {
            // This is innocuous. Occurs when a timeout event comes in after
            // a thread fails or completes. Nothing to do.
        } else if (status == LHStatusPb.STARTING) {
            status = LHStatusPb.RUNNING;
            currentNodeRun.advanceIfPossible(eventTime);
        } else {
            throw new RuntimeException("Unrecognized status: " + status);
        }
    }

    public void fail(TaskResultCodePb resultCode, String msg, Date time) {
        this.resultCode = resultCode;
        this.errorMessage = msg;
        status = LHStatusPb.ERROR;
        this.endTime = time;

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void complete(Date time) {
        this.resultCode = TaskResultCodePb.SUCCESS;
        this.errorMessage = null;
        status = LHStatusPb.COMPLETED;
        endTime = time;

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void completeCurrentNode(VariableValue output, Date eventTime) {
        NodeRun crn = getCurrentNodeRun();
        crn.status = LHStatusPb.COMPLETED;

        try {
            mutateVariables(output);
        } catch (LHVarSubError exn) {
            fail(
                TaskResultCodePb.VAR_MUTATION_ERROR,
                "Failed mutating variables: " + exn.getMessage(),
                eventTime
            );
            // TODO: Maybe explicitly call `failThread()` here
            return;
        }

        // If we got here, then we're good.
        advanceFrom(getCurrentNode());
    }

    public void advanceFrom(Node curNode) {
        Node nextNode = null;
        for (Edge e : curNode.outgoingEdges) {
            try {
                if (evaluateEdge(e)) {
                    nextNode = e.getSinkNode();
                    break;
                }
            } catch (LHVarSubError exn) {
                LHUtil.log(
                    "Failing threadrun due to VarSubError",
                    wfRun.id,
                    currentNodePosition
                );
                fail(
                    TaskResultCodePb.VAR_MUTATION_ERROR,
                    "Failed evaluating outgoing edge: " + exn.getMessage(),
                    new Date()
                );
                return;
            }
        }
        if (nextNode == null) {
            throw new RuntimeException(
                "Not possible to have a node with zero activated edges"
            );
        }

        activateNode(nextNode);
    }

    public void activateNode(Node node) {
        Date arrivalTime = new Date();

        currentNodePosition++;
        NodeRun old = getCurrentNodeRun();
        Node oldNode = old == null ? null : old.getNode();

        NodeRun cnr = new NodeRun();
        cnr.threadRun = this;
        cnr.nodeName = node.name;
        cnr.status = LHStatusPb.STARTING;

        cnr.wfRunId = wfRunId;
        cnr.threadRunNumber = number;
        cnr.position = currentNodePosition;
        cnr.threadSpecName = threadSpecName;

        cnr.arrivalTime = arrivalTime;
        cnr.wfSpecId = wfSpecId;
        cnr.nodeName = node.name;

        cnr.position = currentNodePosition;

        if (oldNode == null || old == null) {
            // then it's a start node
            cnr.number = 0;
            cnr.attemptNumber = 0;
        } else if (oldNode.name.equals(node.name)) {
            // Then it's a retry, since we don't allow edges pointing to the same
            // node.
            cnr.number = old.number;
            cnr.attemptNumber = old.attemptNumber + 1;
        } else {
            // Not a retry, so it's a new "number" and attemptNumber is 0.
            cnr.number = old.number + 1;
            cnr.attemptNumber = 0;
        }
        cnr.setSubNodeRun(node.getSubNode().createRun(arrivalTime));

        putNodeRun(cnr);

        cnr.getSubNodeRun().arrive(arrivalTime);
        cnr.getSubNodeRun().advanceIfPossible(arrivalTime);
    }

    private boolean evaluateEdge(Edge e) throws LHVarSubError {
        if (e.condition == null) {
            return true;
        }

        VariableValue lhs = assignVariable(e.condition.left);
        VariableValue rhs = assignVariable(e.condition.right);

        switch (e.condition.comparator) {
            case LESS_THAN:
                return Comparer.compare(lhs, rhs) < 0;
            case LESS_THAN_EQ:
                return Comparer.compare(lhs, rhs) <= 0;
            case GREATER_THAN:
                return Comparer.compare(lhs, rhs) > 0;
            case GREATER_THAN_EQ:
                return Comparer.compare(lhs, rhs) >= 0;
            case EQUALS:
                return lhs != null && lhs.equals(rhs);
            case NOT_EQUALS:
                return lhs != null && !lhs.equals(rhs);
            case IN:
                return Comparer.contains(rhs, lhs);
            case NOT_IN:
                return !Comparer.contains(rhs, lhs);
            default:
                throw new RuntimeException(
                    "Unhandled comparison enum " + e.condition.comparator
                );
        }
    }

    // TODO: variable locking may have to consider this.
    public Map<String, VariableValue> assignVarsForNode(Node node)
        throws LHVarSubError {
        Map<String, VariableValue> out = new HashMap<>();
        TaskDef taskDef = node.taskNode.taskDef;

        for (Map.Entry<String, VariableDef> entry : taskDef.requiredVars.entrySet()) {
            String varName = entry.getKey();
            VariableDef requiredVarDef = entry.getValue();
            VariableAssignment assn = node.taskNode.variables.get(varName);
            VariableValue val;

            if (assn != null) {
                val = assignVariable(assn);
            } else {
                if (requiredVarDef.defaultValue == null) {
                    throw new LHVarSubError(
                        null,
                        "Variable " +
                        varName +
                        " neither has default value nor assignment."
                    );
                }
                val = requiredVarDef.defaultValue;
            }
            if (val.type != requiredVarDef.type) {
                throw new LHVarSubError(
                    null,
                    "Variable " +
                    varName +
                    " should be " +
                    requiredVarDef.type +
                    " but is of type " +
                    val.type
                );
            }
            out.put(varName, val);
        }
        return out;
    }

    // public void setStatus(
    //     LHStatusPb newStatus,
    //     String errorMessage,
    //     TaskResultCodePb code
    // ) {
    //     status = newStatus;
    //     resultCode = code;
    //     this.errorMessage = errorMessage;

    //     Date time = new Date();

    //     wfRun.handleThreadStatus(number, time, newStatus);
    // }

    private void mutateVariables(VariableValue nodeOutput) throws LHVarSubError {
        Node node = getCurrentNode();

        // Need to do this atomically in a transaction, so that if one of the
        // mutations fail then none of them occur.
        // That's why we write to an in-memory Map. If all mutations succeed,
        // then we flush the contents of the Map to the Variables.
        Map<String, VariableValue> varCache = new HashMap<>();
        for (VariableMutation mut : node.variableMutations) {
            mut.execute(this, varCache, nodeOutput);
        }

        // If we got this far without a LHVarSubError, then we can safely save all
        // of the variables.
        for (Map.Entry<String, VariableValue> entry : varCache.entrySet()) {
            // TODO: This needs to be extended once we add Threads.
            putLocalVariable(entry.getKey(), entry.getValue());
        }
    }

    public VariableValue assignVariable(VariableAssignment assn)
        throws LHVarSubError {
        return assignVariable(assn, new HashMap<>());
    }

    public VariableValue assignVariable(
        VariableAssignment assn,
        Map<String, VariableValue> txnCache
    ) throws LHVarSubError {
        VariableValue val = null;
        switch (assn.rhsSourceType) {
            case LITERAL_VALUE:
                val = assn.rhsLiteralValue;
                break;
            case VARIABLE_NAME:
                val = getVariable(assn.rhsVariableName).value;
                break;
            case SOURCE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        if (assn.jsonPath != null) {
            val = val.jsonPath(assn.jsonPath);
        }

        return val;
    }

    public void putNodeRun(NodeRun task) {
        wfRun.stores.putNodeRun(task);
    }

    public NodeRun getNodeRun(int position) {
        NodeRun out = wfRun.stores.getNodeRun(number, position);
        if (out != null) {
            out.threadRun = this;
        }
        return out;
    }

    public void putVariable(Variable var) {
        wfRun.threadRuns
            .get(var.threadRunNumber)
            .putLocalVariable(var.name, var.value);
    }

    public Variable getVariable(String name) {
        // For now, just do the local one
        // Once we have threads, this will do a backtrack up the thread tree.
        return getLocalVariable(name);
    }

    public void putLocalVariable(String name, VariableValue var) {
        wfRun.stores.putVariable(name, var, number);
    }

    public Variable getLocalVariable(String name) {
        return wfRun.stores.getVariable(name, number);
    }
}

class Comparer {

    @SuppressWarnings("all") // lol
    public static int compare(VariableValue left, VariableValue right)
        throws LHVarSubError {
        try {
            int result =
                ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());
            return result;
        } catch (Exception exn) {
            LHUtil.log(exn.getMessage());
            throw new LHVarSubError(exn, "Failed comparing the provided values.");
        }
    }

    public static boolean contains(VariableValue left, VariableValue right)
        throws LHVarSubError {
        // Can only do for Str, Arr, and Obj

        if (left.type == VariableTypePb.STR) {
            String rStr = right.asStr().strVal;

            return left.asStr().strVal.contains(rStr);
        } else if (left.type == VariableTypePb.JSON_ARR) {
            Object rObj = right.getVal();
            List<Object> lhs = left.asArr().jsonArrVal;

            for (Object o : lhs) {
                if (LHUtil.deepEquals(o, rObj)) {
                    return true;
                }
            }
            return false;
        } else if (left.type == VariableTypePb.JSON_OBJ) {
            return left.asObj().jsonObjVal.containsKey(right.asStr().strVal);
        } else {
            throw new LHVarSubError(null, "Can't do CONTAINS on " + left.type);
        }
    }
}
