package io.littlehorse.common.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.ExternalEventPb;
import io.littlehorse.common.proto.ExternalEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ExternalEvent extends GETable<ExternalEventPb> {

    @JsonIgnore // We want Jackson to show  the full ID, not this.
    public String guid;

    public String wfRunId;
    public String externalEventDefName;
    public Date createdAt;
    public VariableValue content;
    public Integer threadRunNumber;
    public Integer taskRunPosition;
    public boolean claimed;

    public Class<ExternalEventPb> getProtoBaseClass() {
        return ExternalEventPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventPbOrBuilder p = (ExternalEventPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        content = VariableValue.fromProto(p.getContentOrBuilder());
        claimed = p.getClaimed();

        if (p.hasThreadRunNumber()) {
            threadRunNumber = p.getThreadRunNumber();
        }
        if (p.hasTaskRunPosition()) {
            taskRunPosition = p.getTaskRunPosition();
        }
    }

    public ExternalEventPb.Builder toProto() {
        ExternalEventPb.Builder out = ExternalEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setExternalEventDefName(externalEventDefName)
            .setGuid(guid)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setContent(content.toProto())
            .setClaimed(claimed);

        if (threadRunNumber != null) {
            out.setThreadRunNumber(threadRunNumber);
        }
        if (taskRunPosition != null) {
            out.setTaskRunPosition(taskRunPosition);
        }

        return out;
    }

    @JsonIgnore
    @Override
    public String getObjectId() {
        return getObjectId(wfRunId, externalEventDefName, guid);
    }

    public static String getObjectId(
        String wfRunId,
        String externalEventDefName,
        String guid
    ) {
        return getStorePrefix(wfRunId, externalEventDefName) + "-" + guid;
    }

    // Just for Jackson
    public String getId() {
        return getObjectId();
    }

    @JsonIgnore
    @Override
    public String getPartitionKey() {
        return wfRunId;
    }

    @JsonIgnore
    @Override
    public List<Tag> getTags() {
        // For now, there's no secondary indexing on external events.
        // We may extend the ExternalEventDef (perhaps with a schema) to allow for
        // indexing based on event content.
        List<Tag> out = Arrays.asList();

        return out;
    }

    @JsonIgnore
    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public static ExternalEvent fromProto(ExternalEventPbOrBuilder p) {
        ExternalEvent out = new ExternalEvent();
        out.initFrom(p);
        return out;
    }

    public static String getStorePrefix(String wfRunId, String externalEventDefId) {
        return wfRunId + "-" + externalEventDefId;
    }
}
