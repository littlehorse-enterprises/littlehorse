package io.littlehorse.server.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.scheduler.LHTimerPb;
import io.littlehorse.common.proto.scheduler.LHTimerPb.PayloadCase;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class LHTimer extends LHSerializable<LHTimerPb> {

  public Date maturationTime;
  public String topic;
  public String key;
  public WfRunEvent wfRunEvent;
  public PayloadCase type;

  public LHTimer() {}

  public LHTimer(WfRunEvent wfRunEvt, Date maturationTime) {
    topic = LHConstants.WF_RUN_EVENT_TOPIC;
    key = wfRunEvt.wfRunId;
    this.maturationTime = maturationTime;
    this.wfRunEvent = wfRunEvt;
    this.type = PayloadCase.WF_RUN_EVENT;
  }

  public void initFrom(MessageOrBuilder proto) {
    LHTimerPb p = (LHTimerPb) proto;
    maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
    topic = p.getTopic();
    key = p.getKey();
    type = p.getPayloadCase();

    switch (type) {
      case WF_RUN_EVENT:
        wfRunEvent = new WfRunEvent();
        wfRunEvent.initFrom(p.getWfRunEvent());
        break;
      case PAYLOAD_NOT_SET:
      // Not possible
    }
  }

  public LHTimerPb.Builder toProto() {
    LHTimerPb.Builder out = LHTimerPb
      .newBuilder()
      .setMaturationTime(LHUtil.fromDate(maturationTime))
      .setKey(key)
      .setTopic(topic);

    switch (type) {
      case WF_RUN_EVENT:
        out.setWfRunEvent(wfRunEvent.toProto());
        break;
      case PAYLOAD_NOT_SET:
      // Not possible
    }

    return out;
  }

  public String getStoreKey() {
    return LHUtil.toLhDbFormat(maturationTime) + "_" + topic + "_" + key;
  }

  @JsonIgnore
  public Class<LHTimerPb> getProtoBaseClass() {
    return LHTimerPb.class;
  }

  @JsonIgnore
  public byte[] getPayload(LHConfig config) {
    switch (type) {
      case WF_RUN_EVENT:
        return wfRunEvent.toBytes(config);
      case PAYLOAD_NOT_SET:
      default:
        return null;
    }
  }
}
