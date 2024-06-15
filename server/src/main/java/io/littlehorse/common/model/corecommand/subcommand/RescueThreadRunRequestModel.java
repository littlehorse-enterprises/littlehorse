package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.RescueThreadRunRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class RescueThreadRunRequestModel extends CoreSubCommand<RescueThreadRunRequest> {

    private WfRunIdModel wfRunId;
    private int threadRunNumber;
    private boolean skipCurrentNode;

    @Override
    public Class<RescueThreadRunRequest> getProtoBaseClass() {
        return RescueThreadRunRequest.class;
    }

    @Override
    public RescueThreadRunRequest.Builder toProto() {
        return RescueThreadRunRequest.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setThreadRunNumber(threadRunNumber)
                .setSkipCurrentNode(skipCurrentNode);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        RescueThreadRunRequest p = (RescueThreadRunRequest) proto;
        this.wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, ctx);
        this.threadRunNumber = p.getThreadRunNumber();
        this.skipCurrentNode = p.getSkipCurrentNode();
    }

    @Override
    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public WfRun process(ProcessorExecutionContext ctx, LHServerConfig config) {
        GetableManager getableManager = ctx.getableManager();
        WfRunModel wfRun = getableManager.get(wfRunId);
        if (wfRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WfRun %s".formatted(wfRunId));
        }

        Pair<Boolean, Status> canRescueAndStatus = wfRun.canRescueThreadRun(threadRunNumber, ctx);
        boolean canRescue = canRescueAndStatus.getKey();
        if (!canRescue) {
            Status errorStatus = canRescueAndStatus.getRight();
            throw new LHApiException(errorStatus);
        }

        Optional<Status> statusToThrow = wfRun.rescueThreadRun(threadRunNumber, skipCurrentNode, ctx);
        if (statusToThrow.isPresent()) {
            throw new LHApiException(statusToThrow.get());
        }

        return wfRun.toProto().build();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
