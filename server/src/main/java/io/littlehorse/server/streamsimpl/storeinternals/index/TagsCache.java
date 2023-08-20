package io.littlehorse.server.streamsimpl.storeinternals.index;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TagsCachePb;
import io.littlehorse.common.proto.TagsCachePb.CachedTagPb;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagsCache extends LHSerializable<TagsCachePb> {

    public List<CachedTag> tags = new ArrayList<>();

    public TagsCache() {}

    public TagsCache(List<CachedTag> tags) {
        this.tags = tags;
    }

    public Class<TagsCachePb> getProtoBaseClass() {
        return TagsCachePb.class;
    }

    public void initFrom(Message proto) {
        TagsCachePb p = (TagsCachePb) proto;
        for (CachedTagPb ct : p.getCachedTagsList()) {
            tags.add(CachedTag.fromProto(ct));
        }
    }

    public TagsCachePb.Builder toProto() {
        List<CachedTagPb> cachedTagPbs =
                tags.stream().map(cachedTag -> cachedTag.toProto().build()).toList();
        TagsCachePb.Builder out = TagsCachePb.newBuilder().addAllCachedTags(cachedTagPbs);
        return out;
    }

    public static TagsCache fromProto(TagsCachePb proto) {
        TagsCache out = new TagsCache();
        out.initFrom(proto);
        return out;
    }

    public List<String> getTagIds() {
        return this.tags.stream().map(CachedTag::getId).toList();
    }
    // public void setTagIds(List<String> tagIds) {
    //     this.tagIds = tagIds;
    // }
}
