package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.jlib.common.proto.DeleteExternalEventPb;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;

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
        ExternalEventId eventId = new ExternalEventId(
            wfRunId,
            externalEventDefName,
            guid
        );
        ExternalEvent externalEvent = dao.getExternalEvent(eventId.getStoreKey());
        if (!externalEvent.claimed) {
            return dao.deleteExternalEvent(eventId.getStoreKey());
        } else {
            DeleteObjectReply response = new DeleteObjectReply();
            response.code = LHResponseCodePb.VALIDATION_ERROR;
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
