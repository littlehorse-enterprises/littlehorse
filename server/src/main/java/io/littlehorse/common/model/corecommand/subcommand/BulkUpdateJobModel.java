package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class BulkUpdateJobModel extends CoreSubCommand<BulkUpdateJob> {

    private int partitionKey;
    private String startKey;
    private String endKey;
    private String resumeFromKey;


    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        BulkUpdateJob p = (BulkUpdateJob) proto;
        this.partitionKey = p.getPartition();
        this.startKey = p.getStartKey();
        this.endKey = p.getEndKey();
        this.resumeFromKey = p.hasResumeFromKey() ? p.getResumeFromKey() : null;
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
        return builder;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {

        return null;
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return BulkUpdateJob.class;
    }

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public String getPartitionKey() {
        return String.valueOf(partitionKey); // ??
    }
}
