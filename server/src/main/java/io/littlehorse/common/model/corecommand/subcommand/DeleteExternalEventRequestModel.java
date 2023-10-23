package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.DeleteExternalEventRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public boolean hasResponse() {
        return true;
    }

    public Empty process(CoreProcessorDAO dao, LHServerConfig config, String tenantId) {
        ExternalEventModel externalEvent = dao.get(id);
        if (!externalEvent.claimed) {
            dao.delete(id);
            return Empty.getDefaultInstance();
        } else {
            throw new LHApiException(Status.FAILED_PRECONDITION, "ExternalEvent already claimed!");
        }
    }

    public void initFrom(Message proto) {
        DeleteExternalEventRequest p = (DeleteExternalEventRequest) proto;
        id = LHSerializable.fromProto(p.getId(), ExternalEventIdModel.class);
    }

    public static DeleteExternalEventRequestModel fromProto(DeleteExternalEventRequest p) {
        DeleteExternalEventRequestModel out = new DeleteExternalEventRequestModel();
        out.initFrom(p);
        return out;
    }
}
