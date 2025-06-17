package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.MissingThreadRunException;
import io.littlehorse.common.exceptions.ThreadRunRescueFailedException;
import io.littlehorse.common.exceptions.UnRescuableThreadRunException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.RescueThreadRunRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

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
    public WfRun process(CoreProcessorContext ctx, LHServerConfig config) {
        GetableManager getableManager = ctx.getableManager();
        WfRunModel wfRun = getableManager.get(wfRunId);
        if (wfRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WfRun %s".formatted(wfRunId));
        }

        try {
            wfRun.rescueThreadRun(threadRunNumber, skipCurrentNode, ctx);
        } catch (MissingThreadRunException exn) {
            throw exn.toLHApiException();
        } catch (UnRescuableThreadRunException exn) {
            throw exn.toLHApiException();
        } catch (ThreadRunRescueFailedException exn) {
            throw exn.toLHApiException();
        } catch (Exception exn) {
            throw new LHApiException(Status.INTERNAL, exn.getMessage());
        }

        return wfRun.toProto().build();
    }
}
