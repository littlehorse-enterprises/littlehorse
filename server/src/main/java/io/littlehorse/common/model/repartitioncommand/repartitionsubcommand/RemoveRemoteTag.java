package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.RemoveRemoteTagPb;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        var removeRemoteTagSubCommand = (RemoveRemoteTagPb) proto;
        this.storeKey = removeRemoteTagSubCommand.getStoreKey();
        this.partitionKey = removeRemoteTagSubCommand.getPartitionKey();
    }

    @Override
    public Class<? extends GeneratedMessage> getProtoBaseClass() {
        return RemoveRemoteTagPb.class;
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        repartitionedStore.delete(this.storeKey, StoreableType.TAG);
    }

    @Override
    public String getPartitionKey() {
        return this.partitionKey;
    }
}
