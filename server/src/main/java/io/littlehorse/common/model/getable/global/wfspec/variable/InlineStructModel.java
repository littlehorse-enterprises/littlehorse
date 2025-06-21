package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;

public class InlineStructModel extends LHSerializable<InlineStruct> {

    @Getter
    private Map<String, StructFieldModel> fields;

    @Override
    public InlineStruct.Builder toProto() {
        InlineStruct.Builder out = InlineStruct.newBuilder();

        for (Entry<String, StructFieldModel> field : fields.entrySet()) {
            out.putFields(field.getKey(), field.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) throws LHSerdeException {
        InlineStruct inlineStruct = (InlineStruct) proto;

        fields = new HashMap<>();

        for (Entry<String, StructField> field : inlineStruct.getFieldsMap().entrySet()) {
            fields.put(field.getKey(), StructFieldModel.fromProto(field.getValue(), StructFieldModel.class, ctx));
        }
    }

    public static InlineStructModel fromProto(InlineStruct proto, ExecutionContext context) {
        InlineStructModel out = new InlineStructModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<InlineStruct> getProtoBaseClass() {
        return InlineStruct.class;
    }
}
