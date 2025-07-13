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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class CorrelatedEventModel extends CoreGetable<CorrelatedEvent>
        implements CoreOutputTopicGetable<CorrelatedEvent> {

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
}
