package io.littlehorse.server.streamsbackend.storeinternals.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.DiscreteTagLocalCounterPb;
import io.littlehorse.common.proto.DiscreteTagLocalCounterPbOrBuilder;

public class DiscreteTagLocalCounter extends Storeable<DiscreteTagLocalCounterPb> {

    public String tagAttributes;
    public int partition;
    public long localCount;

    public Class<DiscreteTagLocalCounterPb> getProtoBaseClass() {
        return DiscreteTagLocalCounterPb.class;
    }

    public DiscreteTagLocalCounter() {}

    public DiscreteTagLocalCounterPb.Builder toProto() {
        DiscreteTagLocalCounterPb.Builder out = DiscreteTagLocalCounterPb.newBuilder();
        return out
            .setTagAttributes(tagAttributes)
            .setLocalCount(localCount)
            .setPartition(partition);
    }

    public void initFrom(MessageOrBuilder proto) {
        DiscreteTagLocalCounterPbOrBuilder p = (DiscreteTagLocalCounterPbOrBuilder) proto;
        localCount = p.getLocalCount();
        tagAttributes = p.getTagAttributes();
        partition = p.getPartition();
    }

    public static String getObjectId(String tagAttributes, int partition) {
        return tagAttributes + "_" + partition;
    }

    public String getObjectId() {
        return getObjectId(tagAttributes, partition);
    }

    public static DiscreteTagLocalCounter fromProto(
        DiscreteTagLocalCounterPbOrBuilder p
    ) {
        DiscreteTagLocalCounter out = new DiscreteTagLocalCounter();
        out.initFrom(p);
        return out;
    }
}
