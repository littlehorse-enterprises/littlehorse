package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.ServerACL;
import io.littlehorse.common.proto.ServerACLs;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
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
    public void initFrom(Message proto) {
        ServerACLs p = (ServerACLs) proto;
        for (ServerACL acl : p.getAclsList()) {
            acls.add(LHSerializable.fromProto(acl, ServerACLModel.class));
        }
    }
}
