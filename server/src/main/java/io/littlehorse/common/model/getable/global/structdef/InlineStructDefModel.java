package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;

public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

    @Getter
    private Map<String, StructFieldDefModel> fields = new HashMap<>();

    @Override
    public InlineStructDef.Builder toProto() {
        InlineStructDef.Builder out = InlineStructDef.newBuilder();

        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            out.putFields(field.getKey(), field.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        InlineStructDef proto = (InlineStructDef) p;

        for (Entry<String, StructFieldDef> structFieldDef : proto.getFieldsMap().entrySet()) {
            fields.put(
                    structFieldDef.getKey(),
                    LHSerializable.fromProto(structFieldDef.getValue(), StructFieldDefModel.class, context));
        }
    }

    public static InlineStructDefModel fromProto(InlineStructDef proto, ExecutionContext context) {
        InlineStructDefModel out = new InlineStructDefModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<InlineStructDef> getProtoBaseClass() {
        return InlineStructDef.class;
    }

    public void validate() {
        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            // TODO: Propose and agree upon Field Name validation technique!

            field.getValue().validate();
        }
    }

    public Map<String, StructFieldDefModel> getRequiredFields() {
        return fields.entrySet().stream()
                .filter(entry -> entry.getValue().isRequired())
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }
}
