package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.EntrypointNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExitNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.NopNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.SleepNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartMultipleThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.UserTaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
    private StartMultipleThreadsNodeModel startMultipleThreadsNode;

    public List<FailureHandlerDefModel> failureHandlers;

    public Class<Node> getProtoBaseClass() {
        return Node.class;
    }

    public Node.Builder toProto() {
        Node.Builder out = Node.newBuilder();

        for (EdgeModel o : outgoingEdges) {
            out.addOutgoingEdges(o.toProto());
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
            case START_MULTIPLE_THREADS:
                out.setStartMultipleThreads(startMultipleThreadsNode.toProto());
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

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        Node proto = (Node) p;
        type = proto.getNodeCase();

        for (Edge epb : proto.getOutgoingEdgesList()) {
            EdgeModel edge = EdgeModel.fromProto(epb, context);
            edge.threadSpecModel = threadSpecModel;
            outgoingEdges.add(edge);
        }

        for (FailureHandlerDef ehpb : proto.getFailureHandlersList()) {
            failureHandlers.add(FailureHandlerDefModel.fromProto(ehpb, context));
        }

        switch (type) {
            case TASK:
                taskNode = new TaskNodeModel();
                taskNode.initFrom(proto.getTask(), context);
                break;
            case ENTRYPOINT:
                entrypointNode = new EntrypointNodeModel();
                entrypointNode.initFrom(proto.getEntrypoint(), context);
                break;
            case EXIT:
                exitNode = new ExitNodeModel();
                exitNode.initFrom(proto.getExit(), context);
                break;
            case EXTERNAL_EVENT:
                externalEventNode = new ExternalEventNodeModel();
                externalEventNode.initFrom(proto.getExternalEvent(), context);
                break;
            case START_THREAD:
                startThreadNode = new StartThreadNodeModel();
                startThreadNode.initFrom(proto.getStartThread(), context);
                break;
            case WAIT_FOR_THREADS:
                waitForThreadsNode = new WaitForThreadsNodeModel();
                waitForThreadsNode.initFrom(proto.getWaitForThreads(), context);
                break;
            case NOP:
                nop = new NopNodeModel();
                break;
            case SLEEP:
                sleepNode = new SleepNodeModel();
                sleepNode.initFrom(proto.getSleep(), context);
                break;
            case USER_TASK:
                userTaskNode = LHSerializable.fromProto(proto.getUserTask(), UserTaskNodeModel.class, context);
                break;
            case START_MULTIPLE_THREADS:
                startMultipleThreadsNode = LHSerializable.fromProto(
                        proto.getStartMultipleThreads(), StartMultipleThreadsNodeModel.class, context);
                break;
            case NODE_NOT_SET:
                throw new RuntimeException("Node " + name + " on thread " + threadSpecModel.name + " is unset!");
        }
        getSubNode().setNode(this);
    }

    // Implementation details below

    public NodeModel() {
        outgoingEdges = new ArrayList<>();
        failureHandlers = new ArrayList<>();
    }

    public List<EdgeModel> outgoingEdges;
    public String name;

    public ThreadSpecModel threadSpecModel;

    public void validate() throws LHApiException {
        for (EdgeModel e : outgoingEdges) {
            if (e.sinkNodeName.equals(name)) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "Self loop not allowed!");
            }

            NodeModel sink = threadSpecModel.nodes.get(e.sinkNodeName);
            if (sink == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        String.format("Outgoing edge referring to missing node %s!", e.sinkNodeName));
            }

            if (sink.type == NodeCase.ENTRYPOINT) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        String.format("Entrypoint node has incoming edge from node %s.", threadSpecModel.name, name));
            }
            if (e.condition != null) {
                e.condition.validate();
            }
        }

        if (!outgoingEdges.isEmpty()) {
            EdgeModel last = outgoingEdges.get(outgoingEdges.size() - 1);
            if (last.condition != null) {
                // throw new LHValidationError(
                // null,
                // "Last outgoing edge has non-null condition!"
                // );

                log.warn(
                        """
                                    There is no default edge, better know what you're doing!.
                                    Future releases will validate that everything is ok.
                                """);
            }
        }

        try {
            getSubNode().validate();
        } catch (LHApiException exn) {
            // Decorate the exception with contextual info
            throw exn.getCopyWithPrefix("Sub Node");
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
            case START_MULTIPLE_THREADS:
                return startMultipleThreadsNode;
            case NODE_NOT_SET:
        }
        throw new RuntimeException("incomplete switch statement");
    }

    /**
     * Returns the set of all thread variable names referred to by this Node. Used
     * internally for
     * validation of the WfSpec.
     */
    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();

        for (EdgeModel edge : outgoingEdges) {
            out.addAll(edge.getRequiredVariableNames());
        }

        out.addAll(getSubNode().getNeededVariableNames());

        return out;
    }
}
