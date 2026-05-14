package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.CountedTag;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CountedTagModel extends Storeable<CountedTag> {

    private String attibuteString;
    private long count;

    public CountedTagModel() {}

    public CountedTagModel(String attibuteString) {
        this.attibuteString = attibuteString;
        this.count = 0L;
    }

    @Override
    public CountedTag.Builder toProto() {
        return CountedTag.newBuilder().setAttributeString(attibuteString).setCount(count);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountedTag countedTag = (CountedTag) proto;
        attibuteString = countedTag.getAttributeString();
        count = countedTag.getCount();
    }

    @Override
    public Class<CountedTag> getProtoBaseClass() {
        return CountedTag.class;
    }

    @Override
    public String getStoreKey() {
        return attibuteString;
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

    public void decrement() {
        count--;
    }
}
