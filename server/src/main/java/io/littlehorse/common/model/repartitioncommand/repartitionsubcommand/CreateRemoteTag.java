package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.CreateRemoteTagPb;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class CreateRemoteTag extends LHSerializable<CreateRemoteTagPb> implements RepartitionSubCommand {

    private Tag tag;

    public CreateRemoteTag() {}

    public CreateRemoteTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        repartitionedStore.put(tag);
    }

    @Override
    public String getPartitionKey() {
        return this.tag.getAttributeString();
    }

    @Override
    public CreateRemoteTagPb.Builder toProto() {
        return CreateRemoteTagPb.newBuilder().setTag(this.tag.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CreateRemoteTagPb remoteTagSubCommandPb = (CreateRemoteTagPb) proto;
        this.tag = LHSerializable.fromProto(remoteTagSubCommandPb.getTag(), Tag.class, context);
    }

    @Override
    public Class<? extends GeneratedMessage> getProtoBaseClass() {
        return CreateRemoteTagPb.class;
    }
}
