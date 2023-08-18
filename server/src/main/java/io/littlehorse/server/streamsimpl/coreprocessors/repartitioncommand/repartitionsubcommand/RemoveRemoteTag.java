package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.RemoveRemoteTagPb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class RemoveRemoteTag extends LHSerializable<RemoveRemoteTagPb> implements RepartitionSubCommand {

    private String storeKey;
    private String partitionKey;

    public RemoveRemoteTag() {}

    public RemoveRemoteTag(String storeKey, String partitionKey) {
        this.storeKey = storeKey;
        this.partitionKey = partitionKey;
    }

    @Override
    public RemoveRemoteTagPb.Builder toProto() {
        return RemoveRemoteTagPb.newBuilder().setStoreKey(this.storeKey).setPartitionKey(this.partitionKey);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        var removeRemoteTagSubCommand = (RemoveRemoteTagPb) proto;
        this.storeKey = removeRemoteTagSubCommand.getStoreKey();
        this.partitionKey = removeRemoteTagSubCommand.getPartitionKey();
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return RemoveRemoteTagPb.class;
    }

    @Override
    public void process(LHStoreWrapper repartitionedStore, ProcessorContext<Void, Void> ctx) {
        repartitionedStore.delete(this.storeKey, Tag.class);
    }

    @Override
    public String getPartitionKey() {
        return this.partitionKey;
    }
}
