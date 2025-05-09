package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.text.MessageFormat;

public class StructFieldDefModel extends LHSerializable<StructFieldDef> {

    private String name;
    private boolean isOptional;
    private TypeDefinitionModel fieldType;

    @Override
    public StructFieldDef.Builder toProto() {
        StructFieldDef.Builder out = StructFieldDef.newBuilder()
                .setName(this.name)
                .setOptional(this.isOptional)
                .setFieldType(this.fieldType.toProto());

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructFieldDef proto = (StructFieldDef) p;
        name = proto.getName();
        isOptional = proto.getOptional();
        fieldType = LHSerializable.fromProto(proto.getFieldType(), TypeDefinitionModel.class, context);
    }

    @Override
    public Class<StructFieldDef> getProtoBaseClass() {
        return StructFieldDef.class;
    }

    public void validate() throws LHApiException {
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    MessageFormat.format("StructField name [{0}] must be a valid hostname", name));
        }
    }
}
