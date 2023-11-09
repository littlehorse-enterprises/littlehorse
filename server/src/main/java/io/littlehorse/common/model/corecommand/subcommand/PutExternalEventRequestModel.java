package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
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
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

public class PutExternalEventRequestModel extends CoreSubCommand<PutExternalEventRequest> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;
    public VariableValueModel content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<PutExternalEventRequest> getProtoBaseClass() {
        return PutExternalEventRequest.class;
    }

    public PutExternalEventRequest.Builder toProto() {
        PutExternalEventRequest.Builder out = PutExternalEventRequest.newBuilder()
                .setWfRunId(wfRunId)
                .setExternalEventDefName(externalEventDefName)
                .setContent(content.toProto());

        if (guid != null) out.setGuid(guid);
        if (threadRunNumber != null) out.setThreadRunNumber(threadRunNumber);
        if (nodeRunPosition != null) out.setNodeRunPosition(nodeRunPosition);

        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    @Override
    public ExternalEvent process(CoreProcessorDAO dao, LHServerConfig config) {
        ExternalEventDefModel eed = dao.getExternalEventDef(externalEventDefName);
        if (eed == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "No ExternalEventDef named " + externalEventDefName);
        }

        if (guid == null) guid = LHUtil.generateGuid();
        ExternalEventModel evt = new ExternalEventModel();
        evt.wfRunId = wfRunId;
        evt.content = content;
        evt.externalEventDefName = externalEventDefName;
        evt.guid = guid;
        evt.nodeRunPosition = nodeRunPosition;
        evt.threadRunNumber = threadRunNumber;
        evt.claimed = false;

        dao.put(evt);

        if (eed.retentionHours != LHConstants.INFINITE_RETENTION) {
            LHTimer timer = new LHTimer();
            timer.topic = dao.getCoreCmdTopic();
            timer.key = this.wfRunId;
            Date now = new Date();
            timer.maturationTime = DateUtils.addHours(now, eed.retentionHours);

            // Schedule the garbage collection of the event.
            DeleteExternalEventRequestModel deleteExternalEvent = new DeleteExternalEventRequestModel();
            deleteExternalEvent.setId(new ExternalEventIdModel(wfRunId, externalEventDefName, guid));
            CommandModel deleteExtEventCmd = new CommandModel();
            deleteExtEventCmd.setSubCommand(deleteExternalEvent);
            deleteExtEventCmd.time = timer.maturationTime;
            timer.payload = deleteExtEventCmd.toProto().build().toByteArray();
            timer.setTenantId(dao.context().tenantId());
            timer.setPrincipalId(dao.context().principalId());
            dao.scheduleTimer(timer);
        }

        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel != null) {
            WfSpecModel spec = dao.getWfSpec(wfRunModel.wfSpecName, wfRunModel.wfSpecVersion);
            if (spec == null) {
                wfRunModel
                        .threadRunModels
                        .get(0)
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

    public void initFrom(Message proto) {
        PutExternalEventRequest p = (PutExternalEventRequest) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
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
