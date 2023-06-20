package io.littlehorse.server.streamsimpl.storeinternals.index;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.DiscreteTagLocalCounterPb;

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

    public void initFrom(Message proto) {
        DiscreteTagLocalCounterPb p = (DiscreteTagLocalCounterPb) proto;
        localCount = p.getLocalCount();
        tagAttributes = p.getTagAttributes();
        partition = p.getPartition();
    }

    public static String getObjectId(String tagAttributes, int partition) {
        return tagAttributes + "_" + partition;
    }

    public String getStoreKey() {
        return getObjectId(tagAttributes, partition);
    }

    public static DiscreteTagLocalCounter fromProto(DiscreteTagLocalCounterPb p) {
        DiscreteTagLocalCounter out = new DiscreteTagLocalCounter();
        out.initFrom(p);
        return out;
    }
}
