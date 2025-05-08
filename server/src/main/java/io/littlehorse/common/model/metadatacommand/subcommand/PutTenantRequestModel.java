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
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Tenant process(MetadataCommandExecution context) {
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
            if (Pattern.matches(".*[\\\\/].*", this.id)) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "/ and \\ are not valid characters for Tenant");
            }
            tenant = new TenantModel(id);
            tenant.setCreatedAt(context.currentCommand().getTime());
        }
        tenant.setOutputTopicConfig(outputTopicConfig);
        metadataManager.put(tenant);
        if (outputTopicConfig != null) {
            context.maybeCreateOutputTopic(tenant);
        }
        return tenant.toProto().build();
    }
}
