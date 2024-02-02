package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

public class PrincipalIdModel extends ClusterMetadataId<PrincipalId, Principal, PrincipalModel> {
    @Getter
    private String id;

    public PrincipalIdModel() {}

    public PrincipalIdModel(String id) {
        this.id = id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PrincipalId principalId = (PrincipalId) proto;
        this.id = principalId.getId();
    }

    @Override
    public PrincipalId.Builder toProto() {
        return PrincipalId.newBuilder().setId(id);
    }

    @Override
    public Class<PrincipalId> getProtoBaseClass() {
        return PrincipalId.class;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        this.id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.PRINCIPAL;
    }
}
