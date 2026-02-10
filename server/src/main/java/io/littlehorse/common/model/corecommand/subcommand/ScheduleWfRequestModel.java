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
import io.littlehorse.common.model.metadatacommand.subcommand.ScheduleWfRunCommandModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ScheduleWfRequest;
import io.littlehorse.sdk.common.proto.ScheduledWfRun;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a request to schedule workflow runs based on a specific cron expression.
 */
public class ScheduleWfRequestModel extends CoreSubCommand<ScheduleWfRequest> {
    private String id;
    private String wfSpecName;
    private Integer majorVersion;
    private Integer revision;
    private final Map<String, VariableValueModel> variables = new HashMap<>();
    private WfRunIdModel parentWfRunId;
    private String cronExpression;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
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
    public ScheduledWfRun process(CoreProcessorContext executionContext, LHServerConfig config) {
        WfSpecModel spec = executionContext.service().getWfSpec(wfSpecName, majorVersion, revision);
        if (spec == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "WfSpec %s %s.%s does not exist".formatted(wfSpecName, majorVersion, revision));
        }
        // Ensure we have an ID for partitioning and storage
        if (id == null) {
            id = LHUtil.generateGuid();
        }
        WfSpecIdModel wfSpecId = spec.getId();
        ScheduledWfRunIdModel scheduledId = new ScheduledWfRunIdModel(id);
        ScheduledWfRunModel scheduledWfRun =
                new ScheduledWfRunModel(scheduledId, wfSpecId, variables, parentWfRunId, cronExpression);
        executionContext.getableManager().put(scheduledWfRun);
        ScheduleWfRunCommandModel scheduledCommand = new ScheduleWfRunCommandModel(
                scheduledId, wfSpecName, majorVersion, revision, parentWfRunId, variables, cronExpression);

        // Schedule first execution at the next cron time, not immediately
        CommandModel current = executionContext.currentCommand();
        Date baseTime = current != null ? current.getTime() : null;
        Optional<Date> firstRun = LHUtil.nextDate(cronExpression, baseTime != null ? baseTime : new Date());
        if (firstRun.isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Cron expression does not yield a next execution time");
        }
        LHTimer timer = new LHTimer(new CommandModel(scheduledCommand, firstRun.get()));
        timer.maturationTime = firstRun.get();
        executionContext.getTaskManager().scheduleTimer(timer);
        return scheduledWfRun.toProto().build();
    }

    @Override
    public String getPartitionKey() {
        if (id == null) id = LHUtil.generateGuid();
        return new ScheduledWfRunIdModel(id).getPartitionKey().get();
    }
}
