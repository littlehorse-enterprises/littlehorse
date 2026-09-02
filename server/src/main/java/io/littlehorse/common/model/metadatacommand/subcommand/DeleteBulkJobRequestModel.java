package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.sdk.common.proto.DeleteBulkJobRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class DeleteBulkJobRequestModel extends MetadataSubCommand<DeleteBulkJobRequest> {

    private BulkJobIdModel id;

    public Class<DeleteBulkJobRequest> getProtoBaseClass() {
        return DeleteBulkJobRequest.class;
    }

    public DeleteBulkJobRequest.Builder toProto() {
        return DeleteBulkJobRequest.newBuilder().setId(id.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteBulkJobRequest p = (DeleteBulkJobRequest) proto;
        id = LHSerializable.fromProto(p.getId(), BulkJobIdModel.class, context);
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public Empty process(MetadataProcessorContext context) {
        BulkJobModel existing = context.metadataManager().get(id);
        if (existing != null && existing.getStatus() == BulkJobStatus.BULK_JOB_RUNNING) {
            // Guard against deleting a job that is still running (e.g. if it transitioned back to
            // RUNNING between the API-level check and command processing).
            throw new LHApiException(Status.FAILED_PRECONDITION, "Cannot delete a RUNNING BulkJob");
        }
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    public static DeleteBulkJobRequestModel fromProto(DeleteBulkJobRequest p, ExecutionContext context) {
        DeleteBulkJobRequestModel out = new DeleteBulkJobRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
