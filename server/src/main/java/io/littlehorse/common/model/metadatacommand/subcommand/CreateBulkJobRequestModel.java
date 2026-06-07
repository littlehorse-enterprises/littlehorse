package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.CreateBulkJobRequest;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBulkJobRequestModel extends MetadataSubCommand<CreateBulkJobRequest> {

    private String id;
    private BulkDeleteWfRun bulkDeleteWfRun;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CreateBulkJobRequest p = (CreateBulkJobRequest) proto;
        if (p.hasId()) {
            id = p.getId();
        }
        if (p.hasBulkDeleteWfRun()) {
            bulkDeleteWfRun = p.getBulkDeleteWfRun();
        }
    }

    @Override
    public CreateBulkJobRequest.Builder toProto() {
        CreateBulkJobRequest.Builder builder = CreateBulkJobRequest.newBuilder();
        if (id != null) {
            builder.setId(id);
        }
        if (bulkDeleteWfRun != null) {
            builder.setBulkDeleteWfRun(bulkDeleteWfRun);
        }
        return builder;
    }

    @Override
    public Class<CreateBulkJobRequest> getProtoBaseClass() {
        return CreateBulkJobRequest.class;
    }

    @Override
    public BulkJob process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();

        String bulkJobId = (id != null) ? id : LHUtil.generateGuid();
        BulkJobIdModel idModel = new BulkJobIdModel(bulkJobId);

        // Idempotency check: if the job already exists, return it
        BulkJobModel existing = metadataManager.get(idModel);
        if (existing != null) {
            return existing.toProto().build();
        }

        BulkJobModel bulkJob = new BulkJobModel(idModel, bulkDeleteWfRun);
        metadataManager.put(bulkJob);

        return bulkJob.toProto().build();
    }
}

