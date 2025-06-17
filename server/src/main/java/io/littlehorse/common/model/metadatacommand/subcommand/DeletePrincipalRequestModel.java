package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeletePrincipalRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeletePrincipalRequestModel extends MetadataSubCommand<DeletePrincipalRequest>
        implements ClusterLevelCommand {

    private PrincipalIdModel id;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeletePrincipalRequest deleteRequest = (DeletePrincipalRequest) proto;
        this.id = LHSerializable.fromProto(deleteRequest.getId(), PrincipalIdModel.class, context);
    }

    @Override
    public DeletePrincipalRequest.Builder toProto() {
        return DeletePrincipalRequest.newBuilder().setId(id.toProto());
    }

    @Override
    public Class<DeletePrincipalRequest> getProtoBaseClass() {
        return DeletePrincipalRequest.class;
    }

    @Override
    public Empty process(MetadataProcessorContext context) {
        if (id.getId().equals(LHConstants.ANONYMOUS_PRINCIPAL)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Deleting the anonymous principal is not allowed. However, "
                            + "you can remove its permissions via the PutPrincipal request.");
        }

        if (id.equals(context.authorization().principalId())) {
            throw new LHApiException(Status.PERMISSION_DENIED, "Cannot delete your own principal");
        }

        PrincipalModel caller =
                context.service().getPrincipal(context.authorization().principalId());

        ensureThatCallerCanEditPrincipalsInRelevantTenants(context, caller);

        ensureThatThereIsStillAnAdminPrincipal(context, caller);

        log.trace("deleting principal {}", id);
        context.metadataManager().delete(id);
        return Empty.getDefaultInstance();
    }

    private void ensureThatThereIsStillAnAdminPrincipal(MetadataProcessorContext ctx, PrincipalModel caller) {
        if (caller.isAdmin()) {
            // Since the caller is admin, and we do not delete the Caller, we are fine.
            return;
        }

        PrincipalModel toDelete = ctx.service().getPrincipal(id);
        if (toDelete == null || !toDelete.isAdmin()) {
            // We're not removing an admin principal, so we're good.
            //
            // Also, note that the LH API treats a delete on a nonexistent resource as "OK"
            return;
        }

        Collection<PrincipalIdModel> adminPrincipals = ctx.service().adminPrincipalIds();
        if (adminPrincipals.size() == 1) {
            // Then we know that we are deleting the last admin Principal.
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Cannot delete the last admin principal %s".formatted(id.getId()));
        }
    }

    private void ensureThatCallerCanEditPrincipalsInRelevantTenants(
            MetadataProcessorContext ctx, PrincipalModel caller) {
        if (!caller.hasPermissionToEditPrincipals()) {
            throw new LHApiException(Status.PERMISSION_DENIED, "You do not have permission to delete Principals.");
        }
    }
}
