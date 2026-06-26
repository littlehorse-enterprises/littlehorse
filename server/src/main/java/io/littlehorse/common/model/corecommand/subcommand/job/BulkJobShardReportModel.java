package io.littlehorse.common.model.corecommand.subcommand.job;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.proto.BulkJobShardReport;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class BulkJobShardReportModel extends MetadataSubCommand<BulkJobShardReport> {

    private BulkJobIdModel bulkJobId;
    private int partition;
    private boolean completed;
    private String lastSeenKey;
    private Date lastSeenTimestamp;

    public BulkJobShardReportModel() {}

    public BulkJobShardReportModel(
            BulkJobIdModel bulkJobId, int partition, boolean completed, String lastSeenKey, Date lastSeenTimestamp) {
        this.bulkJobId = bulkJobId;
        this.partition = partition;
        this.completed = completed;
        this.lastSeenKey = lastSeenKey;
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkJobShardReport p = (BulkJobShardReport) proto;
        this.bulkJobId = LHSerializable.fromProto(p.getBulkJobId(), BulkJobIdModel.class, context);
        this.partition = p.getPartition();
        this.completed = p.getCompleted();
        this.lastSeenKey = p.getLastSeenKey();
        if (p.hasLastSeenTimestamp()) {
            this.lastSeenTimestamp = LHUtil.fromProtoTs(p.getLastSeenTimestamp());
        }
    }

    @Override
    public BulkJobShardReport.Builder toProto() {
        BulkJobShardReport.Builder out = BulkJobShardReport.newBuilder()
                .setBulkJobId(bulkJobId.toProto())
                .setPartition(partition)
                .setCompleted(completed)
                .setLastSeenKey(lastSeenKey);
        if (lastSeenTimestamp != null) {
            out.setLastSeenTimestamp(LHUtil.fromDate(lastSeenTimestamp));
        }
        return out;
    }

    @Override
    public Class<BulkJobShardReport> getProtoBaseClass() {
        return BulkJobShardReport.class;
    }

    @Override
    public Message process(MetadataProcessorContext executionContext) {

        BulkJobModel bulkJobModel = executionContext.metadataManager().get(bulkJobId);
        bulkJobModel.updateShard(this);

        executionContext.metadataManager().put(bulkJobModel);
        return Empty.getDefaultInstance();
    }
}
