package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;

public class PutTenantRequestModel extends MetadataSubCommand<PutTenantRequest> implements ClusterLevelCommand {
    private String id;
    private OutputTopicConfigModel outputTopicConfig;

    public PutTenantRequestModel() {}

    public PutTenantRequestModel(String id) {
        this.id = id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutTenantRequest putTenantRequest = (PutTenantRequest) proto;
        this.id = putTenantRequest.getId();
        if (putTenantRequest.hasOutputTopicConfig()) {
            this.outputTopicConfig = LHSerializable.fromProto(
                    putTenantRequest.getOutputTopicConfig(), OutputTopicConfigModel.class, context);
        }
    }

    @Override
    public PutTenantRequest.Builder toProto() {
        PutTenantRequest.Builder result = PutTenantRequest.newBuilder().setId(id);
        if (outputTopicConfig != null) {
            result.setOutputTopicConfig(this.outputTopicConfig.toProto());
        }
        return result;
    }

    @Override
    public Class<PutTenantRequest> getProtoBaseClass() {
        return PutTenantRequest.class;
    }

    @Override
    public Tenant process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();
        PrincipalModel caller =
                context.service().getPrincipal(context.authorization().principalId());
        if (!caller.canCreateTenants()) {
            throw new LHApiException(
                    Status.PERMISSION_DENIED,
                    String.format(
                            "Missing permission %s over resource %s.",
                            ACLAction.WRITE_METADATA, ACLResource.ACL_TENANT));
        }
        TenantModel tenant = metadataManager.get(new TenantIdModel(id));
        if (tenant == null) {
            if (this.id.isEmpty() || !LHUtil.isValidLHName(this.id) || Character.isDigit(this.id.charAt(0))) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Tenant id must be a valid hostname and not start with a digit");
            }
            tenant = new TenantModel(id);
            tenant.setCreatedAt(context.currentCommand().getTime());
        }
        tenant.setOutputTopicConfig(outputTopicConfig);
        metadataManager.put(tenant);
        if (outputTopicConfig != null && context.serverConfig().shouldCreateOutputTopics()) {
            final TenantModel finalTenant = tenant;
            Thread.startVirtualThread(() -> {
                context.maybeCreateOutputTopics(finalTenant);
            });
        }
        return tenant.toProto().build();
    }

    public String getId() {
        return this.id;
    }

    public OutputTopicConfigModel getOutputTopicConfig() {
        return this.outputTopicConfig;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setOutputTopicConfig(final OutputTopicConfigModel outputTopicConfig) {
        this.outputTopicConfig = outputTopicConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PutTenantRequestModel)) return false;
        final PutTenantRequestModel other = (PutTenantRequestModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$outputTopicConfig = this.getOutputTopicConfig();
        final Object other$outputTopicConfig = other.getOutputTopicConfig();
        if (this$outputTopicConfig == null
                ? other$outputTopicConfig != null
                : !this$outputTopicConfig.equals(other$outputTopicConfig)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PutTenantRequestModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $outputTopicConfig = this.getOutputTopicConfig();
        result = result * PRIME + ($outputTopicConfig == null ? 43 : $outputTopicConfig.hashCode());
        return result;
    }
}
