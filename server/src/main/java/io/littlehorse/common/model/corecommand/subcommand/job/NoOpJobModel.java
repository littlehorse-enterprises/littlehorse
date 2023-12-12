package io.littlehorse.common.model.corecommand.subcommand.job;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.NoOpJob;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.LHIterator;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class NoOpJobModel extends LHSerializable<NoOpJob> implements BulkJob {

    private ReadOnlyMetadataManager metadataManager;
    private LHIterator<WfSpecModel> range;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        metadataManager = context.metadataManager();
    }

    @Override
    public NoOpJob.Builder toProto() {
        return NoOpJob.newBuilder();
    }

    @Override
    public void init(String startKey, String endKey) {
        range = metadataManager.range(startKey, endKey, WfSpecModel.class);
    }

    @Override
    public boolean hasNext() {
        return range.hasNext();
    }

    @Override
    public String processOneRecord() {
        LHIterator.Entry<WfSpecModel> next = range.next();
        String key = next.key();
        WfSpecModel wfSpec = next.value();
        return key;
    }

    @Override
    public Class<NoOpJob> getProtoBaseClass() {
        return NoOpJob.class;
    }
}
