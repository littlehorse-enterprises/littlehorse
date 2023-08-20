package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventResponseModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

public class PutExternalEventRequestModel extends SubCommand<PutExternalEventRequest> {

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

        if (guid != null)
            out.setGuid(guid);
        if (threadRunNumber != null)
            out.setThreadRunNumber(threadRunNumber);
        if (nodeRunPosition != null)
            out.setNodeRunPosition(nodeRunPosition);

        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public PutExternalEventResponseModel process(CoreProcessorDAO dao, LHConfig config) {
        PutExternalEventResponseModel out = new PutExternalEventResponseModel();

        ExternalEventDefModel eed = dao.getExternalEventDef(externalEventDefName);
        if (eed == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "No ExternalEventDef named " + externalEventDefName;
            return out;
        }

        if (guid == null)
            guid = LHUtil.generateGuid();
        ExternalEventModel evt = new ExternalEventModel();
        evt.wfRunId = wfRunId;
        evt.content = content;
        evt.externalEventDefName = externalEventDefName;
        evt.guid = guid;
        evt.nodeRunPosition = nodeRunPosition;
        evt.threadRunNumber = threadRunNumber;
        evt.claimed = false;

        dao.saveExternalEvent(evt);

        LHTimer timer = new LHTimer();
        timer.topic = dao.getCoreCmdTopic();
        timer.key = this.wfRunId;
        Date now = new Date();
        timer.maturationTime = DateUtils.addHours(now, eed.retentionHours);
        DeleteExternalEventRequestModel deleteExternalEvent = new DeleteExternalEventRequestModel();
        deleteExternalEvent.externalEventDefName = this.externalEventDefName;
        deleteExternalEvent.wfRunId = this.wfRunId;
        deleteExternalEvent.guid = this.guid;

        CommandModel deleteExtEventCmd = new CommandModel();
        deleteExtEventCmd.setSubCommand(deleteExternalEvent);
        deleteExtEventCmd.time = timer.maturationTime;
        timer.payload = deleteExtEventCmd.toProto().build().toByteArray();
        dao.scheduleTimer(timer);

        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel != null) {
            WfSpecModel spec = dao.getWfSpec(wfRunModel.wfSpecName, wfRunModel.wfSpecVersion);
            if (spec == null) {
                wfRunModel.threadRunModels
                        .get(0)
                        .fail(new FailureModel("Appears wfSpec was deleted", LHConstants.INTERNAL_ERROR), new Date());
                out.code = LHResponseCode.NOT_FOUND_ERROR;
                out.message = "Apparently WfSpec was deleted!";
            } else {
                wfRunModel.wfSpecModel = spec;
                wfRunModel.processExternalEvent(evt);
                out.code = LHResponseCode.OK;
            }
            dao.saveWfRun(wfRunModel);
            dao.saveExternalEvent(evt);
        } else {
            // it's a pre-emptive event.
            out.code = LHResponseCode.OK;
        }

        out.result = evt;

        return out;
    }

    public void initFrom(Message proto) {
        PutExternalEventRequest p = (PutExternalEventRequest) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        content = VariableValueModel.fromProto(p.getContent());

        if (p.hasGuid())
            guid = p.getGuid();
        if (p.hasThreadRunNumber())
            threadRunNumber = p.getThreadRunNumber();
        if (p.hasNodeRunPosition())
            nodeRunPosition = p.getNodeRunPosition();
    }

    public static PutExternalEventRequestModel fromProto(PutExternalEventRequest p) {
        PutExternalEventRequestModel out = new PutExternalEventRequestModel();
        out.initFrom(p);
        return out;
    }
}
