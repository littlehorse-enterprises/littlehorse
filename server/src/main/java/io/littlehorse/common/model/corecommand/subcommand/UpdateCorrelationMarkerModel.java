package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.UpdateCorrelationmarkerPb;
import io.littlehorse.common.proto.UpdateCorrelationmarkerPb.CorrelationUpdateAction;
import io.littlehorse.server.streams.storeinternals.EventCorrelationMarkerModel;
import io.littlehorse.server.streams.topology.core.CorrelationMarkerManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class UpdateCorrelationMarkerModel extends CoreSubCommand<UpdateCorrelationmarkerPb> {

    private String correlationKey;
    private ExternalEventDefIdModel externalEventDefId;
    private NodeRunIdModel waitingNodeRun;
    private CorrelationUpdateAction action;

    @Override
    public Class<UpdateCorrelationmarkerPb> getProtoBaseClass() {
        return UpdateCorrelationmarkerPb.class;
    }

    @Override
    public UpdateCorrelationmarkerPb.Builder toProto() {
        UpdateCorrelationmarkerPb.Builder out = UpdateCorrelationmarkerPb.newBuilder()
                .setCorrelationKey(correlationKey)
                .setExternalEventDefId(externalEventDefId.toProto())
                .setWaitingNodeRun(waitingNodeRun.toProto())
                .setAction(action);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        UpdateCorrelationmarkerPb p = (UpdateCorrelationmarkerPb) proto;
        this.correlationKey = p.getCorrelationKey();
        this.action = p.getAction();
        this.waitingNodeRun = LHSerializable.fromProto(p.getWaitingNodeRun(), NodeRunIdModel.class, ignored);
        this.externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);
    }

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public Empty process(ProcessorExecutionContext context, LHServerConfig config) {
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
        context.maybeCorrelateEventPedros(marker);

        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return correlationKey;
    }
}
