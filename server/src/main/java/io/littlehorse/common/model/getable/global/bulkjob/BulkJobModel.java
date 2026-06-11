package io.littlehorse.common.model.getable.global.bulkjob;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.PunctuationExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.processor.api.Record;

@Getter
@Setter
public class BulkJobModel extends MetadataGetable<BulkJob> {

    private BulkJobIdModel id;
    private Date createdAt;
    private BulkJobStatus status;
    private BulkDeleteWfRunModel bulkDeleteWfRun;
    private long totalItems;
    private long processedItems;

    public BulkJobModel() {}

    public BulkJobModel(BulkJobIdModel id, BulkDeleteWfRunModel bulkDeleteWfRun) {
        this.id = id;
        this.createdAt = new Date();
        this.status = BulkJobStatus.BULK_JOB_RUNNING;
        this.bulkDeleteWfRun = bulkDeleteWfRun;
        this.totalItems = 0;
        this.processedItems = 0;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkJob p = (BulkJob) proto;
        id = LHSerializable.fromProto(p.getId(), BulkJobIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        status = p.getStatus();
        totalItems = p.getTotalItems();
        processedItems = p.getProcessedItems();

        if (p.hasBulkDeleteWfRun()) {
            bulkDeleteWfRun = LHSerializable.fromProto(p.getBulkDeleteWfRun(), BulkDeleteWfRunModel.class, context);
        }
    }

    @Override
    public BulkJob.Builder toProto() {
        BulkJob.Builder out = BulkJob.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setStatus(status)
                .setTotalItems(totalItems)
                .setProcessedItems(processedItems);

        if (bulkDeleteWfRun != null) {
            out.setBulkDeleteWfRun(bulkDeleteWfRun.toProto());
        }
        return out;
    }

    public Optional<BulkJobShardCursorModel> tryToComplete(
            Consumer<Record> commandOutput, PunctuationExecutionContext context, BulkJobShardCursorModel shardCursor) {
        return bulkDeleteWfRun.process(
                c -> forwardDeleteCommand(c, commandOutput, context.serverConfig()), context.coreStore(), shardCursor);
    }

    private void forwardDeleteCommand(
            CoreSubCommand<?> subCommand, Consumer<Record> commandOutput, LHServerConfig config) {
        CommandModel command = new CommandModel(subCommand);
        LHTimer timer = new LHTimer(command, true);
        timer.topic = command.getTopic(config);
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = command.getPartitionKey();
        cpo.topic = command.getTopic(config);
        cpo.payload = timer;
        TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);
        PrincipalIdModel principalId = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);
        commandOutput.accept(new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, principalId)));
    }

    @Override
    public Class<BulkJob> getProtoBaseClass() {
        return BulkJob.class;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(new GetableIndex<>(
                List.of(Pair.of("status", GetableIndex.ValueType.SINGLE)), Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public BulkJobIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "status" -> {
                return List.of(new IndexedField(key, status.toString(), tagStorageType.get()));
            }
        }
        return List.of();
    }
}
