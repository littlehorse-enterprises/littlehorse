package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
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

    public DeleteObjectReply process(CoreProcessorDAO dao, LHConfig config) {
        ExternalEventIdModel eventId = new ExternalEventIdModel(wfRunId, externalEventDefName, guid);
        ExternalEventModel externalEvent = dao.get(eventId);
        if (!externalEvent.claimed) {
            return dao.delete(eventId);
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
