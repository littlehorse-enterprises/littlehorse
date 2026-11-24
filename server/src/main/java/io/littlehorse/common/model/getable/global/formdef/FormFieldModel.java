package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.LHPathModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormField;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class FormFieldModel extends LHSerializable<FormField> {

    private LHPathModel fieldPath;
    private Map<String, VariableValueModel> metadata = new HashMap<>();

    public FormFieldModel() {}

    public FormFieldModel(LHPathModel fieldPath, Map<String, VariableValueModel> metadata) {
        this.fieldPath = fieldPath;
        this.metadata = metadata;
    }

    @Override
    public FormField.Builder toProto() {
        FormField.Builder out = FormField.newBuilder().setFieldPath(fieldPath.toProto());
        metadata.forEach((key, value) -> out.putMetadata(key, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        FormField p = (FormField) proto;
        fieldPath = LHPathModel.fromProto(p.getFieldPath(), context);
        for (Map.Entry<String, VariableValue> entry : p.getMetadataMap().entrySet()) {
            metadata.put(entry.getKey(), VariableValueModel.fromProto(entry.getValue(), context));
        }
    }

    @Override
    public Class<FormField> getProtoBaseClass() {
        return FormField.class;
    }
}
