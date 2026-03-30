package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

public class ArrayModel extends LHSerializable<Array> {

    @Getter
    private ArrayList<VariableValueModel> items;

    @Getter
    @Setter
    private TypeDefinitionModel elementType;

    public ArrayModel() {}

    @Override
    public Array.Builder toProto() {
        Array.Builder out = Array.newBuilder();
        for (VariableValueModel item : items) {
            out.addItems(item.toProto());
        }
        if (elementType != null) {
            out.setElementType(elementType.toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Array p = (Array) proto;
        items = new ArrayList<>();
        for (VariableValue item : p.getItemsList()) {
            items.add(VariableValueModel.fromProto(item, VariableValueModel.class, context));
        }
        if (p.hasElementType()) {
            this.elementType = TypeDefinitionModel.fromProto(p.getElementType(), TypeDefinitionModel.class, context);
        }
    }

    @Override
    public Class<Array> getProtoBaseClass() {
        return Array.class;
    }
}
