package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.OutputSchemaPb;
import io.littlehorse.common.proto.OutputSchemaPbOrBuilder;
import io.littlehorse.common.proto.VariableTypePb;

public class OutputSchema extends LHSerializable<OutputSchemaPb> {

    public VariableTypePb outputType;

    public Class<OutputSchemaPb> getProtoBaseClass() {
        return OutputSchemaPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        OutputSchemaPbOrBuilder p = (OutputSchemaPbOrBuilder) proto;
        outputType = p.getOutputType();
    }

    public OutputSchemaPb.Builder toProto() {
        OutputSchemaPb.Builder out = OutputSchemaPb
            .newBuilder()
            .setOutputType(outputType);
        return out;
    }

    public static OutputSchema fromProto(OutputSchemaPbOrBuilder proto) {
        OutputSchema out = new OutputSchema();
        out.initFrom(proto);
        return out;
    }
}
