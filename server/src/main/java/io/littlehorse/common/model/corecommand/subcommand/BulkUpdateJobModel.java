package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJob;
import io.littlehorse.common.model.corecommand.subcommand.job.NoOpJobModel;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.LHTaskManager;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

public class BulkUpdateJobModel extends CoreSubCommand<BulkUpdateJob> {

    private int partitionKey;
    private String startKey;
    private String endKey;
    private String resumeFromKey;
    private BulkJob job;
    private NoOpJobModel noOpJob;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkUpdateJob p = (BulkUpdateJob) proto;
        this.partitionKey = p.getPartition();
        this.startKey = p.getStartKey();
        this.endKey = p.getEndKey();
        this.resumeFromKey = p.hasResumeFromKey() ? p.getResumeFromKey() : null;
        this.job = resolveJob(p, context);
    }

    private BulkJob resolveJob(BulkUpdateJob jobProto, ExecutionContext context) {
        return switch (jobProto.getJobCase()) {
            case NO_OP -> {
                this.noOpJob = LHSerializable.fromProto(jobProto.getNoOp(), NoOpJobModel.class, context);
                yield noOpJob;
            }
            default -> throw new IllegalArgumentException("%s not supported yet".formatted(jobProto.getJobCase()));
        };
    }

    @Override
    public BulkUpdateJob.Builder toProto() {
        BulkUpdateJob.Builder builder = BulkUpdateJob.newBuilder();
        builder.setPartition(partitionKey);
        builder.setStartKey(startKey);
        builder.setEndKey(endKey);
        if (resumeFromKey != null) {
            builder.setResumeFromKey(resumeFromKey);
        }
        if (this.noOpJob != null) {
            builder.setNoOp(noOpJob.toProto());
        }
        return builder;
    }

    @Override
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        Date limitTime = DateUtils.addMilliseconds(new Date(), config.getMaxBulkJobIterDurationMs());
        try (LHKeyValueIterator<?> range =
                executionContext.getableManager().range(startKey, endKey, StoredGetable.class)) {
            String lastKey = null;
            Date iterationTime = new Date();
            while (range.hasNext() && iterationTime.compareTo(limitTime) <= 0) {
                lastKey = job.processOneRecord(range.next());
            }
            if (range.hasNext() && lastKey != null) {
                scheduleNextIteration(
                        lastKey,
                        executionContext.getTaskManager(),
                        DateUtils.addSeconds(iterationTime, config.getBulkJobDelayIntervalSeconds()));
            }
            return Empty.getDefaultInstance();
        }
    }

    @Override
    public Class<? extends GeneratedMessage> getProtoBaseClass() {
        return BulkUpdateJob.class;
    }

    @Override
    public String getPartitionKey() {
        return String.valueOf(partitionKey); // ??
    }

    private void scheduleNextIteration(String lastKey, LHTaskManager taskManager, Date nextBulkDate) {
        taskManager.scheduleTimer(new LHTimer(buildNextCommand(lastKey, nextBulkDate)));
    }

    private CommandModel buildNextCommand(String lastKey, Date nextBulkDate) {
        BulkUpdateJobModel nextBulkJob = new BulkUpdateJobModel();
        nextBulkJob.partitionKey = partitionKey;
        nextBulkJob.startKey = startKey;
        nextBulkJob.endKey = endKey;
        nextBulkJob.resumeFromKey = lastKey;
        return new CommandModel(nextBulkJob, nextBulkDate);
    }
}
