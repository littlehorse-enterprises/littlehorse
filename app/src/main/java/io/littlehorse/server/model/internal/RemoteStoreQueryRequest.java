package io.littlehorse.server.model.internal;

import org.apache.kafka.streams.KeyQueryMetadata;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.RemoteStoreQueryRequestPb;
import io.littlehorse.common.proto.server.RemoteStoreQueryRequestPbOrBuilder;

public class RemoteStoreQueryRequest extends LHSerializable<RemoteStoreQueryRequestPb> {
    public String storeName;
    public int partition;
    public String storeKey;
    public boolean isActiveStore;

    public RemoteStoreQueryRequest() {}

    public RemoteStoreQueryRequest(
        KeyQueryMetadata meta, String storeName, String storeKey, boolean isActiveStore
    ) {
        this.partition = meta.partition();
        this.storeName = storeName;
        this.storeKey = storeKey;
        this.isActiveStore = isActiveStore;
    }

    public Class<RemoteStoreQueryRequestPb> getProtoBaseClass() {
        return RemoteStoreQueryRequestPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        RemoteStoreQueryRequestPbOrBuilder p = (RemoteStoreQueryRequestPbOrBuilder) proto;
        storeName = p.getStoreName();
        storeKey = p.getStoreKey();
        partition = p.getPartition();
        isActiveStore = p.getIsActiveStore();
    }

    public RemoteStoreQueryRequestPb.Builder toProto() {
        RemoteStoreQueryRequestPb.Builder out = RemoteStoreQueryRequestPb.newBuilder()
            .setIsActiveStore(isActiveStore)
            .setStoreKey(storeKey)
            .setStoreName(storeName)
            .setPartition(partition);
        return out;
    }
}
