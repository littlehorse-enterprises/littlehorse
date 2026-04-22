package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.InlineStructFieldValue.StructValueCase;
import io.littlehorse.sdk.common.proto.StructBuilder;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public class StructBuilderModel extends LHSerializable<StructBuilder> {

    private StructDefIdModel structDefId;
    private InlineStructBuilderModel value;

    @Override
    public Class<StructBuilder> getProtoBaseClass() {
        return StructBuilder.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        StructBuilder p = (StructBuilder) proto;
        structDefId = StructDefIdModel.fromProto(p.getStructDefId(), context);
        value = InlineStructBuilderModel.fromProto(p.getValue(), context);
    }

    @Override
    public StructBuilder.Builder toProto() {
        return StructBuilder.newBuilder().setStructDefId(structDefId.toProto()).setValue(value.toProto());
    }

    public TypeDefinitionModel getTypeDefinition() {
        return new TypeDefinitionModel(structDefId);
    }

    public Set<String> getRequiredWfRunVarNames() {
        return value.getRequiredWfRunVarNames();
    }

    public Set<String> getRequiredNodeNames() {
        return value.getRequiredNodeNames();
    }

    public Collection<String> getRequiredVariableNames() {
        return value.getRequiredVariableNames();
    }

    public void validate(NodeModel source, ReadOnlyMetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        validateAgainst(getTypeDefinition(), source, manager, threadSpec.getWfSpec(), threadSpec.getName());
    }

    public void validateAgainst(
            TypeDefinitionModel expectedType,
            NodeModel source,
            ReadOnlyMetadataManager manager,
            WfSpecModel wfSpec,
            String threadSpecName)
            throws InvalidExpressionException {
        pinStructVersions(manager);

        if (expectedType == null || expectedType.isNull()) {
            expectedType = getTypeDefinition();
        }

        if (!expectedType.isCompatibleWith(getTypeDefinition())) {
            throw new InvalidExpressionException(
                    "Cannot assign " + getTypeDefinition() + " to " + expectedType + " without explicit casting.");
        }

        validateInlineStructBuilder(
                value, expectedType, source, manager, wfSpec, threadSpecName, wfSpec.fetchThreadSpec(threadSpecName));
    }

    static void validateInlineStructBuilder(
            InlineStructBuilderModel builder,
            TypeDefinitionModel expectedType,
            NodeModel source,
            ReadOnlyMetadataManager manager,
            WfSpecModel wfSpec,
            String threadSpecName,
            ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        StructDefModel structDef = new WfService(manager).getStructDef(expectedType.getStructDefId());
        if (structDef == null) {
            throw new InvalidExpressionException("StructDef not found: " + expectedType.getStructDefId());
        }

        Map<String, StructFieldDefModel> fieldDefs = structDef.getStructDef().getFields();
        Set<String> missingRequired = new HashSet<>();

        for (Map.Entry<String, StructFieldDefModel> entry : fieldDefs.entrySet()) {
            if (entry.getValue().isRequired() && !builder.getFields().containsKey(entry.getKey())) {
                missingRequired.add(entry.getKey());
            }
        }

        if (!missingRequired.isEmpty()) {
            throw new InvalidExpressionException(
                    "Missing required field(s) for StructDef "
                            + expectedType.getStructDefId()
                            + ": "
                            + String.join(", ", missingRequired));
        }

        for (Map.Entry<String, InlineStructFieldValueModel> entry : builder.getFields().entrySet()) {
            String fieldName = entry.getKey();
            StructFieldDefModel fieldDef = fieldDefs.get(fieldName);
            if (fieldDef == null) {
                throw new InvalidExpressionException(
                        "StructDef " + expectedType.getStructDefId() + " does not contain field '" + fieldName + "'");
            }

            InlineStructFieldValueModel fieldValue = entry.getValue();
            if (fieldValue.getStructValueCase() == StructValueCase.SIMPLE_VALUE) {
                VariableAssignmentModel assn = fieldValue.getSimpleValue();
                if (assn.getRhsSourceType() == SourceCase.STRUCT_BUILDER) {
                    assn.getStructBuilder().validateAgainst(fieldDef.getFieldType(), source, manager, wfSpec, threadSpecName);
                    continue;
                }
                Optional<TypeDefinitionModel> sourceType = assn.resolveType(manager, wfSpec, threadSpecName);
                if (sourceType.isPresent() && !fieldDef.getFieldType().isCompatibleWith(sourceType.get())) {
                    throw new InvalidExpressionException(
                            "Field '" + fieldName + "' expects " + fieldDef.getFieldType() + " but got " + sourceType.get());
                }
            } else if (fieldValue.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
                TypeDefinitionModel nestedExpected = fieldDef.getFieldType();
                fieldValue.getSubStructure().validateAgainst(nestedExpected, source, manager, threadSpec);
            } else {
                throw new InvalidExpressionException("Field '" + fieldName + "' is missing a value");
            }
        }
    }

    public static StructBuilderModel fromProto(StructBuilder proto, ExecutionContext context) {
        StructBuilderModel out = new StructBuilderModel();
        out.initFrom(proto, context);
        return out;
    }

    private void pinStructVersions(ReadOnlyMetadataManager manager) throws InvalidExpressionException {
        StructDefModel structDef = new WfService(manager).getStructDef(structDefId);
        if (structDef == null) {
            throw new InvalidExpressionException("StructDef not found: " + structDefId);
        }

        structDefId.setVersion(structDef.getObjectId().getVersion());
        pinInlineStructVersions(value, structDef, manager);
    }

    private void pinInlineStructVersions(
            InlineStructBuilderModel builder,
            StructDefModel structDef,
            ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        for (Map.Entry<String, InlineStructFieldValueModel> entry : builder.getFields().entrySet()) {
            StructFieldDefModel fieldDef = structDef.getStructDef().getFields().get(entry.getKey());
            if (fieldDef == null) {
                continue;
            }

            InlineStructFieldValueModel fieldValue = entry.getValue();
            if (fieldValue.getStructValueCase() == StructValueCase.SIMPLE_VALUE
                    && fieldValue.getSimpleValue().getRhsSourceType() == SourceCase.STRUCT_BUILDER) {
                fieldValue.getSimpleValue().getStructBuilder().pinStructVersions(manager);
            } else if (fieldValue.getStructValueCase() == StructValueCase.SUB_STRUCTURE) {
                StructDefModel nestedStructDef = new WfService(manager).getStructDef(fieldDef.getFieldType().getStructDefId());
                if (nestedStructDef == null) {
                    throw new InvalidExpressionException("StructDef not found: " + fieldDef.getFieldType().getStructDefId());
                }
                pinInlineStructVersions(fieldValue.getSubStructure(), nestedStructDef, manager);
            }
        }
    }
}
