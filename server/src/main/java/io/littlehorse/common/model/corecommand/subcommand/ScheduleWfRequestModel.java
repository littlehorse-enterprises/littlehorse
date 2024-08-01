package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ScheduledWfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.ScheduledCommandModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.ScheduleWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScheduleWfRequestModel extends CoreSubCommand<ScheduleWfRequest> {
    private String id;
    private String wfSpecName;
    private Integer majorVersion;
    private Integer revision;
    private final Map<String, VariableValueModel> variables = new HashMap<>();
    private WfRunIdModel parentWfRunId;
    private String cronExpression;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ScheduleWfRequest p = (ScheduleWfRequest) proto;
        if (p.hasId()) id = p.getId();
        wfSpecName = p.getWfSpecName();
        cronExpression = p.getCronExpression();
        if (p.hasRevision()) revision = p.getRevision();
        if (p.hasMajorVersion()) majorVersion = p.getMajorVersion();

        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }

        if (p.hasParentWfRunId()) {
            parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);
        }
    }

    @Override
    public ScheduleWfRequest.Builder toProto() {
        ScheduleWfRequest.Builder out =
                ScheduleWfRequest.newBuilder().setWfSpecName(wfSpecName).setCronExpression(cronExpression);
        if (id != null) out.setId(id);
        if (revision != null) out.setRevision(revision);
        if (majorVersion != null) out.setMajorVersion(majorVersion);

        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        if (parentWfRunId != null) {
            out.setParentWfRunId(parentWfRunId.toProto());
        }
        return out;
    }

    @Override
    public Class<ScheduleWfRequest> getProtoBaseClass() {
        return ScheduleWfRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        Date eventTime = executionContext.currentCommand().getTime();
        Optional<Date> scheduledTime = LHUtil.nextDate(cronExpression, eventTime);
        if (scheduledTime.isPresent()) {
            ScheduledCommandModel scheduledCommand =
                    new ScheduledCommandModel(new CommandModel(createRunWfCommand(executionContext)), cronExpression);
            LHTimer timer = new LHTimer(new CommandModel(scheduledCommand));
            timer.maturationTime = scheduledTime.get();
            executionContext.getTaskManager().scheduleTimer(timer);
            WfSpecModel spec = executionContext.service().getWfSpec(wfSpecName, majorVersion, revision);
            WfSpecIdModel wfSpecId = spec.getId();
            ScheduledWfRunIdModel scheduledId = new ScheduledWfRunIdModel(id);
            ScheduledWfRunModel scheduledWfRun =
                    new ScheduledWfRunModel(scheduledId, wfSpecId, variables, parentWfRunId, cronExpression);
            executionContext.getableManager().put(scheduledWfRun);
            return scheduledWfRun.toProto().build();
        } else {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Invalid next date");
        }
    }

    @Override
    public String getPartitionKey() {
        if (id == null) id = LHUtil.generateGuid();

        // Child wfrun needs access to state of parent, so it needs to be on the same partition
        if (parentWfRunId != null) {
            return parentWfRunId.getPartitionKey().get();
        }
        return id;
    }

    private RunWfRequestModel createRunWfCommand(ExecutionContext context) {
        RunWfRequest.Builder protoBuilder = RunWfRequest.newBuilder();
        protoBuilder.setWfSpecName(wfSpecName);
        if (majorVersion != null) {
            protoBuilder.setMajorVersion(majorVersion);
        }
        if (revision != null) {
            protoBuilder.setRevision(revision);
        }
        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            protoBuilder.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        if (parentWfRunId != null) {
            protoBuilder.setParentWfRunId(parentWfRunId.toProto());
        }
        return LHSerializable.fromProto(protoBuilder.build(), RunWfRequestModel.class, context);
    }
}
