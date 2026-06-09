package io.littlehorse.server.streams.storeinternals.index;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TagsCachePb;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CachedTag extends LHSerializable<TagsCachePb.CachedTagPb> {

    private String id;
    private boolean isRemote;
    private boolean isCounted;

    public CachedTag() {}

    public CachedTag(Tag tag) {
        this.id = tag.getStoreKey();

        // When we re-enable remote tags, this will be more complex.
        this.isRemote = tag.isRemote();
        this.isCounted = tag.isCounted();
    }

    @Override
    public TagsCachePb.CachedTagPb.Builder toProto() {
        return TagsCachePb.CachedTagPb.newBuilder()
                .setId(id)
                .setIsRemote(isRemote)
                .setIsCounted(isCounted);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TagsCachePb.CachedTagPb cachedTagPb = (TagsCachePb.CachedTagPb) proto;
        this.id = cachedTagPb.getId();
        this.isRemote = cachedTagPb.getIsRemote();
        this.isCounted = cachedTagPb.getIsCounted();
    }

    public static CachedTag fromProto(TagsCachePb.CachedTagPb proto, ExecutionContext context) {
        CachedTag out = new CachedTag();
        out.initFrom(proto, context);
        return out;
    }

    public String getAttributeString() {
        String[] splittedStoreKey = id.split("/");
        return splittedStoreKey[0] + "/" + splittedStoreKey[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedTag cachedTag)) return false;
        return isRemote == cachedTag.isRemote && Objects.equals(id, cachedTag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isRemote);
    }

    @Override
    public Class<? extends GeneratedMessage> getProtoBaseClass() {
        return null;
    }
}
