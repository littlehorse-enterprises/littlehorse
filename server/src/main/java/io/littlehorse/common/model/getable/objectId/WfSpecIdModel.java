package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
// Used by WfSpec, TaskDef, and ExternalEventDef

public class WfSpecIdModel extends MetadataId<WfSpecId, WfSpec, WfSpecModel> {
    private String name;
    private int majorVersion;
    private int revision;

    public WfSpecIdModel() {}

    public WfSpecIdModel(String name, int majorVersion, int revision) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.revision = revision;
    }

    public WfSpecIdModel(String name) {
        this.name = name;
    }

    @Override
    public Class<WfSpecId> getProtoBaseClass() {
        return WfSpecId.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WfSpecId p = (WfSpecId) proto;
        majorVersion = p.getMajorVersion();
        revision = p.getRevision();
        name = p.getName();
    }

    @Override
    public WfSpecId.Builder toProto() {
        WfSpecId.Builder out = WfSpecId.newBuilder()
                .setMajorVersion(majorVersion)
                .setName(name)
                .setRevision(revision);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(
                name, LHUtil.toLHDbVersionFormat(majorVersion), LHUtil.toLHDbVersionFormat(revision));
    }

    public void initFromString(String key) {
        String[] split = key.split("/");
        name = split[0];
        majorVersion = Integer.valueOf(split[1]);
        revision = Integer.valueOf(split[2]);
    }

    // TODO: This leaks from Storeable.java
    public static String getPrefix(String name) {
        return GetableClassEnum.WF_SPEC.getNumber() + "/" + name + "/";
    }

    public static String getPrefix(String name, int majorVersion) {
        return GetableClassEnum.WF_SPEC.getNumber() + "/" + name + "/" + LHUtil.toLHDbVersionFormat(majorVersion) + "/";
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_SPEC;
    }

    public String getName() {
        return this.name;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public int getRevision() {
        return this.revision;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setMajorVersion(final int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void setRevision(final int revision) {
        this.revision = revision;
    }
}
