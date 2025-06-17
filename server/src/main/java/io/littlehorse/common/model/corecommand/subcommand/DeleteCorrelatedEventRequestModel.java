package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteCorrelatedEventRequestModel extends CoreSubCommand<DeleteCorrelatedEventRequest> {

    private CorrelatedEventIdModel id;

    @Override
    public Class<DeleteCorrelatedEventRequest> getProtoBaseClass() {
        return DeleteCorrelatedEventRequest.class;
    }

    @Override
    public DeleteCorrelatedEventRequest.Builder toProto() {
        DeleteCorrelatedEventRequest.Builder result =
                DeleteCorrelatedEventRequest.newBuilder().setId(id.toProto());
        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        DeleteCorrelatedEventRequest p = (DeleteCorrelatedEventRequest) proto;
        id = LHSerializable.fromProto(p.getId(), CorrelatedEventIdModel.class, ctx);
    }

    @Override
    public Empty process(CoreProcessorContext ctx, LHServerConfig config) {
        GetableManager manager = ctx.getableManager();

        CorrelatedEventModel correlatedEvent = manager.delete(id);
        if (correlatedEvent == null) {
            log.trace("correlated event {} was already deleted or never existed", id);
        } else {
            log.trace("successfully deleted correlated event {}", id);
        }

        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return id.getPartitionKey().get();
    }
}
