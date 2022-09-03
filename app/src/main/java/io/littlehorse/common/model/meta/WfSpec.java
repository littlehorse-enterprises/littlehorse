package io.littlehorse.common.model.meta;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.RunStartOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.wfspec.ThreadSpecPb;
import io.littlehorse.common.proto.wfspec.WfSpecPb;
import io.littlehorse.common.proto.wfspec.WfSpecPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntry;
import io.littlehorse.server.model.scheduler.LHTimer;
import io.littlehorse.server.model.scheduler.WfRunState;


public class WfSpec extends POSTable<WfSpecPbOrBuilder> {
    public String id;
    public String name;
    public Date createdAt;
    public Date updatedAt;
    public long lastOffset;


    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    public String getObjectId() {
        if (id.equals("")) {
            id = LHUtil.generateGuid();
        }
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getPartitionKey() {
        return getObjectId();
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

    public WfSpecPb.Builder toProto() {
        WfSpecPb.Builder out = WfSpecPb.newBuilder()
            .setId(id)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setUpdatedAt(LHUtil.fromDate(updatedAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status)
            .setName(name)
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

    public void initFrom(MessageOrBuilder pr) throws LHSerdeError {
        WfSpecPbOrBuilder proto = (WfSpecPbOrBuilder) pr;
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
            ThreadSpec ts = new ThreadSpec();
            ts.wfSpec = this;
            ts.name = e.getKey();
            ts.initFrom(e.getValue());
            threadSpecs.put(e.getKey(), ts);
        }
    }

    @JsonIgnore public Class<WfSpecPb> getProtoBaseClass() {
        return WfSpecPb.class;
    }

    public static WfSpec fromProto(WfSpecPbOrBuilder proto) throws LHSerdeError {
        WfSpec out = new WfSpec();
        out.initFrom(proto);
        return out;
    }

    @JsonIgnore public boolean handleDelete() {
        return true;
    }

    @JsonIgnore public void handlePost(
        POSTable<WfSpecPbOrBuilder> old, LHDatabaseClient dbClient, LHConfig config
    ) throws LHValidationError, LHConnectionError {
        if (old != null) {
            throw new LHValidationError(null, "Mutating WfSpec not yet supported");
        }

        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHValidationError(null, "Unknown entrypoint thread");
        }

        for (Map.Entry<String, ThreadSpec> e: threadSpecs.entrySet()) {
            ThreadSpec ts = e.getValue();
            ts.validate(dbClient, config);
        }
    }

    public List<IndexEntry> getIndexEntries() {
        List<IndexEntry> out = Arrays.asList(
            new IndexEntry(this, Pair.of("name", name))
        );

        return out;
    }

    public WfRunState startNewRun(
        WfRunEvent e,
        List<TaskScheduleRequest> tasksToSchedule,
        List<LHTimer> timersToSchedule
    ) {
        WfRunState out = new WfRunState();
        out.id = e.runRequest.wfRunId;
        out.oEvents.wfRunId = out.id;

        out.wfSpec = this;
        out.tasksToSchedule = tasksToSchedule;
        out.timersToSchedule = timersToSchedule;
        out.wfSpecId = id;
        out.wfSpecName = name;
        out.startTime = e.time;
        out.status = LHStatusPb.RUNNING;
        out.oEvents.add(new ObservabilityEvent(
            new RunStartOe(id, name), e.time)
        );

        out.startThread(entrypointThreadName, e.time, null);

        return out;
    }
}
