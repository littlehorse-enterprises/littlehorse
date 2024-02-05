package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
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
}
