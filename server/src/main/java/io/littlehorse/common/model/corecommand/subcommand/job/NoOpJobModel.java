package io.littlehorse.common.model.corecommand.subcommand.job;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.NoOpJob;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;

public class NoOpJobModel extends LHSerializable<NoOpJob> implements AbstractBulkJob<WfRunModel> {

    private CoreProcessorContext processorContext;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        processorContext = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public NoOpJob.Builder toProto() {
        return NoOpJob.newBuilder();
    }

    @Override
    public Class<NoOpJob> getProtoBaseClass() {
        return NoOpJob.class;
    }

    @Override
    public void process(WfRunModel record) {
        processorContext.getableManager().delete(record.getId());
    }
}
