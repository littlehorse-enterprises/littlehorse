package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.OutputSchemaPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;

public class OutputSchema extends LHSerializable<OutputSchemaPb> {

    public VariableTypePb outputType;

    public Class<OutputSchemaPb> getProtoBaseClass() {
        return OutputSchemaPb.class;
    }

    public void initFrom(Message proto) {
        OutputSchemaPb p = (OutputSchemaPb) proto;
        outputType = p.getOutputType();
    }

    public OutputSchemaPb.Builder toProto() {
        OutputSchemaPb.Builder out = OutputSchemaPb
            .newBuilder()
            .setOutputType(outputType);
        return out;
    }

    public static OutputSchema fromProto(OutputSchemaPb proto) {
        OutputSchema out = new OutputSchema();
        out.initFrom(proto);
        return out;
    }
}
