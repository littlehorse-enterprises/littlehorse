package io.littlehorse.scheduler.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.scheduler.SchedulerTimerPb;
import io.littlehorse.common.proto.scheduler.SchedulerTimerPb.TimerMessageCase;
import io.littlehorse.common.util.LHUtil;

public class SchedulerTimer extends LHSerializable<SchedulerTimerPb> {
    public Date maturationTime;
    public TimerMessageCase type;
    public TaskTimeout taskTimeout;

    public void initFrom(MessageOrBuilder proto) {
        SchedulerTimerPb p = (SchedulerTimerPb) proto;
        type = p.getTimerMessageCase();
        maturationTime = LHUtil.fromProtoTs(p.getMaturationTime());

        switch(type) {
        case TASK_TIMEOUT:
            taskTimeout = new TaskTimeout();
            taskTimeout.initFrom(p.getTaskTimeout());
            break;

        case TIMERMESSAGE_NOT_SET:
            // nothing to do.
        }
    }

    public SchedulerTimerPb.Builder toProto() {
        SchedulerTimerPb.Builder out = SchedulerTimerPb.newBuilder()
            .setMaturationTime(LHUtil.fromDate(maturationTime));

        return out;
    }

    @JsonIgnore public Class<SchedulerTimerPb> getProtoBaseClass() {
        return SchedulerTimerPb.class;
    }
}
