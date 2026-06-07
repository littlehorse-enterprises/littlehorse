package io.littlehorse.common.model.corecommand.subcommand.job;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.proto.BulkDeleteWfRunJob;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkDeleteWfRunJobModel extends LHSerializable<BulkDeleteWfRunJob> implements AbstractBulkJob<WfRunModel> {

    private BulkJobIdModel bulkJobId;
    private BulkDeleteWfRun criteria;
    private CoreProcessorContext processorContext;

    public BulkDeleteWfRunJobModel() {}

    public BulkDeleteWfRunJobModel(BulkJobIdModel bulkJobId, BulkDeleteWfRun criteria) {
        this.bulkJobId = bulkJobId;
        this.criteria = criteria;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        BulkDeleteWfRunJob p = (BulkDeleteWfRunJob) proto;
        this.bulkJobId = LHSerializable.fromProto(p.getBulkJobId(), BulkJobIdModel.class, context);
        this.criteria = p.getCriteria();
        this.processorContext = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public BulkDeleteWfRunJob.Builder toProto() {
        return BulkDeleteWfRunJob.newBuilder()
                .setBulkJobId(bulkJobId.toProto())
                .setCriteria(criteria);
    }

    @Override
    public Class<BulkDeleteWfRunJob> getProtoBaseClass() {
        return BulkDeleteWfRunJob.class;
    }

    @Override
    public void process(WfRunModel record) {
        processorContext.getableManager().delete(record.getId());
    }
}

