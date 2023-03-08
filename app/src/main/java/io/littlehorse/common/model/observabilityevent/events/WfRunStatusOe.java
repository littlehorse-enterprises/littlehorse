package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.WfRunStatusOePb;
import io.littlehorse.jlib.common.proto.WfRunStatusOePbOrBuilder;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import java.util.Date;
import java.util.List;

public class WfRunStatusOe extends SubEvent<WfRunStatusOePb> {

    public LHStatusPb status;

    public Class<WfRunStatusOePb> getProtoBaseClass() {
        return WfRunStatusOePb.class;
    }

    public WfRunStatusOePb.Builder toProto() {
        WfRunStatusOePb.Builder out = WfRunStatusOePb.newBuilder().setStatus(status);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        WfRunStatusOePbOrBuilder p = (WfRunStatusOePbOrBuilder) proto;
        status = p.getStatus();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        WfRun wr = dao.getWfRun(wfRunId);

        List<WfMetricUpdate> wmus = dao.getWfMetricWindows(wr.wfSpecName, time);

        for (WfMetricUpdate wmu : wmus) {
            if (status == LHStatusPb.COMPLETED) {
                wmu.numEntries++;
                wmu.totalCompleted++;

                long startToComplete =
                    (wr.endTime.getTime() - wr.startTime.getTime());

                wmu.startToCompleteTotal += startToComplete;
                if (startToComplete > wmu.startToCompleteMax) {
                    wmu.startToCompleteMax = startToComplete;
                }
            } else if (status == LHStatusPb.ERROR) {
                wmu.totalErrored++;
                wmu.numEntries++;
            } else {
                // ignoring
            }
        }
    }
}
