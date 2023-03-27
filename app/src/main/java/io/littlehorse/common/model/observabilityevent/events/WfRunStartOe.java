package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.WfRunStartOePb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import java.util.Date;
import java.util.List;

public class WfRunStartOe extends SubEvent<WfRunStartOePb> {

    public int wfSpecVersion;
    public String wfSpecName;

    public Class<WfRunStartOePb> getProtoBaseClass() {
        return WfRunStartOePb.class;
    }

    public WfRunStartOePb.Builder toProto() {
        WfRunStartOePb.Builder out = WfRunStartOePb
            .newBuilder()
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion);
        return out;
    }

    public void initFrom(Message proto) {
        WfRunStartOePb p = (WfRunStartOePb) proto;
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        List<WfMetricUpdate> wmus = dao.getWfMetricWindows(wfSpecName, time);

        for (WfMetricUpdate wmu : wmus) {
            wmu.numEntries++;
            wmu.totalStarted++;
        }
    }
}
