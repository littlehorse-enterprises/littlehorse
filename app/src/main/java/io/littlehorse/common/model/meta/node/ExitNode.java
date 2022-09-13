package io.littlehorse.common.model.meta.node;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ExitNodePb;

public class ExitNode extends LHSerializable<ExitNodePb> {

  public Class<ExitNodePb> getProtoBaseClass() {
    return ExitNodePb.class;
  }

  public void initFrom(MessageOrBuilder proto) {}

  public ExitNodePb.Builder toProto() {
    return ExitNodePb.newBuilder();
  }
}
