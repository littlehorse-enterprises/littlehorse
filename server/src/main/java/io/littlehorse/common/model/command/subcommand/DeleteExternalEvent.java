package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.sdk.common.proto.DeleteExternalEventPb;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class DeleteExternalEvent extends SubCommand<DeleteExternalEventPb> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<DeleteExternalEventPb> getProtoBaseClass() {
        return DeleteExternalEventPb.class;
    }

    public DeleteExternalEventPb.Builder toProto() {
        DeleteExternalEventPb.Builder out = DeleteExternalEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setExternalEventDefName(externalEventDefName)
            .setGuid(guid);

        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public DeleteObjectReply process(LHDAO dao, LHConfig config) {
        ExternalEventIdModel eventId = new ExternalEventIdModel(
            wfRunId,
            externalEventDefName,
            guid
        );
        ExternalEventModel externalEvent = dao.getExternalEvent(
            eventId.getStoreKey()
        );
        if (!externalEvent.claimed) {
            return dao.deleteExternalEvent(eventId.getStoreKey());
        } else {
            DeleteObjectReply response = new DeleteObjectReply();
            response.code = LHResponseCode.VALIDATION_ERROR;
            response.message =
                "ExternalEvent already claimed by WfRun " + externalEvent.wfRunId;
            return response;
        }
    }

    public void initFrom(Message proto) {
        DeleteExternalEventPb p = (DeleteExternalEventPb) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
    }

    public static DeleteExternalEvent fromProto(DeleteExternalEventPb p) {
        DeleteExternalEvent out = new DeleteExternalEvent();
        out.initFrom(p);
        return out;
    }
}
