package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForThreadsRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.node.ThreadToWaitForModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ThreadsToWaitForModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.FailureHandlerDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadsToWaitForCase;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class WaitForThreadsNodeModel extends SubNode<WaitForThreadsNode> {

    private ExecutionContext context;

    private ThreadsToWaitForCase type;
    private ThreadsToWaitForModel threads;
    private VariableAssignmentModel threadList;

    private List<FailureHandlerDefModel> perThreadFailureHandlers;

    public Class<WaitForThreadsNode> getProtoBaseClass() {
        return WaitForThreadsNode.class;
    }

    public WaitForThreadsNodeModel() {
        perThreadFailureHandlers = new ArrayList<>();
    }

    public void initFrom(Message proto, ExecutionContext context) {
        this.context = context;

        WaitForThreadsNode p = (WaitForThreadsNode) proto;
        type = p.getThreadsToWaitForCase();

        switch (type) {
            case THREADS:
                threads = LHSerializable.fromProto(p.getThreads(), ThreadsToWaitForModel.class, context);
                break;
            case THREAD_LIST:
                threadList = VariableAssignmentModel.fromProto(p.getThreadList(), context);
                break;
            case THREADSTOWAITFOR_NOT_SET:
                log.warn("should be impossible to get unset threadsToWaitFor");
        }

        for (FailureHandlerDef handler : p.getPerThreadFailureHandlersList()) {
            perThreadFailureHandlers.add(FailureHandlerDefModel.fromProto(handler, context));
        }
    }

    public WaitForThreadsNode.Builder toProto() {
        WaitForThreadsNode.Builder out = WaitForThreadsNode.newBuilder();
        switch (type) {
            case THREADS:
                out.setThreads(threads.toProto());
                break;
            case THREAD_LIST:
                out.setThreadList(threadList.toProto());
                break;
            case THREADSTOWAITFOR_NOT_SET:
                log.warn("should be impossible to get unset threadsToWaitFor");
        }

        for (FailureHandlerDefModel handler : perThreadFailureHandlers) {
            out.addPerThreadFailureHandlers(handler.toProto());
        }
        return out;
    }

    @Override
    public WaitForThreadsRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new WaitForThreadsRunModel();
    }

    public List<WaitForThreadModel> getThreadsToWaitFor(
            NodeRunModel nodeRun, Date currentCommandTime, CoreProcessorContext context)
            throws LHVarSubError, NodeFailureException {
        ThreadRunModel thread = nodeRun.getThreadRun();
        List<WaitForThreadModel> out = new ArrayList<>();

        switch (type) {
            case THREADS:
                for (ThreadToWaitForModel ttwf : threads.getThreads()) {
                    int threadRunNumber = thread.assignVariable(ttwf.getThreadRunNumber())
                            .asInt()
                            .getIntVal()
                            .intValue();
                    out.add(new WaitForThreadModel(nodeRun, threadRunNumber, currentCommandTime, context));
                }
                break;
            case THREAD_LIST:
                VariableValueModel threadListVar = thread.assignVariable(threadList);

                for (Object threadNumberObj : threadListVar.getJsonArrVal()) {
                    out.add(new WaitForThreadModel(
                            nodeRun, Integer.valueOf(threadNumberObj.toString()), currentCommandTime, context));
                }
                break;
            case THREADSTOWAITFOR_NOT_SET:
                log.warn("Should be impossible to have unset threadsToWaitFor");
        }
        return out;
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();

        switch (type) {
            case THREADS:
                for (ThreadToWaitForModel ttwf : threads.getThreads()) {
                    out.addAll(ttwf.getThreadRunNumber().getRequiredWfRunVarNames());
                }
                break;
            case THREAD_LIST:
                out.addAll(threadList.getRequiredWfRunVarNames());
                break;
            case THREADSTOWAITFOR_NOT_SET:
                log.warn("Should be impossible");
        }

        return out;
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws LHApiException {
        switch (type) {
            case THREADS:
                for (ThreadToWaitForModel ttwf : threads.getThreads()) {
                    if (!ttwf.getThreadRunNumber().canBeType(VariableType.INT, node.getThreadSpec())) {
                        throw new LHApiException(
                                Status.INVALID_ARGUMENT,
                                "`threadRunNumber` for WAIT_FOR_THREAD node must resolve to INT!");
                    }
                }
                break;
            case THREAD_LIST:
                if (!threadList.canBeType(VariableType.JSON_ARR, node.getThreadSpec())) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "`threadRunNumber` for WAIT_FOR_THREAD node must resolve to JSON_ARR!");
                }
                break;
            case THREADSTOWAITFOR_NOT_SET:
                log.warn("Should be impossible");
        }
    }
}
