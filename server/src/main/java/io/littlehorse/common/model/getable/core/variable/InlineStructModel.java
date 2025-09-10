package io.littlehorse.common.model.getable.core.variable;

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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InlineStruct p = (InlineStruct) proto;

        fields = new HashMap<>();

        for (Entry<String, StructField> entry : p.getFieldsMap().entrySet()) {
            fields.put(entry.getKey(), StructFieldModel.fromProto(entry.getValue(), StructFieldModel.class, context));
        }
    }

    @Override
    public InlineStruct.Builder toProto() {
        InlineStruct.Builder out = InlineStruct.newBuilder();

        for (Entry<String, StructFieldModel> entry : fields.entrySet()) {
            out.putFields(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public Class<InlineStruct> getProtoBaseClass() {
        return InlineStruct.class;
    }
}
