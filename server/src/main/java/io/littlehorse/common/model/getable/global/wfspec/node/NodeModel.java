package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.InvalidEdgeException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.EntrypointNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExitNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.NopNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.RunChildWfNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.SleepNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartMultipleThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.StartThreadNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ThrowEventNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.UserTaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForConditionNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

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
    private ThrowEventNodeModel throwEventNode;
    private WaitForConditionNodeModel waitForConditionNode;
    private RunChildWfNodeModel runChildWfNode;
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
            case THROW_EVENT:
                out.setThrowEvent(throwEventNode.toProto());
                break;
            case WAIT_FOR_CONDITION:
                out.setWaitForCondition(waitForConditionNode.toProto());
                break;
            case RUN_CHILD_WF:
                out.setRunChildWf(runChildWfNode.toProto());
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
            edge.setThreadSpecModel(threadSpec);
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
            case THROW_EVENT:
                throwEventNode = LHSerializable.fromProto(proto.getThrowEvent(), ThrowEventNodeModel.class, context);
                break;
            case WAIT_FOR_CONDITION:
                waitForConditionNode =
                        LHSerializable.fromProto(proto.getWaitForCondition(), WaitForConditionNodeModel.class, context);
                break;
            case RUN_CHILD_WF:
                this.runChildWfNode =
                        LHSerializable.fromProto(proto.getRunChildWf(), RunChildWfNodeModel.class, context);
                break;
            case NODE_NOT_SET:
                throw new RuntimeException("Node " + name + " on thread " + threadSpec.name + " is unset!");
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

    public ThreadSpecModel threadSpec;

    public Optional<FailureHandlerDefModel> getHandlerFor(FailureModel failure) {
        for (FailureHandlerDefModel handler : failureHandlers) {
            if (handler.doesHandle(failure.getFailureName())) {
                return Optional.of(handler);
            }
        }
        return Optional.empty();
    }

    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        getSubNode().validate(ctx);
        // This can throw an exception, so let's call it here to catch it early on.
        try {
            getOutputType(ctx.metadataManager());
        } catch (InvalidExpressionException exn) {
            throw new InvalidNodeException("Invalid output type for the Node: " + exn.getMessage(), this);
        }

        for (EdgeModel e : outgoingEdges) {
            try {
                e.validate(this, ctx.metadataManager(), threadSpec);
            } catch (InvalidEdgeException exn) {
                throw new InvalidNodeException(exn.getMessage(), this);
            }
        }
        validateFailureHandlers();
    }

    private void validateFailureHandlers() {
        List<String> predefinedErrors =
                Arrays.stream(LHErrorType.values()).map(LHErrorType::toString).toList();
        String invalidNames = failureHandlers.stream()
                .map(FailureHandlerDefModel::getSpecificFailure)
                .filter(Objects::nonNull)
                .filter(failureName -> !predefinedErrors.contains(failureName))
                .filter(Predicate.not(LHUtil::isValidLHName))
                .collect(Collectors.joining(", "));
        if (!invalidNames.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Invalid names for exception handlers: " + invalidNames);
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
            case THROW_EVENT:
                return throwEventNode;
            case WAIT_FOR_CONDITION:
                return waitForConditionNode;
            case RUN_CHILD_WF:
                return runChildWfNode;
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

    /**
     * The Output Type of a node has three cases:
     *
     * 1. The root optional is empty. This means that we do not know what the node returns—could be empty, could
     *    be something.
     * 2. The root optional specifies an empty TypeDefinition. This means that the Node doesn't return anything.
     * 3. The TypeDefinition is set. This means that we *do* know what the Node returns.
     */
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) throws InvalidExpressionException {
        return getSubNode().getOutputType(manager);
    }
}
