package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.OutputSchema;
import io.littlehorse.sdk.common.proto.VariableType;

public class OutputSchemaModel extends LHSerializable<OutputSchema> {

    public VariableType outputType;

    public Class<OutputSchema> getProtoBaseClass() {
        return OutputSchema.class;
    }

    public void initFrom(Message proto) {
        OutputSchema p = (OutputSchema) proto;
        outputType = p.getOutputType();
    }

    public OutputSchema.Builder toProto() {
        OutputSchema.Builder out = OutputSchema
            .newBuilder()
            .setOutputType(outputType);
        return out;
    }

    public static OutputSchemaModel fromProto(OutputSchema proto) {
        OutputSchemaModel out = new OutputSchemaModel();
        out.initFrom(proto);
        return out;
    }
}
