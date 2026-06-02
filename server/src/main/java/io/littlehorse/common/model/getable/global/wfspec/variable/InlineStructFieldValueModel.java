package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue.StructValueCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class InlineStructFieldValueModel extends LHSerializable<InlineStructFieldValue> {

    private StructValueCase structValueCase;
    private VariableAssignmentModel simpleValue;
    private InlineStructBuilderModel subStructure;

    @Override
    public Class<InlineStructFieldValue> getProtoBaseClass() {
        return InlineStructFieldValue.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InlineStructFieldValue p = (InlineStructFieldValue) proto;
        structValueCase = p.getStructValueCase();
        switch (structValueCase) {
            case SIMPLE_VALUE:
                simpleValue = VariableAssignmentModel.fromProto(p.getSimpleValue(), context);
                break;
            case SUB_STRUCTURE:
                subStructure = InlineStructBuilderModel.fromProto(p.getSubStructure(), context);
                break;
            case STRUCTVALUE_NOT_SET:
                break;
        }
    }

    @Override
    public InlineStructFieldValue.Builder toProto() {
        InlineStructFieldValue.Builder out = InlineStructFieldValue.newBuilder();
        switch (structValueCase) {
            case SIMPLE_VALUE:
                out.setSimpleValue(simpleValue.toProto());
                break;
            case SUB_STRUCTURE:
                out.setSubStructure(subStructure.toProto());
                break;
            case STRUCTVALUE_NOT_SET:
                break;
        }
        return out;
    }

    public static InlineStructFieldValueModel fromProto(InlineStructFieldValue proto, ExecutionContext context) {
        InlineStructFieldValueModel out = new InlineStructFieldValueModel();
        out.initFrom(proto, context);
        return out;
    }
}
