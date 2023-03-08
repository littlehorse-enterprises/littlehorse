package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.RepartitionCommandPb;
import io.littlehorse.jlib.common.proto.RepartitionCommandPb.RepartitionCommandCase;
import io.littlehorse.jlib.common.proto.RepartitionCommandPbOrBuilder;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import java.util.Date;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class RepartitionCommand extends LHSerializable<RepartitionCommandPb> {

    public Date time;
    public String commandId;

    public RepartitionCommandCase type;
    public TaskMetricUpdate taskMetricPartitionWindow;
    public WfMetricUpdate wfMetricPartitionWindow;

    public Class<RepartitionCommandPb> getProtoBaseClass() {
        return RepartitionCommandPb.class;
    }

    public RepartitionCommand() {}

    public RepartitionCommand(
        RepartitionSubCommand command,
        Date time,
        String commandId
    ) {
        setSubCommand(command);
        this.time = time;
        this.commandId = commandId;
    }

    public void setSubCommand(RepartitionSubCommand subCommand) {
        if (subCommand.getClass().equals(TaskMetricUpdate.class)) {
            type = RepartitionCommandCase.TASK_METRIC_UPDATE;
            taskMetricPartitionWindow = (TaskMetricUpdate) subCommand;
        } else if (subCommand.getClass().equals(WfMetricUpdate.class)) {
            type = RepartitionCommandCase.WF_METRIC_UPDATE;
            wfMetricPartitionWindow = (WfMetricUpdate) subCommand;
        } else {
            throw new RuntimeException("Unknown class!");
        }
    }

    public RepartitionSubCommand getSubCommand() {
        switch (type) {
            case WF_METRIC_UPDATE:
                return wfMetricPartitionWindow;
            case TASK_METRIC_UPDATE:
                return taskMetricPartitionWindow;
            default:
                throw new RuntimeException("Unrecognized!");
        }
    }

    public void process(LHStoreWrapper store, ProcessorContext<Void, Void> ctx) {
        getSubCommand().process(store, ctx);
    }

    public RepartitionCommandPb.Builder toProto() {
        RepartitionCommandPb.Builder out = RepartitionCommandPb.newBuilder();
        out.setTime(LHUtil.fromDate(time));
        if (commandId != null) out.setCommandId(commandId);

        switch (type) {
            case TASK_METRIC_UPDATE:
                out.setTaskMetricUpdate(taskMetricPartitionWindow.toProto());
                break;
            case WF_METRIC_UPDATE:
                out.setWfMetricUpdate(wfMetricPartitionWindow.toProto());
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        RepartitionCommandPbOrBuilder p = (RepartitionCommandPbOrBuilder) proto;

        type = p.getRepartitionCommandCase();
        if (p.hasCommandId()) commandId = p.getCommandId();
        time = LHUtil.fromProtoTs(p.getTime());

        switch (type) {
            case TASK_METRIC_UPDATE:
                taskMetricPartitionWindow =
                    LHSerializable.fromProto(
                        p.getTaskMetricUpdate(),
                        TaskMetricUpdate.class
                    );
                break;
            case WF_METRIC_UPDATE:
                wfMetricPartitionWindow =
                    LHSerializable.fromProto(
                        p.getWfMetricUpdate(),
                        WfMetricUpdate.class
                    );
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
