package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.PutQuotaRequest;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutQuotaRequestModel extends MetadataSubCommand<PutQuotaRequest> implements ClusterLevelCommand {

    private TenantIdModel tenant;
    private PrincipalIdModel principal;
    private int writeRequestsPerSecond;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutQuotaRequest p = (PutQuotaRequest) proto;
        if (!p.hasTenant()) {
            throw new LHSerdeException("PutQuotaRequest is missing tenant");
        }
        tenant = LHSerializable.fromProto(p.getTenant(), TenantIdModel.class, context);
        if (p.hasPrincipal()) {
            principal = LHSerializable.fromProto(p.getPrincipal(), PrincipalIdModel.class, context);
        }
        writeRequestsPerSecond = p.getWriteRequestsPerSecond();
    }

    @Override
    public PutQuotaRequest.Builder toProto() {
        PutQuotaRequest.Builder builder = PutQuotaRequest.newBuilder()
                .setTenant(tenant.toProto())
                .setWriteRequestsPerSecond(writeRequestsPerSecond);
        if (principal != null) {
            builder.setPrincipal(principal.toProto());
        }
        return builder;
    }

    @Override
    public Class<PutQuotaRequest> getProtoBaseClass() {
        return PutQuotaRequest.class;
    }

    @Override
    public Quota process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();
        PrincipalModel caller =
                context.service().getPrincipal(context.authorization().principalId());

        if (!caller.canEditQuotas()) {
            throw new LHApiException(
                    Status.PERMISSION_DENIED,
                    String.format(
                            "Missing permission %s over resource %s.",
                            ACLAction.WRITE_METADATA, ACLResource.ACL_QUOTA));
        }

        if (writeRequestsPerSecond <= 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "write_requests_per_second must be greater than zero");
        }

        TenantModel existingTenant = metadataManager.get(tenant);
        if (existingTenant == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Could not find tenant %s".formatted(tenant));
        }

        if (principal != null && context.service().getPrincipal(principal) == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Could not find principal %s".formatted(principal));
        }

        QuotaIdModel quotaId = new QuotaIdModel(tenant, principal);
        QuotaModel quota = metadataManager.get(quotaId);
        if (quota == null) {
            quota = new QuotaModel(quotaId);
            quota.setCreatedAt(context.currentCommand().getTime());
        }

        quota.setWriteRequestsPerSecond(writeRequestsPerSecond);
        metadataManager.put(quota);
        return quota.toProto().build();
    }
}
