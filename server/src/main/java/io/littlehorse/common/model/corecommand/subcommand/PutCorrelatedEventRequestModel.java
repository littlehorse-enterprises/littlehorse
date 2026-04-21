package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
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
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class PutCorrelatedEventRequestModel extends CoreSubCommand<PutCorrelatedEventRequest> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PutCorrelatedEventRequestModel.class);
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
    public CorrelatedEvent process(CoreProcessorContext context, LHServerConfig config) {
        // Validate the name. Only `/` is prohibited.
        if (key.contains("/") || key.contains("~")) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "CorrelatedEvent keys cannot contain \'/\' or \'~\'");
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
            try {
                type.validateCompatibility(content, context.metadataManager());
            } catch (TypeValidationException e) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Invalid type of content for event: " + externalEventDef.getName() + ": " + e.getMessage());
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
        manager.put(correlatedEvent);
        context.maybeCorrelateEventToWfRuns(correlatedEvent);
        if (externalEventDef.getCorrelatedEventConfig().getTtlSeconds() != null) {
            DeleteCorrelatedEventRequestModel deleteRequest = new DeleteCorrelatedEventRequestModel();
            deleteRequest.setId(id);
            CommandModel command = new CommandModel(deleteRequest);
            command.setTime(new Date(System.currentTimeMillis()
                    + (1000 * externalEventDef.getCorrelatedEventConfig().getTtlSeconds())));
            context.getTaskManager().scheduleTimer(new LHTimer(command));
        }
        return correlatedEvent.toProto().build();
    }

    public PutCorrelatedEventRequestModel() {}

    public String getKey() {
        return this.key;
    }

    public ExternalEventDefIdModel getExternalEventDefId() {
        return this.externalEventDefId;
    }

    public VariableValueModel getContent() {
        return this.content;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setExternalEventDefId(final ExternalEventDefIdModel externalEventDefId) {
        this.externalEventDefId = externalEventDefId;
    }

    public void setContent(final VariableValueModel content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PutCorrelatedEventRequestModel(key=" + this.getKey() + ", externalEventDefId="
                + this.getExternalEventDefId() + ", content=" + this.getContent() + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PutCorrelatedEventRequestModel)) return false;
        final PutCorrelatedEventRequestModel other = (PutCorrelatedEventRequestModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
        final Object this$externalEventDefId = this.getExternalEventDefId();
        final Object other$externalEventDefId = other.getExternalEventDefId();
        if (this$externalEventDefId == null
                ? other$externalEventDefId != null
                : !this$externalEventDefId.equals(other$externalEventDefId)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PutCorrelatedEventRequestModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $externalEventDefId = this.getExternalEventDefId();
        result = result * PRIME + ($externalEventDefId == null ? 43 : $externalEventDefId.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        return result;
    }
}
