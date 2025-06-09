package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PutCorrelatedEventRequestModel extends CoreSubCommand<PutCorrelatedEventRequest> {

    private String key;
    private ExternalEventDefIdModel externalEventDefId;
    private VariableValueModel content;

    @Override
    public Class<PutCorrelatedEventRequest> getProtoBaseClass() {
        return PutCorrelatedEventRequest.class;
    }

    @Override
    public PutCorrelatedEventRequest.Builder toProto() {
        PutCorrelatedEventRequest.Builder builder = PutCorrelatedEventRequest.newBuilder()
                .setKey(key)
                .setExternalEventDefId(externalEventDefId.toProto())
                .setContent(content.toProto());

        return builder;
    }

    @Override
    public void initFrom(Message p, ExecutionContext ignored) {
        PutCorrelatedEventRequest proto = (PutCorrelatedEventRequest) p;
        this.key = proto.getKey();
        this.externalEventDefId =
                LHSerializable.fromProto(proto.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);

        this.content = LHSerializable.fromProto(proto.getContent(), VariableValueModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return key;
    }

    @Override
    public CorrelatedEvent process(ProcessorExecutionContext context, LHServerConfig config) {
        // Validate the name. Only `/` is prohibited.
        if (key.contains("/")) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "CorrelatedEvent keys cannot contain '/'");
        }

        ExternalEventDefModel externalEventDef = context.metadataManager().get(externalEventDefId);
        if (externalEventDef == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Could not find specified ExternalEventDef");
        }
        if (externalEventDef.getCorrelatedEventConfig() == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Specified ExternalEventDef does not have CorrelatedEvent enabled");
        }
        if (externalEventDef.getReturnType().isPresent()) {
            ReturnTypeModel type = externalEventDef.getReturnType().get();
            if (!type.isCompatibleWith(content)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Invalid type of content for event. Check the return type of ExternalEventDef "
                                + externalEventDef.getName());
            }
        }

        GetableManager manager = context.getableManager();

        CorrelatedEventIdModel id = new CorrelatedEventIdModel(key, externalEventDefId);
        CorrelatedEventModel oldEvent = manager.get(id);
        if (oldEvent != null) {
            throw new LHApiException(Status.ALREADY_EXISTS, "Correlated Event with key " + key + " already exists!");
        }
        CorrelatedEventModel correlatedEvent = new CorrelatedEventModel();
        correlatedEvent.setId(id);
        correlatedEvent.setCreatedAt(context.currentCommand().getTime());
        correlatedEvent.setContent(content);

        // TODO (#1583): Check for CorrelationMarkers and send ExternalEvent's through Timer/Boomerang topology
        manager.put(correlatedEvent);
        correlatedEvent.maybeWakeUpNodeRuns(manager.);

        return correlatedEvent.toProto().build();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
