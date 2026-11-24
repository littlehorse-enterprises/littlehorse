package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Form;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class FormModel extends LHSerializable<Form> {

    private FormDefIdModel formDefId;
    private List<FormFieldModel> fields = new ArrayList<>();
    private Map<String, VariableValueModel> metadata = new HashMap<>();

    public FormModel() {}

    public FormModel(FormDefIdModel formDefId, List<FormFieldModel> fields, Map<String, VariableValueModel> metadata) {
        this.formDefId = formDefId;
        this.fields = fields;
        this.metadata = metadata;
    }

    @Override
    public Form.Builder toProto() {
        Form.Builder out = Form.newBuilder().setFormDefId(formDefId.toProto());
        fields.forEach(field -> out.addFields(field.toProto()));
        metadata.forEach((key, value) -> out.putMetadata(key, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Form p = (Form) proto;
        formDefId = LHSerializable.fromProto(p.getFormDefId(), FormDefIdModel.class, context);
        for (var field : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(field, FormFieldModel.class, context));
        }
        for (Map.Entry<String, VariableValue> entry : p.getMetadataMap().entrySet()) {
            metadata.put(entry.getKey(), VariableValueModel.fromProto(entry.getValue(), context));
        }
    }

    @Override
    public Class<Form> getProtoBaseClass() {
        return Form.class;
    }
}
