package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import java.util.Date;
import java.util.Optional;

public class PutExternalEventRequestModel extends CoreSubCommand<PutExternalEventRequest> {

    public WfRunIdModel wfRunId;
    public ExternalEventDefIdModel externalEventDefId;
    public String guid;
    public VariableValueModel content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;

    @Override
    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public Class<PutExternalEventRequest> getProtoBaseClass() {
        return PutExternalEventRequest.class;
    }

    @Override
    public PutExternalEventRequest.Builder toProto() {
        PutExternalEventRequest.Builder out = PutExternalEventRequest.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setExternalEventDefId(externalEventDefId.toProto())
                .setContent(content.toProto());

        if (guid != null) out.setGuid(guid);
        if (threadRunNumber != null) out.setThreadRunNumber(threadRunNumber);
        if (nodeRunPosition != null) out.setNodeRunPosition(nodeRunPosition);

        return out;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public ExternalEvent process(CoreProcessorDAO dao, LHServerConfig config) {
        ExternalEventDefModel eed = dao.getExternalEventDef(externalEventDefId.getName());
        if (eed == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "No ExternalEventDef named " + externalEventDefId);
        }

        if (guid == null) guid = LHUtil.generateGuid();
        ExternalEventModel evt = new ExternalEventModel(
                content, wfRunId, externalEventDefId, guid, threadRunNumber, nodeRunPosition, dao.getEventTime());
        evt.setDao(dao);
        dao.put(evt);

        Optional<Date> expirationTime = eed.getRetentionPolicy().scheduleCleanup(dao.getEventTime());
        if (expirationTime.isPresent()) {
            DeleteExternalEventRequestModel deleteExternalEvent =
                    new DeleteExternalEventRequestModel(evt.getObjectId());
            // Schedule the garbage collection of the event.
            CommandModel deleteExtEventCmd = new CommandModel(deleteExternalEvent, expirationTime.get());
            dao.scheduleTimer(new LHTimer(deleteExtEventCmd, dao));
        }

        WfRunModel wfRunModel = dao.get(wfRunId);
        if (wfRunModel != null) {
            WfSpecModel spec = dao.getWfSpec(wfRunModel.getWfSpecName(), wfRunModel.getWfSpecVersion());
            if (spec == null) {
                wfRunModel
                        .getThreadRun(0)
                        .fail(new FailureModel("Appears wfSpec was deleted", LHConstants.INTERNAL_ERROR), new Date());

                // NOTE: need to commit the dao before we throw the exception.
                dao.commit();
                throw new LHApiException(Status.DATA_LOSS, "Appears wfSpec was deleted");
            } else {
                wfRunModel.processExternalEvent(evt);
            }
            dao.put(wfRunModel);
            dao.put(evt);
        } else {
            // it's a pre-emptive event.
        }

        return evt.toProto().build();
    }

    @Override
    public void initFrom(Message proto) {
        PutExternalEventRequest p = (PutExternalEventRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class);
        externalEventDefId = LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class);
        content = VariableValueModel.fromProto(p.getContent());

        if (p.hasGuid()) guid = p.getGuid();
        if (p.hasThreadRunNumber()) threadRunNumber = p.getThreadRunNumber();
        if (p.hasNodeRunPosition()) nodeRunPosition = p.getNodeRunPosition();
    }

    public static PutExternalEventRequestModel fromProto(PutExternalEventRequest p) {
        PutExternalEventRequestModel out = new PutExternalEventRequestModel();
        out.initFrom(p);
        return out;
    }
}
