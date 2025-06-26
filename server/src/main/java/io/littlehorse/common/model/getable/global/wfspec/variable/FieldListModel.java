package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructField.FieldList;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;

public class FieldListModel extends LHSerializable<FieldList> {

    private List<StructFieldModel> fields;

    public FieldListModel() {}

    @Override
    public FieldList.Builder toProto() {
        FieldList.Builder out = FieldList.newBuilder();

        for (StructFieldModel field : fields) {
            out.addFields(field.toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) throws LHSerdeException {
        FieldList p = (FieldList) proto;

        for (StructField field : p.getFieldsList()) {
            fields.add(StructFieldModel.fromProto(field, ctx));
        }
    }

    public static FieldListModel fromProto(FieldList proto, ExecutionContext context) {
        FieldListModel out = new FieldListModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<FieldList> getProtoBaseClass() {
        return FieldList.class;
    }
}
