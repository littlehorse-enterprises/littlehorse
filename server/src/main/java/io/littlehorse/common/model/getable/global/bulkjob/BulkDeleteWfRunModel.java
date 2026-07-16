package io.littlehorse.common.model.getable.global.bulkjob;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BulkDeleteWfRunModel extends LHSerializable<BulkDeleteWfRun> {

    private String wfSpecName;
    private Date earliestStart;
    private Date latestStart;
    private LHStatus wfRunStatus;
    private static final Set<LHStatus> DELETABLE_WF_RUN_STATUSES =
            Set.of(LHStatus.COMPLETED, LHStatus.HALTED, LHStatus.ERROR, LHStatus.EXCEPTION);

    @Override
    public BulkDeleteWfRun.Builder toProto() {
        BulkDeleteWfRun.Builder out = BulkDeleteWfRun.newBuilder();
        out.setWfSpecName(wfSpecName);
        out.setEarliestStart(LHUtil.fromDate(earliestStart));
        out.setLatestStart(LHUtil.fromDate(latestStart));
        if (wfRunStatus != null) {
            out.setWfRunStatus(wfRunStatus);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkDeleteWfRun p = (BulkDeleteWfRun) proto;
        this.wfSpecName = p.getWfSpecName();
        this.earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        this.latestStart = LHUtil.fromProtoTs(p.getLatestStart());
        if (p.hasWfRunStatus()) {
            this.wfRunStatus = p.getWfRunStatus();
        }
    }

    @Override
    public Class<BulkDeleteWfRun> getProtoBaseClass() {
        return BulkDeleteWfRun.class;
    }

    private InternalScanPb.TagScanPb buildScan() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("wfSpecName", wfSpecName));
        if (wfRunStatus != null) {
            attributes.add(new Attribute("status", wfRunStatus.toString()));
        }
        String attributeString = Tag.getAttributeString(GetableClassEnum.WF_RUN, attributes);
        TagScanBoundaryStrategy strategy = new TagScanBoundaryStrategy(
                attributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
        return strategy.buildScanProto();
    }

    private String startKey(InternalScanPb.TagScanPb tagScan) {
        return tagScan.getKeyPrefix() + "/" + LHUtil.toLhDbFormat(LHUtil.fromProtoTs(tagScan.getEarliestCreateTime()));
    }

    private String endKey(InternalScanPb.TagScanPb tagScan) {
        return tagScan.getKeyPrefix() + "/" + LHUtil.toLhDbFormat(LHUtil.fromProtoTs(tagScan.getLatestCreateTime()));
    }

    public BulkJobShardCursorModel process(
            Consumer<CoreSubCommand<?>> commandForwarder,
            TenantScopedStore coreStore,
            BulkJobShardCursorModel shardCursor,
            BooleanSupplier outOfBudget) {
        // The cursor stores the full Tag store key of the last WfRun we deleted. On resume we
        // restart the range scan from that key (which the range includes inclusively) and skip it,
        // so every matching WfRun is deleted exactly once across punctuations.
        String resumeFromKey = shardCursor.getLastKey();
        String lastKey = resumeFromKey;
        Date lastSeenTimestamp = shardCursor.getLastSeenTimestamp();
        InternalScanPb.TagScanPb tagScan = buildScan();
        String startKey = resumeFromKey.isBlank() ? startKey(tagScan) : resumeFromKey;
        try (LHKeyValueIterator<Tag> range = coreStore.range(startKey, endKey(tagScan), Tag.class)) {
            while (range.hasNext()) {
                if (outOfBudget.getAsBoolean()) {
                    // Budget exhausted — save progress and resume on next tick
                    shardCursor.setLastKey(lastKey);
                    shardCursor.setLastSeenTimestamp(lastSeenTimestamp);
                    return shardCursor;
                }
                Tag tag = range.next().getValue();
                WfRunIdModel wfRunIdToDelete =
                        (WfRunIdModel) WfRunIdModel.fromString(tag.getDescribedObjectId(), WfRunIdModel.class);
                // The resume key is included in the range; it was already processed, so skip it.
                if (!resumeFromKey.isBlank() && tag.getStoreKey().equals(resumeFromKey)) {
                    ReadOnlyGetableManager getableManager = new ReadOnlyGetableManager(coreStore);
                    WfRunModel wfRunModel = getableManager.get(wfRunIdToDelete);
                    if (wfRunModel != null) {
                        // Previous delete not processed yet — back off, keep scanCompleted == false,
                        // and resume from the same boundary key next tick.
                        return shardCursor;
                    }
                    continue;
                }
                DeleteWfRunRequest delete = DeleteWfRunRequest.newBuilder()
                        .setId(wfRunIdToDelete.toProto())
                        .build();
                InternalDeleteWfRunRequestModel deleteWfRunRequestModel = new InternalDeleteWfRunRequestModel(delete);
                commandForwarder.accept(deleteWfRunRequestModel);
                lastKey = tag.getStoreKey();
                lastSeenTimestamp = tag.createdAt;
            }
        }
        shardCursor.setScanCompleted(true);
        shardCursor.setLastKey(lastKey);
        shardCursor.setLastSeenTimestamp(lastSeenTimestamp);
        return shardCursor;
    }

    public void validate() {
        if (wfSpecName == null || wfSpecName.isBlank()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfSpecName must be provided");
        }
        if (earliestStart != null && latestStart != null && earliestStart.after(latestStart)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "earliestStart must be before or equal to latestStart");
        }
        if (!DELETABLE_WF_RUN_STATUSES.contains(wfRunStatus)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfRunStatus must be a terminal status");
        }
        if (wfRunStatus == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfRunStatus must be provided");
        }
    }
}
