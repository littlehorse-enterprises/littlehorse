package io.littlehorse.server.streamsbackend.storeinternals.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.DiscreteTagLocalCounterPb;
import io.littlehorse.common.proto.TagChangesToBroadcastPb;
import io.littlehorse.common.proto.TagChangesToBroadcastPbOrBuilder;
import java.util.HashMap;
import java.util.Map;

public class TagChangesToBroadcast extends LHSerializable<TagChangesToBroadcastPb> {

    public Map<String, DiscreteTagLocalCounter> changelog;
    public int partition;

    public TagChangesToBroadcast() {
        changelog = new HashMap<>();
    }

    public Class<TagChangesToBroadcastPb> getProtoBaseClass() {
        return TagChangesToBroadcastPb.class;
    }

    public TagChangesToBroadcastPb.Builder toProto() {
        TagChangesToBroadcastPb.Builder out = TagChangesToBroadcastPb.newBuilder();
        for (Map.Entry<String, DiscreteTagLocalCounter> e : changelog.entrySet()) {
            out.putChangelog(e.getKey(), e.getValue().toProto().build());
        }
        out.setPartition(partition);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TagChangesToBroadcastPbOrBuilder p = (TagChangesToBroadcastPbOrBuilder) proto;
        for (Map.Entry<String, DiscreteTagLocalCounterPb> e : p
            .getChangelogMap()
            .entrySet()) {
            changelog.put(
                e.getKey(),
                DiscreteTagLocalCounter.fromProto(e.getValue())
            );
        }
        partition = p.getPartition();
    }

    public static TagChangesToBroadcast fromProto(
        TagChangesToBroadcastPbOrBuilder p
    ) {
        TagChangesToBroadcast out = new TagChangesToBroadcast();
        out.initFrom(p);
        return out;
    }

    public DiscreteTagLocalCounter getCounter(Tag tag) {
        String tagAttributes = tag.getAttributeString();
        DiscreteTagLocalCounter out = changelog.get(tagAttributes);
        if (out != null) {
            if (out.partition != this.partition) {
                throw new RuntimeException("not possible");
            }
            return out;
        }

        out = new DiscreteTagLocalCounter();
        out.partition = this.partition;
        out.localCount = 0;
        out.tagAttributes = tagAttributes;

        changelog.put(tagAttributes, out);
        return out;
    }
}
