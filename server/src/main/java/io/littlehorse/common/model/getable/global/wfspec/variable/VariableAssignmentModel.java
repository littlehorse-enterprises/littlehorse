package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.ExpressionModel;
import io.littlehorse.common.util.TypeCastingUtils;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class VariableAssignmentModel extends LHSerializable<VariableAssignment> {

    private String jsonPath;
    private SourceCase rhsSourceType;
    private String variableName;
    private VariableValueModel rhsLiteralValue;
    private FormatStringModel formatString;
    private NodeOutputReferenceModel nodeOutputReference;
    private ExpressionModel expression;
    private TypeDefinitionModel castTo;

    public Class<VariableAssignment> getProtoBaseClass() {
        return VariableAssignment.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableAssignment p = (VariableAssignment) proto;
        if (p.hasJsonPath()) jsonPath = p.getJsonPath();
        if (p.hasCastTo()) castTo = TypeDefinitionModel.fromProto(p.getCastTo(), context);

        rhsSourceType = p.getSourceCase();
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                variableName = p.getVariableName();
                break;
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValueModel.fromProto(p.getLiteralValue(), context);
                break;
            case FORMAT_STRING:
                formatString = LHSerializable.fromProto(p.getFormatString(), FormatStringModel.class, context);
                break;
            case NODE_OUTPUT:
                nodeOutputReference =
                        LHSerializable.fromProto(p.getNodeOutput(), NodeOutputReferenceModel.class, context);
                break;
            case EXPRESSION:
                expression = LHSerializable.fromProto(p.getExpression(), ExpressionModel.class, context);
                break;
            case SOURCE_NOT_SET:
                // nothing to do;
        }
    }

    public VariableAssignment.Builder toProto() {
        VariableAssignment.Builder out = VariableAssignment.newBuilder();

        if (jsonPath != null) out.setJsonPath(jsonPath);
        if (castTo != null) out.setCastTo(castTo.toProto());

        switch (rhsSourceType) {
            case VARIABLE_NAME:
                out.setVariableName(variableName);
                break;
            case LITERAL_VALUE:
                out.setLiteralValue(rhsLiteralValue.toProto());
                break;
            case FORMAT_STRING:
                out.setFormatString(formatString.toProto());
                break;
            case NODE_OUTPUT:
                out.setNodeOutput(nodeOutputReference.toProto());
                break;
            case EXPRESSION:
                out.setExpression(expression.toProto());
                break;
            case SOURCE_NOT_SET:
                // not possible.
        }

        return out;
    }

    public static VariableAssignmentModel fromProto(VariableAssignment proto, ExecutionContext context) {
        VariableAssignmentModel out = new VariableAssignmentModel();
        out.initFrom(proto, context);
        return out;
    }

    public Set<String> getRequiredWfRunVarNames() {
        Set<String> out = new HashSet<>();
        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            out.add(variableName);
        }
        if (rhsSourceType == SourceCase.FORMAT_STRING) {
            out.addAll(formatString.getFormat().getRequiredWfRunVarNames());
            for (VariableAssignmentModel arg : formatString.getArgs()) {
                out.addAll(arg.getRequiredWfRunVarNames());
            }
        }
        return out;
    }

    public boolean canBeType(TypeDefinitionModel type, ThreadSpecModel tspec) {
        // TODO: extend this to support Struct and StructDef when we implement that.
        return canBeType(type.getType(), tspec);
    }

    public Optional<TypeDefinitionModel> resolveType(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        // If there's an explicit cast, return the cast target type
        if (castTo != null) {
            return Optional.of(castTo);
        }

        return getSourceType(manager, wfSpec, threadSpecName);
    }

    public boolean canBeType(VariableType type, ThreadSpecModel tspec) {
        // If there's an explicit cast, check if the cast target can be the required type
        if (castTo != null) {
            return TypeCastingUtils.canCastTo(castTo.getType(), type);
        }

        // Eww, gross...I really wish I designed strong typing into the system from day 1.
        if (jsonPath != null) return true;

        VariableType baseType = null;

        switch (rhsSourceType) {
            case VARIABLE_NAME:
                VariableDefModel varDef = tspec.getVarDef(variableName).getVarDef();

                // This will need to be refactored once we introduce Structs and StructDefs.
                baseType = varDef.getTypeDef().getType();
                break;
            case LITERAL_VALUE:
                baseType = rhsLiteralValue.getTypeDefinition().getType();
                break;
            case FORMAT_STRING:
                baseType = VariableType.STR;
                break;
            case NODE_OUTPUT:
            case EXPRESSION:
                // For node outputs and expressions, use the utility's conservative approach
                return TypeCastingUtils.canBeTypeWithoutCast(type, true);
            case SOURCE_NOT_SET:
                // Poorly behaved clients (i.e. someone building a WfSpec by hand) could pass in
                // protobuf that does not set the source type. Instead of throwing an IllegalStateException
                // we should throw an error that will get propagated back to the client.
                //
                // The problem with this is that in this scope we lack context about which node has the
                // invalid VariableAssignment, so the client may have trouble determining the source. Still
                // it is better to return INVALID_ARGUMENT than INTERNAL.
                throw new LHApiException(Status.INVALID_ARGUMENT, "VariableAssignment passed with missing source");
        }

        // Use utility to check if assignment is valid without explicit cast
        return TypeCastingUtils.canAssignWithoutCast(baseType, type);
    }

    /**
     * Resolves the value of this VariableAssignment and applies casting if specified.
     * This method should be called during workflow execution to get the final typed value.
     *
     * @param sourceValue The resolved value before casting
     * @return The value after applying any specified casting
     * @throws LHVarSubError if casting fails
     */
    public VariableValueModel applyCastTo(VariableValueModel sourceValue) throws LHVarSubError {
        if (castTo == null) {
            return sourceValue; // No casting needed
        }

        try {
            return castTo.castTo(sourceValue);
        } catch (IllegalArgumentException e) {
            throw new LHVarSubError(e, "Failed to cast value to " + castTo.getType() + ": " + e.getMessage());
        }
    }

    /**
     * Returns true if this VariableAssignment has an explicit cast specified.
     */
    public boolean hasCast() {
        return castTo != null;
    }

    /**
     * Gets the cast target type, or null if no cast is specified.
     * This is used for validation purposes.
     */
    public TypeDefinitionModel getCastTo() {
        return castTo;
    }

    /**
     * Gets the source type of this VariableAssignment without considering any casting.
     * This method resolves the actual type of the source value before any casting is applied.
     *
     * @param manager The metadata manager for resolving types
     * @param wfSpec The workflow specification
     * @param threadSpecName The thread specification name
     * @return The source type before any casting, or empty if it cannot be determined
     */
    public Optional<TypeDefinitionModel> getSourceType(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        if (jsonPath != null) {
            // There is no way to know what this `VariableAssignment` resolves to if there is a jsonpath in use,
            // which is why I wish we could kill JSON_OBJ with fire. Unfortunately, people use it...
            return Optional.empty();
        }

        switch (rhsSourceType) {
            case VARIABLE_NAME:
                return Optional.of(wfSpec.fetchThreadSpec(threadSpecName)
                        .getVarDef(variableName)
                        .getVarDef()
                        .getTypeDef());
            case LITERAL_VALUE:
                return Optional.of(rhsLiteralValue.getTypeDefinition());
            case FORMAT_STRING:
                return Optional.of(new TypeDefinitionModel(VariableType.STR));
            case NODE_OUTPUT:
                // TODO: handle here if nodeOutputType is a STRUCT and we access a field on it.
                NodeModel node = wfSpec.fetchThreadSpec(threadSpecName).getNode(nodeOutputReference.getNodeName());
                if (node == null) {
                    throw new InvalidExpressionException("Node " + nodeOutputReference.getNodeName()
                            + " not present in threadspec " + threadSpecName);
                }
                Optional<ReturnTypeModel> returnTypeOption = node.getOutputType(manager);
                if (returnTypeOption.isPresent()) {
                    return returnTypeOption.get().getOutputType();
                } else {
                    return Optional.empty();
                }
            case EXPRESSION:
                // can be a given type.
                return expression.resolveTypeDefinition(manager, wfSpec, threadSpecName);
            case SOURCE_NOT_SET:
                // Poorly behaved clients (i.e. someone building a WfSpec by hand) could pass in
                // protobuf that does not set the source type. Instead of throwing an IllegalStateException
                // we should throw an error that will get propagated back to the client.
                //
                // The problem with this is that in this scope we lack context about which node has the
                // invalid VariableAssignment, so the client may have trouble determining the source. Still
                // it is better to return INVALID_ARGUMENT than INTERNAL.
                throw new InvalidExpressionException("VariableAssignment passed with missing source");
        }

        return Optional.empty();
    }
}
