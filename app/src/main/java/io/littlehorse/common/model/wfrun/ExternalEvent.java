package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.ExternalEventPb;
import io.littlehorse.common.proto.ExternalEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class ExternalEvent extends GETable<ExternalEventPb> {

    @JsonIgnore // We want Jackson to show  the full ID, not this.
    public String guid;

    public String wfRunId;
    public String externalEventDefName;
    private Date createdAt;
    public VariableValue content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;
    public boolean claimed;

    public boolean hasResponse() {
        return true;
    }

    public Class<ExternalEventPb> getProtoBaseClass() {
        return ExternalEventPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventPbOrBuilder p = (ExternalEventPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
        if (p.hasCreatedAt()) {
            createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        } else {
            createdAt = new Date();
        }
        content = VariableValue.fromProto(p.getContentOrBuilder());
        claimed = p.getClaimed();

        if (p.hasThreadRunNumber()) {
            threadRunNumber = p.getThreadRunNumber();
        }
        if (p.hasNodeRunPosition()) {
            nodeRunPosition = p.getNodeRunPosition();
        }
    }

    public ExternalEventPb.Builder toProto() {
        ExternalEventPb.Builder out = ExternalEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setExternalEventDefName(externalEventDefName)
            .setGuid(guid)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
            .setContent(content.toProto())
            .setClaimed(claimed);

        if (threadRunNumber != null) {
            out.setThreadRunNumber(threadRunNumber);
        }
        if (nodeRunPosition != null) {
            out.setNodeRunPosition(nodeRunPosition);
        }

        return out;
    }

    @JsonIgnore
    @Override
    public String getObjectId() {
        return getStoreKey(wfRunId, externalEventDefName, guid);
    }

    public static String getStoreKey(
        String wfRunId,
        String externalEventDefName,
        String guid
    ) {
        return getStorePrefix(wfRunId, externalEventDefName) + "/" + guid;
    }

    // Just for Jackson
    @JsonProperty("objectId")
    public String getIdForJackson() {
        return getObjectId();
    }

    @JsonIgnore
    @Override
    public String getPartitionKey() {
        return wfRunId;
    }

    @JsonIgnore
    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    public static ExternalEvent fromProto(ExternalEventPbOrBuilder p) {
        ExternalEvent out = new ExternalEvent();
        out.initFrom(p);
        return out;
    }

    public static String getStorePrefix(String wfRunId, String externalEventDefId) {
        return wfRunId + "/" + externalEventDefId;
    }

    public Integer getThreadRunNumber() {
        return threadRunNumber;
    }

    public Integer getNodeRunPosition() {
        return nodeRunPosition;
    }
}
