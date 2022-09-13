package io.littlehorse.common.model.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.RemoteStoreQueryResponsePb;
import io.littlehorse.common.proto.RemoteStoreQueryResponsePbOrBuilder;
import io.littlehorse.common.proto.RemoteStoreQueryStatusPb;

public class RemoteStoreQueryResponse
  extends LHSerializable<RemoteStoreQueryResponsePb> {

  public RemoteStoreQueryStatusPb code;
  public byte[] result;
  public long approximateLag;

  public Class<RemoteStoreQueryResponsePb> getProtoBaseClass() {
    return RemoteStoreQueryResponsePb.class;
  }

  public void initFrom(MessageOrBuilder proto) {
    RemoteStoreQueryResponsePbOrBuilder p = (RemoteStoreQueryResponsePbOrBuilder) proto;
    code = p.getCode();
    if (p.hasResult()) result = p.getResult().toByteArray();
    approximateLag = p.getApproximateLag();
  }

  public RemoteStoreQueryResponsePb.Builder toProto() {
    RemoteStoreQueryResponsePb.Builder out = RemoteStoreQueryResponsePb
      .newBuilder()
      .setApproximateLag(approximateLag)
      .setCode(code);

    if (result != null) {
      out.setResult(ByteString.copyFrom(result));
    }

    return out;
  }

  @JsonIgnore
  public boolean isValid() {
    return (
      code == RemoteStoreQueryStatusPb.RSQ_OK ||
      code == RemoteStoreQueryStatusPb.RSQ_NOT_FOUND
    );
  }
}
