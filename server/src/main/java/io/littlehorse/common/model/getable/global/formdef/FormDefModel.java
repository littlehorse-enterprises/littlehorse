package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormDef;
import io.littlehorse.sdk.common.proto.FormFieldDef;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormDefModel extends MetadataGetable<FormDef> {

    private FormDefIdModel id;
    private StructDefIdModel resultStructDefId;
    private List<FormFieldDefModel> fields = new ArrayList<>();
    private Map<String, FormMetadataValueModel> metadata = new HashMap<>();
    private Date createdAt;

    @Override
    public FormDef.Builder toProto() {
        FormDef.Builder out = FormDef.newBuilder()
                .setResultStructDefId(resultStructDefId.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()));
        if (id != null) out.setId(id.toProto());
        fields.forEach(field -> out.addFields(field.toProto()));
        metadata.forEach((key, value) -> out.putMetadata(key, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        FormDef proto = (FormDef) p;
        id = LHSerializable.fromProto(proto.getId(), FormDefIdModel.class, context);
        resultStructDefId = LHSerializable.fromProto(proto.getResultStructDefId(), StructDefIdModel.class, context);
        for (FormFieldDef field : proto.getFieldsList()) {
            fields.add(LHSerializable.fromProto(field, FormFieldDefModel.class, context));
        }
        for (Map.Entry<String, FormMetadataValue> entry : proto.getMetadataMap().entrySet()) {
            metadata.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), FormMetadataValueModel.class, context));
        }
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }

    /**
     * Returns true if the content matches, ignoring the id and creation time. Map entry order is ignored.
     */
    public boolean contentEquals(FormDefModel other) {
        return contentProto().equals(other.contentProto());
    }

    private FormDef contentProto() {
        return toProto().clearId().clearCreatedAt().build();
    }

    public Set<String> getPlaceholderNames() {
        Set<String> out = new HashSet<>();
        addPlaceholderNames(metadata, out);
        for (FormFieldDefModel field : fields) {
            addPlaceholderNames(field.getMetadata(), out);
        }
        return out;
    }

    /**
     * Builds the runtime Form, replacing placeholder references with their values. A placeholder without a
     * value omits that metadata entry.
     */
    public FormModel resolve(Map<String, VariableValueModel> placeholderValues) {
        List<FormFieldModel> resolvedFields = new ArrayList<>();
        for (FormFieldDefModel field : fields) {
            resolvedFields.add(
                    new FormFieldModel(field.getFieldPath(), resolveMetadata(field.getMetadata(), placeholderValues)));
        }
        return new FormModel(id, resolvedFields, resolveMetadata(metadata, placeholderValues));
    }

    private static void addPlaceholderNames(Map<String, FormMetadataValueModel> values, Set<String> out) {
        for (FormMetadataValueModel value : values.values()) {
            if (value.getPlaceholder() != null) out.add(value.getPlaceholder());
        }
    }

    private static Map<String, VariableValueModel> resolveMetadata(
            Map<String, FormMetadataValueModel> values, Map<String, VariableValueModel> placeholderValues) {
        Map<String, VariableValueModel> out = new HashMap<>();
        values.forEach((key, value) -> value.resolve(placeholderValues).ifPresent(resolved -> out.put(key, resolved)));
        return out;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    @Override
    public FormDefIdModel getObjectId() {
        return id;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Class<FormDef> getProtoBaseClass() {
        return FormDef.class;
    }
}
