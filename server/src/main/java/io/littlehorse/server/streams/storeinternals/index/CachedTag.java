package io.littlehorse.server.streams.storeinternals.index;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.proto.TagsCachePb;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CachedTag extends LHSerializable<TagsCachePb.CachedTagPb> {

    private String id;
    private boolean isRemote;

    public CachedTag() {}

    public CachedTag(String id, boolean isRemote) {
        this.id = id;
        this.isRemote = isRemote;
    }

    public CachedTag(Tag tag) {
        this.id = tag.getStoreKey();
        this.isRemote = tag.getTagStorageType() == TagStorageType.REMOTE;
    }

    @Override
    public TagsCachePb.CachedTagPb.Builder toProto() {
        return TagsCachePb.CachedTagPb.newBuilder().setId(id).setIsRemote(isRemote);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TagsCachePb.CachedTagPb cachedTagPb = (TagsCachePb.CachedTagPb) proto;
        this.id = cachedTagPb.getId();
        this.isRemote = cachedTagPb.getIsRemote();
    }

    public static CachedTag fromProto(TagsCachePb.CachedTagPb proto, ExecutionContext context) {
        CachedTag out = new CachedTag();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return null;
    }
}
