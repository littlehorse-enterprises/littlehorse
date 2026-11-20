package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.LHPathModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormFieldDef;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class FormFieldDefModel extends LHSerializable<FormFieldDef> {

    private LHPathModel fieldPath;
    private Map<String, FormMetadataValueModel> metadata = new HashMap<>();

    @Override
    public FormFieldDef.Builder toProto() {
        FormFieldDef.Builder out = FormFieldDef.newBuilder().setFieldPath(fieldPath.toProto());
        metadata.forEach((key, value) -> out.putMetadata(key, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        FormFieldDef p = (FormFieldDef) proto;
        fieldPath = LHPathModel.fromProto(p.getFieldPath(), context);
        for (Map.Entry<String, FormMetadataValue> entry : p.getMetadataMap().entrySet()) {
            metadata.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), FormMetadataValueModel.class, context));
        }
    }

    @Override
    public Class<FormFieldDef> getProtoBaseClass() {
        return FormFieldDef.class;
    }
}
