package io.littlehorse.common.model.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TagsCachePb;
import io.littlehorse.common.proto.TagsCachePbOrBuilder;
import java.util.ArrayList;
import java.util.List;

public class TagsCache extends LHSerializable<TagsCachePb> {

    public List<String> tagIds;

    public TagsCache() {
        this.tagIds = new ArrayList<>();
    }

    public Class<TagsCachePb> getProtoBaseClass() {
        return TagsCachePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TagsCachePbOrBuilder p = (TagsCachePbOrBuilder) proto;
        for (String tagId : p.getTagIdsList()) {
            tagIds.add(tagId);
        }
    }

    public TagsCachePb.Builder toProto() {
        TagsCachePb.Builder out = TagsCachePb.newBuilder();
        for (String tagId : tagIds) {
            out.addTagIds(tagId);
        }
        return out;
    }

    public static TagsCache fromProto(TagsCachePbOrBuilder proto) {
        TagsCache out = new TagsCache();
        out.initFrom(proto);
        return out;
    }
}
