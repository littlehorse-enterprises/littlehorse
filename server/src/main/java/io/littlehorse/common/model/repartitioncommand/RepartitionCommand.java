package io.littlehorse.common.model.repartitioncommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateTaskMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWfMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.common.proto.RepartitionCommandPb;
import io.littlehorse.common.proto.RepartitionCommandPb.RepartitionCommandCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Setter
@Getter
public class RepartitionCommand extends LHSerializable<RepartitionCommandPb> {

    public Date time;
    public String commandId;

    public RepartitionCommandCase type;
    private CreateRemoteTag createRemoteTag;
    private RemoveRemoteTag removeRemoteTag;
    private AggregateWfMetricsModel aggregateWfMetrics;
    private AggregateTaskMetricsModel aggregateTaskMetrics;
    private AggregateWindowMetricsModel aggregateWindowMetrics;

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
        if (subCommand.getClass().equals(CreateRemoteTag.class)) {
            type = RepartitionCommandCase.CREATE_REMOTE_TAG;
            createRemoteTag = (CreateRemoteTag) subCommand;
        } else if (subCommand.getClass().equals(RemoveRemoteTag.class)) {
            type = RepartitionCommandCase.REMOVE_REMOTE_TAG;
            removeRemoteTag = (RemoveRemoteTag) subCommand;
        } else if (subCommand.getClass().equals(AggregateWfMetricsModel.class)) {
            type = RepartitionCommandCase.AGGREGATE_WF_METRICS;
            aggregateWfMetrics = (AggregateWfMetricsModel) subCommand;
        } else if (subCommand.getClass().equals(AggregateTaskMetricsModel.class)) {
            type = RepartitionCommandCase.AGGREGATE_TASK_METRICS;
            aggregateTaskMetrics = (AggregateTaskMetricsModel) subCommand;
        } else if (subCommand.getClass().equals(AggregateWindowMetricsModel.class)) {
            type = RepartitionCommandCase.AGGREGATE_WINDOW_METRICS;
            aggregateWindowMetrics = (AggregateWindowMetricsModel) subCommand;
        } else {
            throw new RuntimeException("Unknown class!");
        }
    }

    public RepartitionSubCommand getSubCommand() {
        switch (type) {
            case CREATE_REMOTE_TAG:
                return createRemoteTag;
            case REMOVE_REMOTE_TAG:
                return removeRemoteTag;
            case AGGREGATE_WF_METRICS:
                return aggregateWfMetrics;
            case AGGREGATE_TASK_METRICS:
                return aggregateTaskMetrics;
            case AGGREGATE_WINDOW_METRICS:
                return aggregateWindowMetrics;
            default:
                throw new RuntimeException("Unrecognized!");
        }
    }

    public void process(TenantScopedStore store, ProcessorContext<Void, Void> ctx) {
        getSubCommand().process(store, ctx);
    }

    public RepartitionCommandPb.Builder toProto() {
        RepartitionCommandPb.Builder out = RepartitionCommandPb.newBuilder();
        out.setTime(LHUtil.fromDate(time));
        if (commandId != null) out.setCommandId(commandId);

        switch (type) {
            case CREATE_REMOTE_TAG:
                out.setCreateRemoteTag(createRemoteTag.toProto());
                break;
            case REMOVE_REMOTE_TAG:
                out.setRemoveRemoteTag(removeRemoteTag.toProto());
                break;
            case AGGREGATE_WF_METRICS:
                out.setAggregateWfMetrics(aggregateWfMetrics.toProto());
                break;
            case AGGREGATE_TASK_METRICS:
                out.setAggregateTaskMetrics(aggregateTaskMetrics.toProto());
                break;
            case AGGREGATE_WINDOW_METRICS:
                out.setAggregateWindowMetrics(aggregateWindowMetrics.toProto());
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        RepartitionCommandPb p = (RepartitionCommandPb) proto;

        type = p.getRepartitionCommandCase();
        if (p.hasCommandId()) commandId = p.getCommandId();
        time = LHUtil.fromProtoTs(p.getTime());

        switch (type) {
            case CREATE_REMOTE_TAG:
                createRemoteTag = LHSerializable.fromProto(p.getCreateRemoteTag(), CreateRemoteTag.class, context);
                break;
            case REMOVE_REMOTE_TAG:
                removeRemoteTag = LHSerializable.fromProto(p.getRemoveRemoteTag(), RemoveRemoteTag.class, context);
                break;
            case AGGREGATE_WF_METRICS:
                aggregateWfMetrics =
                        LHSerializable.fromProto(p.getAggregateWfMetrics(), AggregateWfMetricsModel.class, context);
                break;
            case AGGREGATE_TASK_METRICS:
                aggregateTaskMetrics =
                        LHSerializable.fromProto(p.getAggregateTaskMetrics(), AggregateTaskMetricsModel.class, context);
                break;
            case AGGREGATE_WINDOW_METRICS:
                aggregateWindowMetrics = LHSerializable.fromProto(
                        p.getAggregateWindowMetrics(), AggregateWindowMetricsModel.class, context);
                break;
            case REPARTITIONCOMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
