package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.DeleteExternalEventRequest;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteExternalEventRequestModel extends CoreSubCommand<DeleteExternalEventRequest> {

    private ExternalEventIdModel id;

    public String getPartitionKey() {
        return id.getPartitionKey().get();
    }

    public Class<DeleteExternalEventRequest> getProtoBaseClass() {
        return DeleteExternalEventRequest.class;
    }

    public DeleteExternalEventRequest.Builder toProto() {
        DeleteExternalEventRequest.Builder out =
                DeleteExternalEventRequest.newBuilder().setId(id.toProto());

        return out;
    }

    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        ExternalEventModel externalEvent = executionContext.getableManager().get(id);
        if (!externalEvent.isClaimed()) {
            executionContext.getableManager().delete(id);
            return Empty.getDefaultInstance();
        } else {
            throw new LHApiException(Status.FAILED_PRECONDITION, "ExternalEvent already claimed!");
        }
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteExternalEventRequest p = (DeleteExternalEventRequest) proto;
        id = LHSerializable.fromProto(p.getId(), ExternalEventIdModel.class, context);
    }

    public static DeleteExternalEventRequestModel fromProto(DeleteExternalEventRequest p, ExecutionContext context) {
        DeleteExternalEventRequestModel out = new DeleteExternalEventRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
