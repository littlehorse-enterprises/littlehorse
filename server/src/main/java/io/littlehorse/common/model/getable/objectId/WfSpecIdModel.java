package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import lombok.Getter;
import lombok.Setter;

// Used by WfSpec, TaskDef, and ExternalEventDef
@Getter
@Setter
public class WfSpecIdModel extends MetadataId<WfSpecId, WfSpec, WfSpecModel> {

    private String name;
    private int version;

    public WfSpecIdModel() {}

    public WfSpecIdModel(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public Class<WfSpecId> getProtoBaseClass() {
        return WfSpecId.class;
    }

    @Override
    public void initFrom(Message proto) {
        WfSpecId p = (WfSpecId) proto;
        version = p.getVersion();
        name = p.getName();
    }

    @Override
    public WfSpecId.Builder toProto() {
        WfSpecId.Builder out = WfSpecId.newBuilder().setVersion(version).setName(name);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public void initFromString(String key) {
        String[] split = key.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
    }

    // TODO: This leaks from Storeable.java
    public static String getPrefix(String name) {
        return GetableClassEnum.WF_SPEC.getNumber() + "/" + name + "/";
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC;
    }
}
