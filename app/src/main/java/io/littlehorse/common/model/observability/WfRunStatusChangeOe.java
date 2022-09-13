package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.observability.WfRunStatusChangeOePb;

public class WfRunStatusChangeOe extends LHSerializable<WfRunStatusChangeOePb> {

  public LHStatusPb status;

  public WfRunStatusChangeOePb.Builder toProto() {
    WfRunStatusChangeOePb.Builder out = WfRunStatusChangeOePb
      .newBuilder()
      .setStatus(status);

    return out;
  }

  public Class<WfRunStatusChangeOePb> getProtoBaseClass() {
    return WfRunStatusChangeOePb.class;
  }

  public WfRunStatusChangeOe() {}

  public void initFrom(MessageOrBuilder proto) {
    WfRunStatusChangeOePb p = (WfRunStatusChangeOePb) proto;
    status = p.getStatus();
  }

  public static WfRunStatusChangeOe fromProto(WfRunStatusChangeOePb proto) {
    WfRunStatusChangeOe out = new WfRunStatusChangeOe();
    out.initFrom(proto);
    return out;
  }

  public WfRunStatusChangeOe(LHStatusPb status) {
    this.status = status;
  }
}
