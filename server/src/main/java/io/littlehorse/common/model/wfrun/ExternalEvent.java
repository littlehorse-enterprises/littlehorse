package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class ExternalEvent extends Getable<ExternalEventPb> {

    // We want Jackson to show  the full ID, not this.
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

    public void initFrom(Message proto) {
        ExternalEventPb p = (ExternalEventPb) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
        if (p.hasCreatedAt()) {
            createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        } else {
            createdAt = new Date();
        }
        content = VariableValue.fromProto(p.getContent());
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

    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    @Override
    public List<GetableIndex> getIndexes() {
        return List.of(
            new GetableIndex(
                ExternalEvent.class,
                List.of(
                    Pair.of(
                        "extEvtDefName",
                        getable ->
                            List.of(
                                ((ExternalEvent) getable).getExternalEventDefName()
                            )
                    ),
                    Pair.of(
                        "isClaimed",
                        getable ->
                            List.of(
                                String.valueOf(((ExternalEvent) getable).isClaimed())
                            )
                    )
                ),
                wfRunPb -> true,
                TagStorageTypePb.LOCAL
            ),
            new GetableIndex(
                ExternalEvent.class,
                List.of(
                    Pair.of(
                        "extEvtDefName",
                        getable ->
                            List.of(
                                ((ExternalEvent) getable).getExternalEventDefName()
                            )
                    )
                ),
                wfRunPb -> true,
                TagStorageTypePb.LOCAL
            )
        );
    }

    public static ExternalEvent fromProto(ExternalEventPb p) {
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

    public ExternalEventId getObjectId() {
        return new ExternalEventId(wfRunId, externalEventDefName, guid);
    }
}
