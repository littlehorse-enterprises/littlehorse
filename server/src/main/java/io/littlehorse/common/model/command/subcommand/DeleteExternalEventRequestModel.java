package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.sdk.common.proto.DeleteExternalEventRequest;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class DeleteExternalEventRequestModel extends SubCommand<DeleteExternalEventRequest> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<DeleteExternalEventRequest> getProtoBaseClass() {
        return DeleteExternalEventRequest.class;
    }

    public DeleteExternalEventRequest.Builder toProto() {
        DeleteExternalEventRequest.Builder out = DeleteExternalEventRequest.newBuilder()
                .setWfRunId(wfRunId)
                .setExternalEventDefName(externalEventDefName)
                .setGuid(guid);

        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        ExternalEventIdModel eventId = new ExternalEventIdModel(wfRunId, externalEventDefName, guid);
        ExternalEventModel externalEvent = dao.getExternalEvent(eventId.getStoreKey());
        if (!externalEvent.claimed) {
            return dao.deleteExternalEvent(eventId.getStoreKey());
        } else {
            DeleteObjectReply response = new DeleteObjectReply();
            response.code = LHResponseCode.VALIDATION_ERROR;
            response.message = "ExternalEvent already claimed by WfRun " + externalEvent.wfRunId;
            return response;
        }
    }

    public void initFrom(Message proto) {
        DeleteExternalEventRequest p = (DeleteExternalEventRequest) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
    }

    public static DeleteExternalEventRequestModel fromProto(DeleteExternalEventRequest p) {
        DeleteExternalEventRequestModel out = new DeleteExternalEventRequestModel();
        out.initFrom(p);
        return out;
    }
}
