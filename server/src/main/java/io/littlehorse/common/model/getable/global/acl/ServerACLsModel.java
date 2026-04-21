package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;

public class ServerACLsModel extends LHSerializable<ServerACLs> {
    private List<ServerACLModel> acls = new ArrayList<>();

    @Override
    public Class<ServerACLs> getProtoBaseClass() {
        return ServerACLs.class;
    }

    @Override
    public ServerACLs.Builder toProto() {
        ServerACLs.Builder out = ServerACLs.newBuilder();
        for (ServerACLModel acl : acls) {
            out.addAcls(acl.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ServerACLs p = (ServerACLs) proto;
        for (ServerACL acl : p.getAclsList()) {
            acls.add(LHSerializable.fromProto(acl, ServerACLModel.class, context));
        }
    }

    /**
     * Checks whether this ACL permits the execution of
     * a specified ${@link ACLAction} on a given ${@link ACLResource}.
     * @param resource The resource to be verified.
     * @param action   The action to be verified.
     * @return True if the action on the resource is allowed; otherwise, false.
     */
    public boolean allows(ACLResource resource, ACLAction action) {
        return acls.stream().anyMatch(acl -> acl.allows(resource, action));
    }

    public List<ServerACLModel> getAcls() {
        return this.acls;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerACLsModel)) return false;
        final ServerACLsModel other = (ServerACLsModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$acls = this.getAcls();
        final Object other$acls = other.getAcls();
        if (this$acls == null ? other$acls != null : !this$acls.equals(other$acls)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ServerACLsModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $acls = this.getAcls();
        result = result * PRIME + ($acls == null ? 43 : $acls.hashCode());
        return result;
    }
}
