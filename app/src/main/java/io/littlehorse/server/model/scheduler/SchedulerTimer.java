package io.littlehorse.server.model.scheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.scheduler.SchedulerTimerPb;
import io.littlehorse.common.util.LHUtil;

public class SchedulerTimer extends LHSerializable<SchedulerTimerPb> {
    public Date maturationTime;
    public String wfRunId;
    public WfRunEvent event;

    public void initFrom(MessageOrBuilder proto) {
        SchedulerTimerPb p = (SchedulerTimerPb) proto;
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());
        wfRunId = p.getWfRunId();
        event = new WfRunEvent();
        event.initFrom(p.getEvent());
    }

    public SchedulerTimerPb.Builder toProto() {
        SchedulerTimerPb.Builder out = SchedulerTimerPb.newBuilder()
            .setMaturationTime(LHUtil.fromDate(maturationTime))
            .setWfRunId(wfRunId)
            .setEvent(event.toProto());

        return out;
    }

    public String getStoreKey() {
        return LHUtil.toLhDbFormat(maturationTime) + "_" + wfRunId;
    }

    @JsonIgnore public Class<SchedulerTimerPb> getProtoBaseClass() {
        return SchedulerTimerPb.class;
    }
}
