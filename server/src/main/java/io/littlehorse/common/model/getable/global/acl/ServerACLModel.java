package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ServerACLModel extends LHSerializable<ServerACL> {

    private List<ACLResource> resources = new ArrayList<>();
    private List<ACLAction> allowedActions = new ArrayList<>();
    private Optional<String> name = Optional.empty();
    private Optional<String> prefix = Optional.empty();

    public ServerACLModel() {}

    public ServerACLModel(final List<ACLResource> resources, final List<ACLAction> allowedActions) {
        this.resources = resources;
        this.allowedActions = allowedActions;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ServerACL serverACL = (ServerACL) proto;
        if (serverACL.hasName()) {
            this.name = Optional.of(serverACL.getName());
        } else {
            this.prefix = Optional.of(serverACL.getPrefix());
        }
        this.resources = serverACL.getResourcesList();
        this.allowedActions = serverACL.getAllowedActionsList();
    }

    @Override
    public ServerACL.Builder toProto() {
        ServerACL.Builder out = ServerACL.newBuilder();
        name.ifPresent(out::setName);
        prefix.ifPresent(out::setName);
        out.addAllResources(resources);
        out.addAllAllowedActions(allowedActions);
        return out;
    }

    @Override
    public Class<ServerACL> getProtoBaseClass() {
        return ServerACL.class;
    }

    public boolean isAdmin() {
        return resources.contains(ACLResource.ACL_ALL_RESOURCES) && allowedActions.contains(ACLAction.ALL_ACTIONS);
    }
}
