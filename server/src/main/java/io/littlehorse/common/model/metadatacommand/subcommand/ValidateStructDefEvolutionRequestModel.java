package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.util.InlineStructDefUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Set;

public class ValidateStructDefEvolutionRequestModel extends LHSerializable<ValidateStructDefEvolutionRequest> {

    private StructDefIdModel structDefId;
    private StructDefCompatibilityType compatibilityType;
    private InlineStructDefModel structDef;

    @Override
    public ValidateStructDefEvolutionRequest.Builder toProto() {
        ValidateStructDefEvolutionRequest.Builder proto = ValidateStructDefEvolutionRequest.newBuilder();

        proto.setStructDefId(structDefId.toProto());
        proto.setCompatibilityType(compatibilityType);
        proto.setStructDef(structDef.toProto());

        return proto;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ValidateStructDefEvolutionRequest p = (ValidateStructDefEvolutionRequest) proto;

        structDefId = StructDefIdModel.fromProto(p.getStructDefId(), context);
        compatibilityType = p.getCompatibilityType();
        structDef = InlineStructDefModel.fromProto(p.getStructDef(), context);
    }

    public ValidateStructDefEvolutionResponse process(RequestExecutionContext executionContext) {
        structDef.validate();

        StructDefModel existingStructDef = executionContext.service().getStructDef(structDefId.getName(), null);

        if (existingStructDef == null) {
            return ValidateStructDefEvolutionResponse.newBuilder()
                    .setIsValid(true)
                    .build();
        } else {
            InlineStructDefModel oldStructDef = existingStructDef.getStructDef();

            Set<String> invalidFields =
                    InlineStructDefUtil.getIncompatibleFields(compatibilityType, structDef, oldStructDef);

            System.out.println(invalidFields);

            return ValidateStructDefEvolutionResponse.newBuilder()
                    .setIsValid(invalidFields.isEmpty())
                    .build();
        }
    }

    @Override
    public Class<ValidateStructDefEvolutionRequest> getProtoBaseClass() {
        return ValidateStructDefEvolutionRequest.class;
    }
}
