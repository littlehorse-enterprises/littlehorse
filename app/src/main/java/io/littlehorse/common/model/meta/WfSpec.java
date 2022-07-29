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
import io.littlehorse.common.model.scheduler.WfRun;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.WFSpecPb;
import io.littlehorse.common.proto.WFSpecPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class WfSpec extends GETable {
    public String id;
    public String name;
    public Date createdAt;
    public Date updatedAt;

    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    public WfSpec() {
        threadSpecs = new HashMap<>();
    }

    public WFSpecPb.Builder toProtoBuilder() {
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
                    p.getValue().toProtoBuilder().build()
                );
            }
        }

        return out;
    }

    public static WfSpec fromProto(WFSpecPbOrBuilder proto) {
        WfSpec out = new WfSpec();
        out.id = proto.getId();
        out.createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        out.updatedAt = LHUtil.fromProtoTs(proto.getUpdatedAt());
        out.entrypointThreadName = proto.getEntrypointThreadName();
        out.status = proto.getStatus();
        out.name = proto.getName();

        for (
            Map.Entry<String, ThreadSpecPb> e: proto.getThreadSpecsMap().entrySet()
        ) {
            ThreadSpec ts = ThreadSpec.fromProto(e.getValue());
            ts.wfSpec = out;
            ts.name = e.getKey();
            out.threadSpecs.put(e.getKey(), ts);
        }
        return out;
    }

    public WfRun startNewRun(WFRunEvent e, List<TaskScheduleRequest> toSchedule) {
        WfRun out = new WfRun(e.runRequest.wfRunId);

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
