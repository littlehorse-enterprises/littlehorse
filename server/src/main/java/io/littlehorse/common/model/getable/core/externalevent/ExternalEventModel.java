package io.littlehorse.common.model.getable.core.externalevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class ExternalEventModel extends CoreGetable<ExternalEvent> {

    private ExternalEventIdModel id;
    private Date createdAt;
    private VariableValueModel content;
    private Integer threadRunNumber;
    private Integer nodeRunPosition;

    @Setter
    private boolean claimed;

    public ExternalEventModel() {}

    /**
     * Create ExternalEvent
     * @param content is the content of the ExternalEvent
     * @param wfRunId is the wfRunId
     * @param externalEventDefId doesn't need explaining, use your brain
     * @param guid can be null. If null, it is auto-generated
     * @param threadRunNumber can be null. If null, left as null.
     * @param nodeRunPosition can be null. If null, left as null.
     * @param createdAt is the event time
     */
    public ExternalEventModel(
            VariableValueModel content,
            ExternalEventIdModel id,
            Integer threadRunNumber,
            Integer nodeRunPosition,
            Date createdAt) {
        this.content = content;
        this.id = id;
        this.createdAt = createdAt;
        this.threadRunNumber = threadRunNumber;
        this.nodeRunPosition = nodeRunPosition;
        this.claimed = false;
    }

    public boolean hasResponse() {
        return true;
    }

    public Class<ExternalEvent> getProtoBaseClass() {
        return ExternalEvent.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEvent p = (ExternalEvent) proto;
        id = LHSerializable.fromProto(p.getId(), ExternalEventIdModel.class, context);
        if (p.hasCreatedAt()) {
            createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        } else {
            createdAt = new Date();
        }
        content = VariableValueModel.fromProto(p.getContent(), context);
        claimed = p.getClaimed();

        if (p.hasThreadRunNumber()) {
            threadRunNumber = p.getThreadRunNumber();
        }
        if (p.hasNodeRunPosition()) {
            nodeRunPosition = p.getNodeRunPosition();
        }
    }

    public ExternalEvent.Builder toProto() {
        ExternalEvent.Builder out = ExternalEvent.newBuilder()
                .setId(id.toProto())
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
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(
                                // This first one is used to optimize the processing of an
                                // ExternalEventNodeRun. It saves us from having to iterate
                                // over every single `ExternalEvent` associated with the
                                // `WfRun`; we only need to iterate over this tag
                                // with isClaimed=false and choose the first one according
                                // to timestamp.
                                Pair.of("wfRunId", GetableIndex.ValueType.SINGLE),
                                Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("isClaimed", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("isClaimed", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "extEvtDefName" -> {
                return List.of(new IndexedField(key, this.getExternalEventDefName(), tagStorageType.get()));
            }
            case "isClaimed" -> {
                return List.of(new IndexedField(key, this.isClaimed(), tagStorageType.get()));
            }
            case "wfRunId" -> {
                return List.of(new IndexedField(key, this.getId().getWfRunId().toString(), tagStorageType.get()));
            }
        }
        return List.of();
    }

    public void markClaimedBy(NodeRunModel nodeRun) {
        this.claimed = true;
        this.threadRunNumber = nodeRun.getId().getThreadRunNumber();
        this.nodeRunPosition = nodeRun.getId().getPosition();
    }

    public static ExternalEventModel fromProto(ExternalEvent p, ExecutionContext context) {
        ExternalEventModel out = new ExternalEventModel();
        out.initFrom(p, context);
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
        return id;
    }

    public String getExternalEventDefName() {
        return id.getExternalEventDefId().getName();
    }
}
