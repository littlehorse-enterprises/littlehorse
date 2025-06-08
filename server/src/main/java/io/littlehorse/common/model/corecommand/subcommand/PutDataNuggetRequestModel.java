package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.externalevent.DataNuggetModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.DataNuggetIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.DataNugget;
import io.littlehorse.sdk.common.proto.PutDataNuggetRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PutDataNuggetRequestModel extends CoreSubCommand<PutDataNuggetRequest> {

    private String key;
    private ExternalEventDefIdModel externalEventDefId;
    private String guid;
    private VariableValueModel content;
    private Integer expectedEpoch;

    @Override
    public Class<PutDataNuggetRequest> getProtoBaseClass() {
        return PutDataNuggetRequest.class;
    }

    @Override
    public PutDataNuggetRequest.Builder toProto() {
        PutDataNuggetRequest.Builder builder = PutDataNuggetRequest.newBuilder()
                .setKey(key)
                .setExternalEventDefId(externalEventDefId.toProto())
                .setContent(content.toProto());

        if (guid != null) {
            builder.setGuid(guid);
        }

        if (expectedEpoch != null) {
            builder.setExpectedEpoch(expectedEpoch);
        }

        return builder;
    }

    @Override
    public void initFrom(Message p, ExecutionContext ignored) {
        PutDataNuggetRequest proto = (PutDataNuggetRequest) p;
        this.key = proto.getKey();
        this.externalEventDefId =
                LHSerializable.fromProto(proto.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);

        this.guid = proto.hasGuid() ? proto.getGuid() : null;
        this.content = LHSerializable.fromProto(proto.getContent(), VariableValueModel.class, ignored);
        this.expectedEpoch = proto.hasExpectedEpoch() ? proto.getExpectedEpoch() : null;
    }

    @Override
    public String getPartitionKey() {
        return key;
    }

    @Override
    public DataNugget process(ProcessorExecutionContext context, LHServerConfig config) {
        // Validate the name. Only `/` is prohibited.
        if (key.contains("/")) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "DataNugget keys cannot contain '/'");
        }

        ExternalEventDefModel externalEventDef = context.metadataManager().get(externalEventDefId);
        if (externalEventDef == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Could not find specified ExternalEventDef");
        }
        if (externalEventDef.getDataNuggetConfig() == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Specified ExternalEventDef does not have DataNugget enabled");
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
        if (guid == null) guid = LHUtil.generateGuid();

        DataNuggetIdModel id = new DataNuggetIdModel(key, externalEventDefId, guid);
        DataNuggetModel dataNugget = manager.get(id);
        if (dataNugget != null) {
            if (dataNugget.getEpoch() != expectedEpoch) {
                throw new LHApiException(
                        Status.FAILED_PRECONDITION,
                        "Mismatched epoch. Expected " + expectedEpoch + " and observed " + dataNugget.getEpoch());
            }

            dataNugget.setEpoch(expectedEpoch + 1);
            dataNugget.setContent(content);
        } else {
            dataNugget = new DataNuggetModel();
            dataNugget.setId(id);
            dataNugget.setCreatedAt(context.currentCommand().getTime());
            dataNugget.setContent(content);
            dataNugget.setEpoch(0);
        }

        // TODO (#1583): Check for CorrelationMarkers and send ExternalEvent's through Timer/Boomerang topology
        manager.put(dataNugget);

        return dataNugget.toProto().build();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
