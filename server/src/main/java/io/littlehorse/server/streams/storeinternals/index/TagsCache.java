package io.littlehorse.server.streams.storeinternals.index;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TagsCachePb;
import io.littlehorse.common.proto.TagsCachePb.CachedTagPb;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagsCache extends LHSerializable<TagsCachePb> {

    public List<CachedTag> tags = new ArrayList<>();

    public TagsCache() {}

    public TagsCache(List<Tag> tags) {
        this.setTags(tags.stream().map(tag -> new CachedTag(tag)).toList());
    }

    public Class<TagsCachePb> getProtoBaseClass() {
        return TagsCachePb.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TagsCachePb p = (TagsCachePb) proto;
        for (CachedTagPb ct : p.getCachedTagsList()) {
            tags.add(CachedTag.fromProto(ct, context));
        }
    }

    public TagsCachePb.Builder toProto() {
        List<CachedTagPb> cachedTagPbs =
                tags.stream().map(cachedTag -> cachedTag.toProto().build()).toList();
        TagsCachePb.Builder out = TagsCachePb.newBuilder().addAllCachedTags(cachedTagPbs);
        return out;
    }

    public static TagsCache fromProto(TagsCachePb proto, ExecutionContext context) {
        TagsCache out = new TagsCache();
        out.initFrom(proto, context);
        return out;
    }

    public List<String> getTagIds() {
        return this.tags.stream().map(CachedTag::getId).toList();
    }
    // public void setTagIds(List<String> tagIds) {
    // this.tagIds = tagIds;
    // }
}
