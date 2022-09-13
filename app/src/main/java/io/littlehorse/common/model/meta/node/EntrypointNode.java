package io.littlehorse.common.model.meta.node;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.wfspec.EntrypointNodePb;

public class EntrypointNode extends LHSerializable<EntrypointNodePb> {

  public Class<EntrypointNodePb> getProtoBaseClass() {
    return EntrypointNodePb.class;
  }

  public EntrypointNodePb.Builder toProto() {
    return EntrypointNodePb.newBuilder();
  }

  public void initFrom(MessageOrBuilder proto) {}
}
