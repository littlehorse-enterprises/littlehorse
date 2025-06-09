package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.EventCorrelationMarker;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class EventCorrelationMarkerModel extends Storeable<EventCorrelationMarker> {

    private Set<NodeRunIdModel> sourceNodeRuns = new HashSet<>();
    private ExternalEventDefIdModel eventDefId;
    private String correlationKey;

    public EventCorrelationMarkerModel() {}

    public EventCorrelationMarkerModel(String key, ExternalEventDefIdModel externalEventDefId) {
        this.correlationKey = key;
        this.eventDefId = externalEventDefId;
    }

    @Override
    public Class<EventCorrelationMarker> getProtoBaseClass() {
        return EventCorrelationMarker.class;
    }

    @Override
    public EventCorrelationMarker.Builder toProto() {
        EventCorrelationMarker.Builder out = EventCorrelationMarker.newBuilder()
                .setEventDefId(eventDefId.toProto())
                .setCorrelationKey(correlationKey);

        for (NodeRunIdModel nodeRun : sourceNodeRuns) {
            out.addSourceNodeRuns(nodeRun.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        EventCorrelationMarker p = (EventCorrelationMarker) proto;
        this.correlationKey = p.getCorrelationKey();
        this.eventDefId = LHSerializable.fromProto(p.getEventDefId(), ExternalEventDefIdModel.class, context);
        
        for (NodeRunId nrid : p.getSourceNodeRunsList()) {
            this.sourceNodeRuns.add(LHSerializable.fromProto(nrid, NodeRunIdModel.class, context));
        }
    }

    @Override
    public StoreableType getType() {
        return StoreableType.CORRELATION_MARKER;
    }

    @Override
    public String getStoreKey() {
        return getStoreKey(correlationKey, eventDefId);
    }

    public static String getStoreKey(String correlationKey, ExternalEventDefIdModel eventId) {
        return LHUtil.getCompositeId(correlationKey, eventId.toString());
    }

    public void addCorrelation(NodeRunIdModel nodeRunId) {
        sourceNodeRuns.add(nodeRunId);
    }

    public void removeCorrelation(NodeRunIdModel nodeRunId) {
        sourceNodeRuns.remove(nodeRunId);
    }

    public List<CoreSubCommand<?>> 

    public boolean isEmpty() {
        return sourceNodeRuns.isEmpty();
    }
}
