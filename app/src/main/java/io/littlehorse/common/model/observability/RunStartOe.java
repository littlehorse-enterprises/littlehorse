package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.observability.RunStartOePb;

public class RunStartOe extends LHSerializable<RunStartOePb> {

  public String wfSpecId;
  public String wfSpecName;

  public RunStartOePb.Builder toProto() {
    return RunStartOePb
      .newBuilder()
      .setWfSpecId(wfSpecId)
      .setWfSpecName(wfSpecName);
  }

  public RunStartOe() {}

  public RunStartOe(String wfSpecId, String wfSpecName) {
    this.wfSpecId = wfSpecId;
    this.wfSpecName = wfSpecName;
  }

  public Class<RunStartOePb> getProtoBaseClass() {
    return RunStartOePb.class;
  }

  public void initFrom(MessageOrBuilder proto) {
    RunStartOePb p = (RunStartOePb) proto;
    wfSpecId = p.getWfSpecId();
    wfSpecName = p.getWfSpecName();
  }

  public static RunStartOe fromProto(RunStartOePb proto) {
    RunStartOe out = new RunStartOe();
    out.initFrom(proto);
    return out;
  }
  // No need to implement loading from protobuf since this repo only writes.
}
