package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.UpdateCorrelationMarkerPb;
import io.littlehorse.common.proto.UpdateCorrelationMarkerPb.CorrelationUpdateAction;
import io.littlehorse.server.streams.storeinternals.EventCorrelationMarkerModel;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.CorrelationMarkerManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UpdateCorrelationMarkerModel extends CoreSubCommand<UpdateCorrelationMarkerPb> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateCorrelationMarkerModel.class);
    private String correlationKey;
    private ExternalEventDefIdModel externalEventDefId;
    private NodeRunIdModel waitingNodeRun;
    private CorrelationUpdateAction action;

    @Override
    public Class<UpdateCorrelationMarkerPb> getProtoBaseClass() {
        return UpdateCorrelationMarkerPb.class;
    }

    @Override
    public UpdateCorrelationMarkerPb.Builder toProto() {
        UpdateCorrelationMarkerPb.Builder out = UpdateCorrelationMarkerPb.newBuilder()
                .setCorrelationKey(correlationKey)
                .setExternalEventDefId(externalEventDefId.toProto())
                .setWaitingNodeRun(waitingNodeRun.toProto())
                .setAction(action);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        UpdateCorrelationMarkerPb p = (UpdateCorrelationMarkerPb) proto;
        this.correlationKey = p.getCorrelationKey();
        this.action = p.getAction();
        this.waitingNodeRun = LHSerializable.fromProto(p.getWaitingNodeRun(), NodeRunIdModel.class, ignored);
        this.externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);
    }

    @Override
    public Empty process(CoreProcessorContext context, LHServerConfig config) {
        CorrelationMarkerManager manager = context.getCorrelationMarkerManager();
        EventCorrelationMarkerModel marker = manager.getOrCreateMarker(correlationKey, externalEventDefId);

        switch (action) {
            case CORRELATE:
                marker.addCorrelation(waitingNodeRun);
                break;
            case UNCORRELATE:
                marker.removeCorrelation(waitingNodeRun);
                break;
            default:
                throw new IllegalStateException("Unrecognized Correlation Marker Action");
        }
        manager.saveCorrelationMarker(marker);
        log.trace("Saved correleation marker {}", marker);
        context.maybeCorrelateEventToWfRuns(marker);

        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return correlationKey;
    }

    public String getCorrelationKey() {
        return this.correlationKey;
    }

    public ExternalEventDefIdModel getExternalEventDefId() {
        return this.externalEventDefId;
    }

    public NodeRunIdModel getWaitingNodeRun() {
        return this.waitingNodeRun;
    }

    public CorrelationUpdateAction getAction() {
        return this.action;
    }

    public void setCorrelationKey(final String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public void setExternalEventDefId(final ExternalEventDefIdModel externalEventDefId) {
        this.externalEventDefId = externalEventDefId;
    }

    public void setWaitingNodeRun(final NodeRunIdModel waitingNodeRun) {
        this.waitingNodeRun = waitingNodeRun;
    }

    public void setAction(final CorrelationUpdateAction action) {
        this.action = action;
    }
}
