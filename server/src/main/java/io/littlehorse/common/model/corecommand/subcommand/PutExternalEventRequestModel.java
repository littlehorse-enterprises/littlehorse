package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
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
    public ExternalEvent process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        WfService service = executionContext.service();
        ExternalEventDefModel eed = service.getExternalEventDef(externalEventDefId.getName());
        Date eventTime = executionContext.currentCommand().getTime();
        GetableManager getableManager = executionContext.getableManager();
        if (eed == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "No ExternalEventDef named " + externalEventDefId);
        }

        if (guid == null) guid = LHUtil.generateGuid();

        ExternalEventModel evt = new ExternalEventModel(
                content, wfRunId, externalEventDefId, guid, threadRunNumber, nodeRunPosition, eventTime);
        getableManager.put(evt);

        Optional<Date> expirationTime = eed.getRetentionPolicy().scheduleCleanup(eventTime);
        if (expirationTime.isPresent()) {
            DeleteExternalEventRequestModel deleteExternalEvent =
                    new DeleteExternalEventRequestModel(evt.getObjectId());
            // Schedule the garbage collection of the event.
            CommandModel deleteExtEventCmd = new CommandModel(deleteExternalEvent, expirationTime.get());
            executionContext.getTaskManager().scheduleTimer(new LHTimer(deleteExtEventCmd));
        }

        WfRunModel wfRun = getableManager.get(wfRunId);
        if (wfRun != null) {
            wfRun.processExternalEvent(evt);
            wfRun.advance(eventTime);
        } else {
            // it's a pre-emptive event.
        }

        return evt.toProto().build();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutExternalEventRequest p = (PutExternalEventRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
        content = VariableValueModel.fromProto(p.getContent(), context);

        if (p.hasGuid()) guid = p.getGuid();
        if (p.hasThreadRunNumber()) threadRunNumber = p.getThreadRunNumber();
        if (p.hasNodeRunPosition()) nodeRunPosition = p.getNodeRunPosition();
    }

    public static PutExternalEventRequestModel fromProto(PutExternalEventRequest p, ExecutionContext context) {
        PutExternalEventRequestModel out = new PutExternalEventRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
