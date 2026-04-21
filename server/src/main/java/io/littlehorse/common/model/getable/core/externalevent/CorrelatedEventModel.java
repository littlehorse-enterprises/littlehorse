package io.littlehorse.common.model.getable.core.externalevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class CorrelatedEventModel extends CoreGetable<CorrelatedEvent>
        implements CoreOutputTopicGetable<CorrelatedEvent> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorrelatedEventModel.class);
    private CorrelatedEventIdModel id;
    private VariableValueModel content;
    private Date createdAt;
    private List<ExternalEventIdModel> externalEvents = new ArrayList<>();

    @Override
    public Class<CorrelatedEvent> getProtoBaseClass() {
        return CorrelatedEvent.class;
    }

    @Override
    public CorrelatedEvent.Builder toProto() {
        CorrelatedEvent.Builder out = CorrelatedEvent.newBuilder()
                .setCreatedAt(LHLibUtil.fromDate(createdAt))
                .setContent(content.toProto())
                .setId(id.toProto());
        for (ExternalEventIdModel extEvtId : externalEvents) {
            out.addExternalEvents(extEvtId.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        CorrelatedEvent p = (CorrelatedEvent) proto;
        this.id = LHSerializable.fromProto(p.getId(), CorrelatedEventIdModel.class, ignored);
        this.content = LHSerializable.fromProto(p.getContent(), VariableValueModel.class, ignored);
        this.createdAt = LHLibUtil.fromProtoTs(p.getCreatedAt());
        this.externalEvents = new ArrayList<>();
        for (ExternalEventId extEvtId : p.getExternalEventsList()) {
            this.externalEvents.add(LHSerializable.fromProto(extEvtId, ExternalEventIdModel.class, ignored));
        }
    }

    @Override
    public CorrelatedEventIdModel getObjectId() {
        return id;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        // TODO (#1582): add indexes
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("hasExtEvts", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)));
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "extEvtDefName" -> {
                return List.of(new IndexedField(
                        key, this.getId().getExternalEventDefId().getName(), TagStorageType.LOCAL));
            }
            case "hasExtEvts" -> {
                return List.of(new IndexedField(key, String.valueOf(!externalEvents.isEmpty()), TagStorageType.LOCAL));
            }
        }
        log.warn("Received unknown key for CorrelatedEvent Index: {}", key);
        return null;
    }

    public CorrelatedEventIdModel getId() {
        return this.id;
    }

    public VariableValueModel getContent() {
        return this.content;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public List<ExternalEventIdModel> getExternalEvents() {
        return this.externalEvents;
    }

    public void setId(final CorrelatedEventIdModel id) {
        this.id = id;
    }

    public void setContent(final VariableValueModel content) {
        this.content = content;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setExternalEvents(final List<ExternalEventIdModel> externalEvents) {
        this.externalEvents = externalEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CorrelatedEventModel)) return false;
        final CorrelatedEventModel other = (CorrelatedEventModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$createdAt = this.getCreatedAt();
        final Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final Object this$externalEvents = this.getExternalEvents();
        final Object other$externalEvents = other.getExternalEvents();
        if (this$externalEvents == null
                ? other$externalEvents != null
                : !this$externalEvents.equals(other$externalEvents)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CorrelatedEventModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final Object $externalEvents = this.getExternalEvents();
        result = result * PRIME + ($externalEvents == null ? 43 : $externalEvents.hashCode());
        return result;
    }
}
