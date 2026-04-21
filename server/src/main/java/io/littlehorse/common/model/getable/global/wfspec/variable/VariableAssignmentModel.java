package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidEdgeException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.ExpressionModel;
import io.littlehorse.common.util.TypeCastingUtils;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.PathCase;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class VariableAssignmentModel extends LHSerializable<VariableAssignment> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VariableAssignmentModel.class);
    private PathCase pathCase;
    private String jsonPath;
    private LHPathModel lhPath;
    private SourceCase rhsSourceType;
    private String variableName;
    private VariableValueModel rhsLiteralValue;
    private FormatStringModel formatString;
    private NodeOutputReferenceModel nodeOutputReference;
    private ExpressionModel expression;
    private TypeDefinitionModel targetType;

    public Class<VariableAssignment> getProtoBaseClass() {
        return VariableAssignment.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableAssignment p = (VariableAssignment) proto;
        if (p.hasTargetType()) targetType = TypeDefinitionModel.fromProto(p.getTargetType(), context);
        pathCase = p.getPathCase();
        switch (pathCase) {
            case JSON_PATH:
                jsonPath = p.getJsonPath();
                break;
            case LH_PATH:
                lhPath = LHPathModel.fromProto(p.getLhPath(), context);
                break;
            case PATH_NOT_SET:
        }
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
        }
        // nothing to do;
    }

    public VariableAssignment.Builder toProto() {
        VariableAssignment.Builder out = VariableAssignment.newBuilder();
        if (targetType != null) out.setTargetType(targetType.toProto());
        switch (pathCase) {
            case JSON_PATH:
                out.setJsonPath(jsonPath);
                break;
            case LH_PATH:
                out.setLhPath(lhPath.toProto());
                break;
            case PATH_NOT_SET:
        }
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
        }
        // not possible.
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

    public Optional<TypeDefinitionModel> resolveType(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        if (targetType != null) {
            return Optional.of(targetType);
        }
        return getSourceType(manager, wfSpec, threadSpecName);
    }

    public boolean canBeType(TypeDefinitionModel type, ThreadSpecModel tspec) {
        // Eww, gross...I really wish I designed strong typing into the system from day 1.
        if (jsonPath != null) return true;
        TypeDefinitionModel baseType = null;
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                VariableDefModel varDef = tspec.getVarDef(variableName).getVarDef();
                // This will need to be refactored once we introduce Structs and StructDefs.
                baseType = varDef.getTypeDef();
                break;
            case LITERAL_VALUE:
                baseType = rhsLiteralValue.getTypeDefinition();
                break;
            case FORMAT_STRING:
                baseType = new TypeDefinitionModel(VariableType.STR);
                break;
            case NODE_OUTPUT:
            case EXPRESSION:
                // TODO (#1124): look at the node to determine if the output of the node
                // can be a given type.
                return true;
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
        return TypeCastingUtils.canBeType(baseType, type);
    }

    /**
     * Resolves the value of this VariableAssignment and applies casting if specified.
     * This method should be called during workflow execution to get the final typed value.
     *
     * @param sourceValue The resolved value before casting
     * @return The value after applying any specified casting
     * @throws LHVarSubError if casting fails
     */
    public VariableValueModel applyCast(VariableValueModel sourceValue) throws LHVarSubError {
        if (targetType == null) {
            return sourceValue;
        }
        try {
            return targetType.applyCast(sourceValue);
        } catch (IllegalArgumentException e) {
            throw new LHVarSubError(e, "Failed to cast value to " + targetType + ": " + e.getMessage());
        }
    }

    /**
     * Gets the source type of this VariableAssignment
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
        TypeDefinitionModel typeDef = null;
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                typeDef = wfSpec.fetchThreadSpec(threadSpecName)
                        .getVarDef(variableName)
                        .getVarDef()
                        .getTypeDef();
                break;
            case LITERAL_VALUE:
                typeDef = rhsLiteralValue.getTypeDefinition();
                break;
            case FORMAT_STRING:
                typeDef = new TypeDefinitionModel(VariableType.STR);
                break;
            case NODE_OUTPUT:
                // TODO: handle here if nodeOutputType is a STRUCT and we access a field on it.
                NodeModel node = wfSpec.fetchThreadSpec(threadSpecName).getNode(nodeOutputReference.getNodeName());
                if (node == null) {
                    throw new InvalidExpressionException("Node " + nodeOutputReference.getNodeName()
                            + " not present in threadspec " + threadSpecName);
                }
                Optional<ReturnTypeModel> returnTypeOption = node.getOutputType(manager);
                if (returnTypeOption.isPresent()
                        && returnTypeOption.get().getOutputType().isPresent()) {
                    typeDef = returnTypeOption.get().getOutputType().get();
                }
                break;
            case EXPRESSION:
                // can be a given type.
                Optional<TypeDefinitionModel> expressionTypeDef =
                        expression.resolveTypeDefinition(manager, wfSpec, threadSpecName);
                if (expressionTypeDef.isPresent()) {
                    typeDef = expressionTypeDef.get();
                }
                break;
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
        if (lhPath != null) {
            return typeDef.getNestedType(lhPath, manager);
        }
        return Optional.ofNullable(typeDef);
    }

    public boolean canBeType(VariableType type, ThreadSpecModel tspec) {
        return canBeType(new TypeDefinitionModel(type), tspec);
    }

    public void validate(NodeModel source, MetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidEdgeException, InvalidExpressionException {
        if (expression != null) {
            expression.validate(source, manager, threadSpec);
        } else {
            Optional<TypeDefinitionModel> sourceType = getSourceType(manager, threadSpec.wfSpec, threadSpec.getName());
            if (sourceType.isEmpty()
                    || !sourceType.get().isCompatibleWith(new TypeDefinitionModel(VariableType.BOOL))) {
                throw new InvalidExpressionException(source.getName() + " Does not resolve to a BOOL value");
            }
        }
    }

    public boolean isSatisfied(ThreadRunModel threadRun) throws LHVarSubError {
        if (expression != null) {
            return expression.isSatisfied(threadRun);
        } else {
            return threadRun.assignVariable(this).getBoolVal();
        }
    }

    public Collection<String> getRequiredVariableNames() {
        final Set<String> out = new HashSet<>();
        if (expression != null) {
            out.addAll(expression.getLhs().getRequiredVariableNames());
            out.addAll(expression.getRhs().getRequiredVariableNames());
        }
        return out;
    }

    public VariableAssignmentModel() {}

    public PathCase getPathCase() {
        return this.pathCase;
    }

    public String getJsonPath() {
        return this.jsonPath;
    }

    public LHPathModel getLhPath() {
        return this.lhPath;
    }

    public SourceCase getRhsSourceType() {
        return this.rhsSourceType;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public VariableValueModel getRhsLiteralValue() {
        return this.rhsLiteralValue;
    }

    public FormatStringModel getFormatString() {
        return this.formatString;
    }

    public NodeOutputReferenceModel getNodeOutputReference() {
        return this.nodeOutputReference;
    }

    public ExpressionModel getExpression() {
        return this.expression;
    }

    public TypeDefinitionModel getTargetType() {
        return this.targetType;
    }

    public void setPathCase(final PathCase pathCase) {
        this.pathCase = pathCase;
    }

    public void setJsonPath(final String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public void setLhPath(final LHPathModel lhPath) {
        this.lhPath = lhPath;
    }

    public void setRhsSourceType(final SourceCase rhsSourceType) {
        this.rhsSourceType = rhsSourceType;
    }

    public void setVariableName(final String variableName) {
        this.variableName = variableName;
    }

    public void setRhsLiteralValue(final VariableValueModel rhsLiteralValue) {
        this.rhsLiteralValue = rhsLiteralValue;
    }

    public void setFormatString(final FormatStringModel formatString) {
        this.formatString = formatString;
    }

    public void setNodeOutputReference(final NodeOutputReferenceModel nodeOutputReference) {
        this.nodeOutputReference = nodeOutputReference;
    }

    public void setExpression(final ExpressionModel expression) {
        this.expression = expression;
    }

    public void setTargetType(final TypeDefinitionModel targetType) {
        this.targetType = targetType;
    }

    @Override
    public String toString() {
        return "VariableAssignmentModel(pathCase=" + this.getPathCase() + ", jsonPath=" + this.getJsonPath()
                + ", lhPath=" + this.getLhPath() + ", rhsSourceType=" + this.getRhsSourceType() + ", variableName="
                + this.getVariableName() + ", rhsLiteralValue=" + this.getRhsLiteralValue() + ", formatString="
                + this.getFormatString() + ", nodeOutputReference=" + this.getNodeOutputReference() + ", expression="
                + this.getExpression() + ", targetType=" + this.getTargetType() + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof VariableAssignmentModel)) return false;
        final VariableAssignmentModel other = (VariableAssignmentModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$pathCase = this.getPathCase();
        final Object other$pathCase = other.getPathCase();
        if (this$pathCase == null ? other$pathCase != null : !this$pathCase.equals(other$pathCase)) return false;
        final Object this$jsonPath = this.getJsonPath();
        final Object other$jsonPath = other.getJsonPath();
        if (this$jsonPath == null ? other$jsonPath != null : !this$jsonPath.equals(other$jsonPath)) return false;
        final Object this$lhPath = this.getLhPath();
        final Object other$lhPath = other.getLhPath();
        if (this$lhPath == null ? other$lhPath != null : !this$lhPath.equals(other$lhPath)) return false;
        final Object this$rhsSourceType = this.getRhsSourceType();
        final Object other$rhsSourceType = other.getRhsSourceType();
        if (this$rhsSourceType == null ? other$rhsSourceType != null : !this$rhsSourceType.equals(other$rhsSourceType))
            return false;
        final Object this$variableName = this.getVariableName();
        final Object other$variableName = other.getVariableName();
        if (this$variableName == null ? other$variableName != null : !this$variableName.equals(other$variableName))
            return false;
        final Object this$rhsLiteralValue = this.getRhsLiteralValue();
        final Object other$rhsLiteralValue = other.getRhsLiteralValue();
        if (this$rhsLiteralValue == null
                ? other$rhsLiteralValue != null
                : !this$rhsLiteralValue.equals(other$rhsLiteralValue)) return false;
        final Object this$formatString = this.getFormatString();
        final Object other$formatString = other.getFormatString();
        if (this$formatString == null ? other$formatString != null : !this$formatString.equals(other$formatString))
            return false;
        final Object this$nodeOutputReference = this.getNodeOutputReference();
        final Object other$nodeOutputReference = other.getNodeOutputReference();
        if (this$nodeOutputReference == null
                ? other$nodeOutputReference != null
                : !this$nodeOutputReference.equals(other$nodeOutputReference)) return false;
        final Object this$expression = this.getExpression();
        final Object other$expression = other.getExpression();
        if (this$expression == null ? other$expression != null : !this$expression.equals(other$expression))
            return false;
        final Object this$targetType = this.getTargetType();
        final Object other$targetType = other.getTargetType();
        if (this$targetType == null ? other$targetType != null : !this$targetType.equals(other$targetType))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VariableAssignmentModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $pathCase = this.getPathCase();
        result = result * PRIME + ($pathCase == null ? 43 : $pathCase.hashCode());
        final Object $jsonPath = this.getJsonPath();
        result = result * PRIME + ($jsonPath == null ? 43 : $jsonPath.hashCode());
        final Object $lhPath = this.getLhPath();
        result = result * PRIME + ($lhPath == null ? 43 : $lhPath.hashCode());
        final Object $rhsSourceType = this.getRhsSourceType();
        result = result * PRIME + ($rhsSourceType == null ? 43 : $rhsSourceType.hashCode());
        final Object $variableName = this.getVariableName();
        result = result * PRIME + ($variableName == null ? 43 : $variableName.hashCode());
        final Object $rhsLiteralValue = this.getRhsLiteralValue();
        result = result * PRIME + ($rhsLiteralValue == null ? 43 : $rhsLiteralValue.hashCode());
        final Object $formatString = this.getFormatString();
        result = result * PRIME + ($formatString == null ? 43 : $formatString.hashCode());
        final Object $nodeOutputReference = this.getNodeOutputReference();
        result = result * PRIME + ($nodeOutputReference == null ? 43 : $nodeOutputReference.hashCode());
        final Object $expression = this.getExpression();
        result = result * PRIME + ($expression == null ? 43 : $expression.hashCode());
        final Object $targetType = this.getTargetType();
        result = result * PRIME + ($targetType == null ? 43 : $targetType.hashCode());
        return result;
    }
}
