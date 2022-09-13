package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.observability.ThreadStatusChangeOePb;

public class ThreadStatusChangeOe
  extends LHSerializable<ThreadStatusChangeOePb> {

  public int threadRunNumber;
  public LHStatusPb status;

  public ThreadStatusChangeOe(int threadRunNumber, LHStatusPb status) {
    this.status = status;
    this.threadRunNumber = threadRunNumber;
  }

  public ThreadStatusChangeOe() {}

  public Class<ThreadStatusChangeOePb> getProtoBaseClass() {
    return ThreadStatusChangeOePb.class;
  }

  public void initFrom(MessageOrBuilder proto) {
    ThreadStatusChangeOePb p = (ThreadStatusChangeOePb) proto;
    threadRunNumber = p.getThreadRunNumber();
    status = p.getStatus();
  }

  public static ThreadStatusChangeOe fromProto(ThreadStatusChangeOePb proto) {
    ThreadStatusChangeOe out = new ThreadStatusChangeOe();
    out.initFrom(proto);
    return out;
  }

  public ThreadStatusChangeOePb.Builder toProto() {
    ThreadStatusChangeOePb.Builder out = ThreadStatusChangeOePb
      .newBuilder()
      .setThreadRunNumber(threadRunNumber)
      .setStatus(status);

    return out;
  }
}
