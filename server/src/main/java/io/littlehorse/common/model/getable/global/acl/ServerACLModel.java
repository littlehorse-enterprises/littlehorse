package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.common.proto.ServerACL;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerACLModel extends LHSerializable<ServerACL> {

    private List<ACLResource> resources = new ArrayList<>();
    private List<ACLAction> allowedActions = new ArrayList<>();
    private Optional<String> name;
    private Optional<String> prefix;

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
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
}
