package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.CommandProcessorDao;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PutExternalEventPb;
import io.littlehorse.common.proto.PutExternalEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class PutExternalEvent extends SubCommand<PutExternalEventPb> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;
    public VariableValue content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<PutExternalEventPb> getProtoBaseClass() {
        return PutExternalEventPb.class;
    }

    public PutExternalEventPb.Builder toProto() {
        PutExternalEventPb.Builder out = PutExternalEventPb
            .newBuilder()
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

    public PutExternalEventReply process(CommandProcessorDao dao, LHConfig config) {
        PutExternalEventReply out = new PutExternalEventReply();

        ExternalEventDef eed = dao.getExternalEventDef(externalEventDefName, null);
        if (eed == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "No ExternalEventDef named " + externalEventDefName;
            return out;
        }

        if (guid == null) guid = LHUtil.generateGuid();
        ExternalEvent evt = new ExternalEvent();
        evt.wfRunId = wfRunId;
        evt.content = content;
        evt.externalEventDefName = externalEventDefName;
        evt.guid = guid;
        evt.nodeRunPosition = nodeRunPosition;
        evt.threadRunNumber = threadRunNumber;
        evt.claimed = false;

        dao.saveExternalEvent(evt);

        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun != null) {
            wfRun.processExternalEvent(evt);
            dao.saveWfRun(wfRun);
            dao.saveExternalEvent(evt);
        }

        out.result = evt;

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutExternalEventPbOrBuilder p = (PutExternalEventPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        content = VariableValue.fromProto(p.getContentOrBuilder());

        if (p.hasGuid()) guid = p.getGuid();
        if (p.hasThreadRunNumber()) threadRunNumber = p.getThreadRunNumber();
        if (p.hasNodeRunPosition()) nodeRunPosition = p.getNodeRunPosition();
    }

    public static PutExternalEvent fromProto(PutExternalEventPbOrBuilder p) {
        PutExternalEvent out = new PutExternalEvent();
        out.initFrom(p);
        return out;
    }
}
