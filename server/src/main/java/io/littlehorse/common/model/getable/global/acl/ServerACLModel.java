package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerACLModel extends LHSerializable<ServerACL> {
    private List<ACLResource> resources = new ArrayList<>();
    private List<ACLAction> allowedActions = new ArrayList<>();
    private Optional<String> name = Optional.empty();
    private Optional<String> prefix = Optional.empty();
    public static final ACLResource ADMIN_RESOURCE = ACLResource.ACL_ALL_RESOURCES;
    public static final ACLAction ADMIN_ACTION = ACLAction.ALL_ACTIONS;

    public ServerACLModel() {}

    public ServerACLModel(final List<ACLResource> resources, final List<ACLAction> allowedActions) {
        this.resources = resources;
        this.allowedActions = allowedActions;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
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
        return resources.contains(ADMIN_RESOURCE) && allowedActions.contains(ADMIN_ACTION);
    }

    public boolean allows(ACLResource resource, ACLAction action) {
        boolean hasEnoughActions = (allowedActions.contains(action) || allowedActions.contains(ACLAction.ALL_ACTIONS));
        boolean hasEnoughResources =
                (resources.contains(resource) || resources.contains(ACLResource.ACL_ALL_RESOURCES));
        return hasEnoughActions && hasEnoughResources;
    }

    public List<ACLResource> getResources() {
        return this.resources;
    }

    public List<ACLAction> getAllowedActions() {
        return this.allowedActions;
    }

    public Optional<String> getName() {
        return this.name;
    }

    public Optional<String> getPrefix() {
        return this.prefix;
    }

    public void setResources(final List<ACLResource> resources) {
        this.resources = resources;
    }

    public void setAllowedActions(final List<ACLAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public void setName(final Optional<String> name) {
        this.name = name;
    }

    public void setPrefix(final Optional<String> prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "ServerACLModel(resources=" + this.getResources() + ", allowedActions=" + this.getAllowedActions()
                + ", name=" + this.getName() + ", prefix=" + this.getPrefix() + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerACLModel)) return false;
        final ServerACLModel other = (ServerACLModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$resources = this.getResources();
        final Object other$resources = other.getResources();
        if (this$resources == null ? other$resources != null : !this$resources.equals(other$resources)) return false;
        final Object this$allowedActions = this.getAllowedActions();
        final Object other$allowedActions = other.getAllowedActions();
        if (this$allowedActions == null
                ? other$allowedActions != null
                : !this$allowedActions.equals(other$allowedActions)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$prefix = this.getPrefix();
        final Object other$prefix = other.getPrefix();
        if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ServerACLModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $resources = this.getResources();
        result = result * PRIME + ($resources == null ? 43 : $resources.hashCode());
        final Object $allowedActions = this.getAllowedActions();
        result = result * PRIME + ($allowedActions == null ? 43 : $allowedActions.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $prefix = this.getPrefix();
        result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
        return result;
    }
}
