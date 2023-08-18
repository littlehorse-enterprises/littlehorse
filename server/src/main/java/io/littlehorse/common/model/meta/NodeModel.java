package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.subnode.EntrypointNodeModel;
import io.littlehorse.common.model.meta.subnode.ExitNodeModel;
import io.littlehorse.common.model.meta.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.meta.subnode.NopNodeModel;
import io.littlehorse.common.model.meta.subnode.SleepNodeModel;
import io.littlehorse.common.model.meta.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.meta.subnode.TaskNodeModel;
import io.littlehorse.common.model.meta.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.sdk.common.proto.VariableMutation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class NodeModel extends LHSerializable<Node> {

    public NodeCase type;
    public TaskNodeModel taskNode;
    public ExternalEventNodeModel externalEventNode;
    public EntrypointNodeModel entrypointNode;
    public ExitNodeModel exitNode;
    public StartThreadNodeModel startThreadNode;
    public WaitForThreadsNodeModel waitForThreadsNode;
    public NopNodeModel nop;
    public SleepNodeModel sleepNode;
    public UserTaskNodeModel userTaskNode;

    public List<VariableMutationModel> variableMutations;

    public List<FailureHandlerDefModel> failureHandlers;

    public Class<Node> getProtoBaseClass() {
        return Node.class;
    }

    public Node.Builder toProto() {
        Node.Builder out = Node.newBuilder();

        for (EdgeModel o : outgoingEdges) {
            out.addOutgoingEdges(o.toProto());
        }

        for (VariableMutationModel v : variableMutations) {
            out.addVariableMutations(v.toProto());
        }

        for (FailureHandlerDefModel eh : failureHandlers) {
            out.addFailureHandlers(eh.toProto());
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
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventNode.toProto());
                break;
            case START_THREAD:
                out.setStartThread(startThreadNode.toProto());
                break;
            case WAIT_FOR_THREADS:
                out.setWaitForThreads(waitForThreadsNode.toProto());
                break;
            case NOP:
                out.setNop(NopNode.newBuilder());
                break;
            case SLEEP:
                out.setSleep(sleepNode.toProto());
                break;
            case USER_TASK:
                out.setUserTask(userTaskNode.toProto());
                break;
            case NODE_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        return out;
    }

    public void initFrom(Message p) {
        Node proto = (Node) p;
        type = proto.getNodeCase();

        for (Edge epb : proto.getOutgoingEdgesList()) {
            EdgeModel edge = EdgeModel.fromProto(epb);
            edge.threadSpecModel = threadSpecModel;
            outgoingEdges.add(edge);
        }

        for (VariableMutation vmpb : proto.getVariableMutationsList()) {
            VariableMutationModel vm = new VariableMutationModel();
            vm.initFrom(vmpb);
            variableMutations.add(vm);
        }

        for (FailureHandlerDef ehpb : proto.getFailureHandlersList()) {
            failureHandlers.add(FailureHandlerDefModel.fromProto(ehpb));
        }

        switch (type) {
            case TASK:
                taskNode = new TaskNodeModel();
                taskNode.initFrom(proto.getTask());
                break;
            case ENTRYPOINT:
                entrypointNode = new EntrypointNodeModel();
                entrypointNode.initFrom(proto.getEntrypoint());
                break;
            case EXIT:
                exitNode = new ExitNodeModel();
                exitNode.initFrom(proto.getExit());
                break;
            case EXTERNAL_EVENT:
                externalEventNode = new ExternalEventNodeModel();
                externalEventNode.initFrom(proto.getExternalEvent());
                break;
            case START_THREAD:
                startThreadNode = new StartThreadNodeModel();
                startThreadNode.initFrom(proto.getStartThread());
                break;
            case WAIT_FOR_THREADS:
                waitForThreadsNode = new WaitForThreadsNodeModel();
                waitForThreadsNode.initFrom(proto.getWaitForThreads());
                break;
            case NOP:
                nop = new NopNodeModel();
                break;
            case SLEEP:
                sleepNode = new SleepNodeModel();
                sleepNode.initFrom(proto.getSleep());
                break;
            case USER_TASK:
                userTaskNode =
                        LHSerializable.fromProto(proto.getUserTask(), UserTaskNodeModel.class);
                break;
            case NODE_NOT_SET:
                throw new RuntimeException(
                        "Node " + name + " on thread " + threadSpecModel.name + " is unset!");
        }
        getSubNode().setNode(this);
    }

    // Implementation details below

    public NodeModel() {
        outgoingEdges = new ArrayList<>();
        variableMutations = new ArrayList<>();
        failureHandlers = new ArrayList<>();
    }

    public List<EdgeModel> outgoingEdges;
    public String name;

    public ThreadSpecModel threadSpecModel;

    public Set<String> neededVariableNames() {
        Set<String> out = new HashSet<>();

        for (VariableMutationModel mut : variableMutations) {
            out.add(mut.lhsName);
            if (mut.rhsSourceVariable != null) {
                if (mut.rhsSourceVariable.getVariableName() != null) {
                    out.add(mut.rhsSourceVariable.getVariableName());
                }
            }
        }

        out.addAll(getSubNode().getNeededVariableNames());

        return out;
    }

    public void validate(LHGlobalMetaStores client, LHConfig config) throws LHValidationError {
        for (EdgeModel e : outgoingEdges) {
            if (e.sinkNodeName.equals(name)) {
                throw new LHValidationError(null, "Self loop not allowed!");
            }

            NodeModel sink = threadSpecModel.nodes.get(e.sinkNodeName);
            if (sink == null) {
                throw new LHValidationError(
                        null,
                        String.format(
                                "Outgoing edge referring to missing node %s!", e.sinkNodeName));
            }

            if (sink.type == NodeCase.ENTRYPOINT) {
                throw new LHValidationError(
                        null,
                        String.format(
                                "Entrypoint node has incoming edge from node %s.",
                                threadSpecModel.name, name));
            }
            if (e.condition != null) {
                e.condition.validate();
            }
        }

        if (!outgoingEdges.isEmpty()) {
            EdgeModel last = outgoingEdges.get(outgoingEdges.size() - 1);
            if (last.condition != null) {
                // throw new LHValidationError(
                //     null,
                //     "Last outgoing edge has non-null condition!"
                // );

                log.warn(
                        """
                        There is no default edge, better know what you're doing!.
                        Future releases will validate that everything is ok.
                    """);
            }
        }

        try {
            getSubNode().validate(client, config);
        } catch (LHValidationError exn) {
            // Decorate the exception with contextual info
            exn.addPrefix("Sub Node");
            throw exn;
        }
    }

    public SubNode<?> getSubNode() {
        switch (type) {
            case TASK:
                return taskNode;
            case ENTRYPOINT:
                return entrypointNode;
            case EXIT:
                return exitNode;
            case EXTERNAL_EVENT:
                return externalEventNode;
            case START_THREAD:
                return startThreadNode;
            case WAIT_FOR_THREADS:
                return waitForThreadsNode;
            case NOP:
                return nop;
            case SLEEP:
                return sleepNode;
            case USER_TASK:
                return userTaskNode;
            case NODE_NOT_SET:
        }
        throw new RuntimeException("incomplete switch statement");
    }

    /**
     * Returns the set of all thread variable names referred to by this Node. Used internally for
     * validation of the WfSpec.
     */
    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableMutationModel mut : variableMutations) {
            out.addAll(mut.getRequiredVariableNames());
        }

        for (EdgeModel edge : outgoingEdges) {
            out.addAll(edge.getRequiredVariableNames());
        }

        out.addAll(getSubNode().getNeededVariableNames());

        return out;
    }
}
