package io.littlehorse.common.model.getable.global.bulkjob;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

public class BulkDeleteWfRunModel extends LHSerializable<BulkDeleteWfRun> {

    private String wfSpecName;
    private Date earliestStart;
    private Date latestStart;
    private LHStatus wfRunStatus;

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

    public String startKey() {
        return GetableClassEnum.WF_RUN.getNumber() + "/";
    }

    public String endKey() {
        return startKey() + "~";
    }

    public Optional<BulkJobShardCursorModel> process(
            Consumer<CoreSubCommand<?>> commandForwarder,
            TenantScopedStore coreStore,
            BulkJobShardCursorModel shardCursor) {
        try (LHKeyValueIterator<Tag> range = coreStore.range(startKey(), endKey(), Tag.class)) {
            while (range.hasNext()) {
                Tag tag = range.next().getValue();
                WfRunIdModel wfRunIdToDelete =
                        (WfRunIdModel) WfRunIdModel.fromString(tag.getDescribedObjectId(), WfRunIdModel.class);
                DeleteWfRunRequest delete = DeleteWfRunRequest.newBuilder()
                        .setId(wfRunIdToDelete.toProto())
                        .build();
                InternalDeleteWfRunRequestModel deleteWfRunRequestModel = new InternalDeleteWfRunRequestModel(delete);
                commandForwarder.accept(deleteWfRunRequestModel);
            }
        }
        return Optional.empty();
    }
}
