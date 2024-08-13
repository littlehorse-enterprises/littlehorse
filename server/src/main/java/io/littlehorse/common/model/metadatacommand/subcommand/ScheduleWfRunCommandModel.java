package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.ScheduleWfRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleWfRunCommandModel extends CoreSubCommand<ScheduleWfRun> {
    private ScheduledWfRunIdModel scheduledId;
    private WfSpecIdModel wfSpecId;
    private Map<String, VariableValueModel> variables = new HashMap<>();
    private String cronExpression;
    private WfRunIdModel parentWfRunId;

    public ScheduleWfRunCommandModel() {}

    public ScheduleWfRunCommandModel(
            ScheduledWfRunIdModel scheduledId,
            WfSpecIdModel wfSpecId,
            WfRunIdModel parentWfRunId,
            Map<String, VariableValueModel> variables,
            String cronExpression) {
        this.scheduledId = scheduledId;
        this.wfSpecId = wfSpecId;
        this.variables = variables;
        this.cronExpression = cronExpression;
        this.parentWfRunId = parentWfRunId;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ScheduleWfRun p = (ScheduleWfRun) proto;
        scheduledId = LHSerializable.fromProto(p.getScheduledId(), ScheduledWfRunIdModel.class, context);
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);
        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }
        cronExpression = p.getCronExpression();
    }

    @Override
    public ScheduleWfRun.Builder toProto() {
        ScheduleWfRun.Builder out = ScheduleWfRun.newBuilder()
                .setScheduledId(scheduledId.toProto())
                .setWfSpecId(wfSpecId.toProto())
                .setParentWfRunId(parentWfRunId.toProto())
                .setCronExpression(cronExpression);

        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public Class<ScheduleWfRun> getProtoBaseClass() {
        return ScheduleWfRun.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        CommandModel commandToExecute = new CommandModel(createRunWfCommand(executionContext), new Date());
        LHTimer runWfTimer = new LHTimer(commandToExecute);
        runWfTimer.maturationTime = new Date();
        executionContext.getTaskManager().scheduleTimer(runWfTimer);
        Optional<Date> scheduledTime = LHUtil.nextDate(cronExpression, new Date());

        if (scheduledTime.isPresent()) {
            CommandModel nextSchedule = new CommandModel(copy(), scheduledTime.get());
            LHTimer nextScheduleTimer = new LHTimer(nextSchedule);
            nextScheduleTimer.maturationTime = scheduledTime.get();
            executionContext.getTaskManager().scheduleTimer(nextScheduleTimer);
        }
        return null;
    }

    private RunWfRequestModel createRunWfCommand(ExecutionContext context) {
        RunWfRequest.Builder protoBuilder = RunWfRequest.newBuilder();
        protoBuilder.setWfSpecName(wfSpecId.getName());
        protoBuilder.setMajorVersion(wfSpecId.getMajorVersion());
        protoBuilder.setRevision(wfSpecId.getRevision());
        protoBuilder.setId(creatNextWfRunId().getId());
        if (parentWfRunId != null) {
            protoBuilder.setParentWfRunId(parentWfRunId.toProto());
        }
        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            protoBuilder.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        RunWfRequestModel output = LHSerializable.fromProto(protoBuilder.build(), RunWfRequestModel.class, context);
        output.getPartitionKey();
        return output;
    }

    @Override
    public String getPartitionKey() {
        return scheduledId.getPartitionKey().get();
    }

    private WfRunIdModel creatNextWfRunId() {
        String nextSimpleId = LHUtil.generateGuid();
        return new WfRunIdModel(nextSimpleId, parentWfRunId);
    }

    private ScheduleWfRunCommandModel copy() {
        return new ScheduleWfRunCommandModel(scheduledId, wfSpecId, parentWfRunId, variables, cronExpression);
    }
}
