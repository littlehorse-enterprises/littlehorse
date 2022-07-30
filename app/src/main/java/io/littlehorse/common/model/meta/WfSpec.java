package io.littlehorse.common.model.meta;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.RunStartOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.WFSpecPb;
import io.littlehorse.common.proto.WFSpecPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.scheduler.model.WfRunState;

public class WfSpec extends GETable<WFSpecPbOrBuilder> {
    public String id;
    public String name;
    public Date createdAt;
    public Date updatedAt;

    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    public String getStoreKey() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getPartitionKey() {
        return id;
    }

    public WfSpec() {
        threadSpecs = new HashMap<>();
    }

    public WFSpecPb.Builder toProto() {
        WFSpecPb.Builder out = WFSpecPb.newBuilder()
            .setId(id)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setUpdatedAt(LHUtil.fromDate(updatedAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpec> p: threadSpecs.entrySet()) {
                out.putThreadSpecs(
                    p.getKey(),
                    p.getValue().toProto().build()
                );
            }
        }

        return out;
    }

    public void initFrom(WFSpecPbOrBuilder proto) {
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        id = proto.getId();
        updatedAt = LHUtil.fromProtoTs(proto.getUpdatedAt());
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        name = proto.getName();

        for (
            Map.Entry<String, ThreadSpecPb> e: proto.getThreadSpecsMap().entrySet()
        ) {
            ThreadSpec ts = ThreadSpec.fromProto(e.getValue());
            ts.wfSpec = this;
            ts.name = e.getKey();
            threadSpecs.put(e.getKey(), ts);
        }
    }

    public Class<WFSpecPb> getProtoBaseClass() {
        return WFSpecPb.class;
    }

    public static WfSpec fromProto(WFSpecPbOrBuilder proto) {
        WfSpec out = new WfSpec();
        out.initFrom(proto);
        return out;
    }

    public WfRunState startNewRun(WFRunEvent e, List<TaskScheduleRequest> toSchedule) {
        WfRunState out = new WfRunState(e.runRequest.wfRunId);

        out.wfSpec = this;
        out.toSchedule = toSchedule;
        out.wfSpecId = id;
        out.wfSpecName = name;
        out.startTime = e.time;
        out.status = LHStatusPb.RUNNING;
        out.startTime = e.time;
        out.oEvents.add(new ObservabilityEvent(new RunStartOe(id), e.time));

        out.startThread(entrypointThreadName, e.time, null);

        return out;
    }
}
