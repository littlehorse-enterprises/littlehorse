package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.InlineStructBuilder;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue.StructValueCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public class InlineStructBuilderModel extends LHSerializable<InlineStructBuilder> {

    private final Map<String, InlineStructFieldValueModel> fields = new HashMap<>();

    @Override
    public Class<InlineStructBuilder> getProtoBaseClass() {
        return InlineStructBuilder.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InlineStructBuilder p = (InlineStructBuilder) proto;
        for (Map.Entry<String, io.littlehorse.sdk.common.proto.InlineStructFieldValue> entry : p.getFieldsMap().entrySet()) {
            fields.put(entry.getKey(), InlineStructFieldValueModel.fromProto(entry.getValue(), context));
        }
    }

    @Override
    public InlineStructBuilder.Builder toProto() {
        InlineStructBuilder.Builder out = InlineStructBuilder.newBuilder();
        for (Map.Entry<String, InlineStructFieldValueModel> entry : fields.entrySet()) {
            out.putFields(entry.getKey(), entry.getValue().toProto().build());
        }
        return out;
    }

    public Set<String> getRequiredWfRunVarNames() {
        Set<String> out = new HashSet<>();
        for (InlineStructFieldValueModel field : fields.values()) {
            if (field.getStructValueCase() == StructValueCase.SIMPLE_VALUE) {
                out.addAll(field.getSimpleValue().getRequiredWfRunVarNames());
            } else if (field.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
                out.addAll(field.getSubStructure().getRequiredWfRunVarNames());
            }
        }
        return out;
    }

    public Set<String> getRequiredNodeNames() {
        Set<String> out = new HashSet<>();
        for (InlineStructFieldValueModel field : fields.values()) {
            if (field.getStructValueCase() == StructValueCase.SIMPLE_VALUE) {
                out.addAll(field.getSimpleValue().getRequiredNodeNames());
            } else if (field.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
                out.addAll(field.getSubStructure().getRequiredNodeNames());
            }
        }
        return out;
    }

    public Collection<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        for (InlineStructFieldValueModel field : fields.values()) {
            if (field.getStructValueCase() == StructValueCase.SIMPLE_VALUE) {
                out.addAll(field.getSimpleValue().getRequiredVariableNames());
            } else if (field.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
                out.addAll(field.getSubStructure().getRequiredVariableNames());
            }
        }
        return out;
    }

    public void validateAgainst(
            TypeDefinitionModel expectedType,
            NodeModel source,
            ReadOnlyMetadataManager manager,
            ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        if (expectedType == null || expectedType.isNull() || expectedType.getStructDefId() == null) {
            throw new InvalidExpressionException(source.getName() + " Does not resolve to a STRUCT value");
        }

        StructBuilderModel.validateInlineStructBuilder(
                this, expectedType, source, manager, threadSpec.getWfSpec(), threadSpec.getName(), threadSpec);
    }

    public Optional<TypeDefinitionModel> getNestedTypeForField(
            String fieldName,
            ReadOnlyMetadataManager manager,
            WfSpecModel wfSpec,
            String threadSpecName,
            TypeDefinitionModel expectedStructType)
            throws InvalidExpressionException {
        InlineStructFieldValueModel field = fields.get(fieldName);
        if (field == null) {
            return Optional.empty();
        }

        if (field.getStructValueCase() == StructValueCase.SIMPLE_VALUE) {
            return field.getSimpleValue().resolveType(manager, wfSpec, threadSpecName);
        }

        if (field.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
            return Optional.ofNullable(expectedStructType);
        }

        return Optional.empty();
    }

    public static InlineStructBuilderModel fromProto(InlineStructBuilder proto, ExecutionContext context) {
        InlineStructBuilderModel out = new InlineStructBuilderModel();
        out.initFrom(proto, context);
        return out;
    }
}
