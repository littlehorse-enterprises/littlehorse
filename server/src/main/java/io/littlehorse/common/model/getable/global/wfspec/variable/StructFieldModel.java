package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructFieldModel extends LHSerializable<StructField> {

    private VariableValueModel primitive;
    private InlineStructModel struct;
    private FieldListModel list;

    @Override
    public StructField.Builder toProto() {
        StructField.Builder out = StructField.newBuilder();

        if (primitive != null) {
            out.setPrimitive(primitive.toProto());
        } else if (struct != null) {
            out.setStruct(struct.toProto());
        } else if (list != null) {
            out.setList(list.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) throws LHSerdeException {
        StructField structField = (StructField) proto;

        switch (structField.getStructValueCase()) {
            case PRIMITIVE:
                primitive = VariableValueModel.fromProto(structField.getPrimitive(), ctx);
                break;
            case LIST:
                list = FieldListModel.fromProto(structField.getList(), ctx);
                break;
            case STRUCT:
                struct = InlineStructModel.fromProto(structField.getStruct(), ctx);
                break;
            case STRUCTVALUE_NOT_SET:
            default:
                // TODO: Throw error here?
                break;
        }
    }

    public static StructFieldModel fromProto(StructField proto, ExecutionContext context) {
        StructFieldModel out = new StructFieldModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<StructField> getProtoBaseClass() {
        return StructField.class;
    }
}
