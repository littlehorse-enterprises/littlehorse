package io.littlehorse.common.model.meta;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.POSTable;
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
import io.littlehorse.server.model.internal.IndexEntry;


public class WfSpec extends POSTable<WFSpecPbOrBuilder> {
    public String id;
    public String name;
    public Date createdAt;
    public Date updatedAt;
    public long lastOffset;


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

    public long getLastUpdatedOffset() {
        return lastOffset;
    }

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    public WFSpecPb.Builder toProto() {
        WFSpecPb.Builder out = WFSpecPb.newBuilder()
            .setId(id)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setUpdatedAt(LHUtil.fromDate(updatedAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status)
            .setLastUpdatedOffset(lastOffset);

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

    public void initFrom(MessageOrBuilder pr) {
        WFSpecPbOrBuilder proto = (WFSpecPbOrBuilder) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        id = proto.getId();
        updatedAt = LHUtil.fromProtoTs(proto.getUpdatedAt());
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        name = proto.getName();
        lastOffset = proto.getLastUpdatedOffset();

        for (
            Map.Entry<String, ThreadSpecPb> e: proto.getThreadSpecsMap().entrySet()
        ) {
            ThreadSpec ts = ThreadSpec.fromProto(e.getValue());
            ts.wfSpec = this;
            ts.name = e.getKey();
            threadSpecs.put(e.getKey(), ts);
        }
    }

    @JsonIgnore public Class<WFSpecPb> getProtoBaseClass() {
        return WFSpecPb.class;
    }

    public static WfSpec fromProto(WFSpecPbOrBuilder proto) {
        WfSpec out = new WfSpec();
        out.initFrom(proto);
        return out;
    }

    @JsonIgnore public boolean handleDelete() {
        return true;
    }

    @JsonIgnore public void handlePost(
        POSTable<WFSpecPbOrBuilder> old, LHDatabaseClient dbClient
    ) throws LHValidationError, LHConnectionError {
        if (old != null) {
            throw new LHValidationError(null, "Mutating WfSpec not yet supported");
        }

        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHValidationError(null, "Unknown entrypoint thread");
        }

        for (Map.Entry<String, ThreadSpec> e: threadSpecs.entrySet()) {
            ThreadSpec ts = e.getValue();
            ts.validate(dbClient);
        }
    }

    public List<IndexEntry> getIndexEntries() {
        return new ArrayList<>();
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
