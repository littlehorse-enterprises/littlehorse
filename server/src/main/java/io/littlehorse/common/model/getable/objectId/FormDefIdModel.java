package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.formdef.FormDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormDef;
import io.littlehorse.sdk.common.proto.FormDefId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class FormDefIdModel extends MetadataId<FormDefId, FormDef, FormDefModel> {

    private String name;
    private int version;

    public FormDefIdModel() {}

    public FormDefIdModel(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public FormDefId.Builder toProto() {
        return FormDefId.newBuilder().setName(name).setVersion(version);
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        FormDefId proto = (FormDefId) p;
        name = proto.getName();
        version = proto.getVersion();
    }

    public static String getPrefix(String name) {
        return GetableClassEnum.FORM_DEF.getNumber() + "/" + name + "/";
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    @Override
    public void initFromString(String key) {
        String[] split = key.split("/");
        name = split[0];
        version = Integer.parseInt(split[1]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.FORM_DEF;
    }

    @Override
    public Class<FormDefId> getProtoBaseClass() {
        return FormDefId.class;
    }

    public FormDefIdModel bumpVersion() {
        return new FormDefIdModel(name, version + 1);
    }

    public static FormDefIdModel fromProto(FormDefId p, ExecutionContext context) {
        FormDefIdModel out = new FormDefIdModel();
        out.initFrom(p, context);
        return out;
    }
}
