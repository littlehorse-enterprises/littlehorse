package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.CountedTag;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CountedTagModel extends Storeable<CountedTag> {

    private String attributeString;
    private long count;

    public CountedTagModel() {}

    public CountedTagModel(String attributeString) {
        this.attributeString = attributeString;
        this.count = 0L;
    }

    @Override
    public CountedTag.Builder toProto() {
        return CountedTag.newBuilder().setAttributeString(attributeString).setCount(count);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountedTag countedTag = (CountedTag) proto;
        attributeString = countedTag.getAttributeString();
        count = countedTag.getCount();
    }

    @Override
    public Class<CountedTag> getProtoBaseClass() {
        return CountedTag.class;
    }

    @Override
    public String getStoreKey() {
        return attributeString;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.COUNTED_TAG;
    }

    public long getCount() {
        return count;
    }

    public void increment() {
        count++;
    }

    public void increment(long increment) {
        count += increment;
    }

    public void decrement() {
        count--;
    }

    public void decrement(long decrement) {
        count -= decrement;
    }
}
