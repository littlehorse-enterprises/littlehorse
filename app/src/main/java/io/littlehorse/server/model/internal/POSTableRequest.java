package io.littlehorse.server.model.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.POSTableRequestPb;
import io.littlehorse.common.proto.POSTableRequestPbOrBuilder;
import io.littlehorse.common.proto.RequestTypePb;

public class POSTableRequest extends LHSerializable<POSTableRequestPb> {
    public RequestTypePb type;
    public String storeKey;
    public String requestId;
    public byte[] payload;

    public Class<POSTableRequestPb> getProtoBaseClass() {
        return POSTableRequestPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        POSTableRequestPbOrBuilder proto = (POSTableRequestPbOrBuilder) p;
        type = proto.getType();
        storeKey = proto.getStoreKey();
        requestId = proto.getRequestId();
        payload = proto.hasPayload() ? proto.getPayload().toByteArray() : null;
    }

    public POSTableRequestPb.Builder toProto() {
        POSTableRequestPb.Builder out = POSTableRequestPb.newBuilder()
            .setType(type)
            .setStoreKey(storeKey)
            .setRequestId(requestId);

        if (payload != null) out.setPayload(ByteString.copyFrom(payload));

        return out;
    }
}
