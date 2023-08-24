package io.littlehorse.common.model.repartitioncommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.common.proto.RepartitionCommandPb;
import io.littlehorse.common.proto.RepartitionCommandPb.RepartitionCommandCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import java.util.Date;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class RepartitionCommand extends LHSerializable<RepartitionCommandPb> {

    public Date time;
    public String commandId;

    public RepartitionCommandCase type;
    public TaskMetricUpdate taskMetricPartitionWindow;
    public WfMetricUpdate wfMetricPartitionWindow;
    private CreateRemoteTag createRemoteTag;
    private RemoveRemoteTag removeRemoteTag;

    public Class<RepartitionCommandPb> getProtoBaseClass() {
        return RepartitionCommandPb.class;
    }

    public RepartitionCommand() {}

    public RepartitionCommand(RepartitionSubCommand command, Date time, String commandId) {
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
        } else if (subCommand.getClass().equals(CreateRemoteTag.class)) {
            type = RepartitionCommandCase.CREATE_REMOTE_TAG;
            createRemoteTag = (CreateRemoteTag) subCommand;
        } else if (subCommand.getClass().equals(RemoveRemoteTag.class)) {
            type = RepartitionCommandCase.REMOVE_REMOTE_TAG;
            removeRemoteTag = (RemoveRemoteTag) subCommand;
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
            case CREATE_REMOTE_TAG:
                return createRemoteTag;
            case REMOVE_REMOTE_TAG:
                return removeRemoteTag;
            default:
                throw new RuntimeException("Unrecognized!");
        }
    }

    public void process(RocksDBWrapper store, ProcessorContext<Void, Void> ctx) {
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
            case CREATE_REMOTE_TAG:
                out.setCreateRemoteTag(createRemoteTag.toProto());
                break;
            case REMOVE_REMOTE_TAG:
                out.setRemoveRemoteTag(removeRemoteTag.toProto());
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public void initFrom(Message proto) {
        RepartitionCommandPb p = (RepartitionCommandPb) proto;

        type = p.getRepartitionCommandCase();
        if (p.hasCommandId()) commandId = p.getCommandId();
        time = LHUtil.fromProtoTs(p.getTime());

        switch (type) {
            case TASK_METRIC_UPDATE:
                taskMetricPartitionWindow = LHSerializable.fromProto(p.getTaskMetricUpdate(), TaskMetricUpdate.class);
                break;
            case WF_METRIC_UPDATE:
                wfMetricPartitionWindow = LHSerializable.fromProto(p.getWfMetricUpdate(), WfMetricUpdate.class);
                break;
            case CREATE_REMOTE_TAG:
                createRemoteTag = LHSerializable.fromProto(p.getCreateRemoteTag(), CreateRemoteTag.class);
                break;
            case REMOVE_REMOTE_TAG:
                removeRemoteTag = LHSerializable.fromProto(p.getRemoveRemoteTag(), RemoveRemoteTag.class);
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
