package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class ExternalEventModel extends Getable<ExternalEvent> {

    // We want Jackson to show  the full ID, not this.
    public String guid;

    public String wfRunId;
    public String externalEventDefName;
    private Date createdAt;
    public VariableValueModel content;
    public Integer threadRunNumber;
    public Integer nodeRunPosition;
    public boolean claimed;

    public boolean hasResponse() {
        return true;
    }

    public Class<ExternalEvent> getProtoBaseClass() {
        return ExternalEvent.class;
    }

    public void initFrom(Message proto) {
        ExternalEvent p = (ExternalEvent) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
        if (p.hasCreatedAt()) {
            createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        } else {
            createdAt = new Date();
        }
        content = VariableValueModel.fromProto(p.getContent());
        claimed = p.getClaimed();

        if (p.hasThreadRunNumber()) {
            threadRunNumber = p.getThreadRunNumber();
        }
        if (p.hasNodeRunPosition()) {
            nodeRunPosition = p.getNodeRunPosition();
        }
    }

    public ExternalEvent.Builder toProto() {
        ExternalEvent.Builder out = ExternalEvent
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
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of(
            new GetableIndex<>(
                List.of(
                    Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("isClaimed", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageType.LOCAL)
            ),
            new GetableIndex<>(
                List.of(Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageType.LOCAL)
            )
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageType> tagStorageTypePb
    ) {
        switch (key) {
            case "extEvtDefName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getExternalEventDefName(),
                        tagStorageTypePb.get()
                    )
                );
            }
            case "isClaimed" -> {
                return List.of(
                    new IndexedField(key, this.isClaimed(), tagStorageTypePb.get())
                );
            }
        }
        return List.of();
    }

    public static ExternalEventModel fromProto(ExternalEvent p) {
        ExternalEventModel out = new ExternalEventModel();
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

    public ExternalEventIdModel getObjectId() {
        return new ExternalEventIdModel(wfRunId, externalEventDefName, guid);
    }
}
