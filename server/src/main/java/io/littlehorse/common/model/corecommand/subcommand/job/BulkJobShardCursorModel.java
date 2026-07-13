package io.littlehorse.common.model.corecommand.subcommand.job;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.proto.BulkJobShardCursor;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Internal storeable that tracks the range scan cursor for a BulkJobShard.
 * Stored in the per-partition Core Store. Not exposed via the public API.
 */
@Getter
@Setter
public class BulkJobShardCursorModel extends Storeable<BulkJobShardCursor> {

    private BulkJobIdModel bulkJobId;
    private String lastKey;
    private boolean scanCompleted;
    private Date lastSeenTimestamp;

    public BulkJobShardCursorModel() {}

    public BulkJobShardCursorModel(BulkJobIdModel bulkJobId) {
        this.bulkJobId = bulkJobId;
        this.lastKey = "";
        this.scanCompleted = false;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkJobShardCursor p = (BulkJobShardCursor) proto;
        this.bulkJobId = LHSerializable.fromProto(p.getBulkJobId(), BulkJobIdModel.class, context);
        this.lastKey = p.getLastKey();
        this.scanCompleted = p.getScanCompleted();
        if (p.hasLastSeenTimestamp()) {
            this.lastSeenTimestamp = LHUtil.fromProtoTs(p.getLastSeenTimestamp());
        }
    }

    @Override
    public BulkJobShardCursor.Builder toProto() {
        BulkJobShardCursor.Builder out = BulkJobShardCursor.newBuilder()
                .setBulkJobId(bulkJobId.toProto())
                .setLastKey(lastKey)
                .setScanCompleted(scanCompleted);
        if (lastSeenTimestamp != null) {
            out.setLastSeenTimestamp(LHUtil.fromDate(lastSeenTimestamp));
        }
        return out;
    }

    @Override
    public Class<BulkJobShardCursor> getProtoBaseClass() {
        return BulkJobShardCursor.class;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.BULK_JOB_SHARD_CURSOR;
    }

    @Override
    public String getStoreKey() {
        return bulkJobId.getStoreableKey();
    }
}
