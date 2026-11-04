package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class LHPathModel extends LHSerializable<LHPath> {

    @Getter
    private List<Selector> path;

    private List<VariableAssignmentModel> dynamicAssignments = new ArrayList<>();

    public LHPathModel() {}

    public LHPathModel(List<Selector> path) {
        this.path = new ArrayList<>(path);
    }

    @Override
    public LHPath.Builder toProto() {
        LHPath.Builder out = LHPath.newBuilder();
        out.addAllPath(path);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHPath p = (LHPath) proto;
        this.path = Collections.unmodifiableList(p.getPathList());
        for (Selector selector : path) {
            if (selector.hasDynamic()) {
                dynamicAssignments.add(VariableAssignmentModel.fromProto(selector.getDynamic(), context));
            }
        }
    }

    @Override
    public Class<LHPath> getProtoBaseClass() {
        return LHPath.class;
    }

    public static LHPathModel fromProto(LHPath proto, ExecutionContext context) {
        LHPathModel out = new LHPathModel();
        out.initFrom(proto, context);
        return out;
    }

    public Map<Selector, VariableValueModel> resolveDynamicSelectors(
            ThreadRunModel thread, Map<String, VariableValueModel> txnCache) throws LHVarSubError {
        Map<Selector, VariableValueModel> out = new HashMap<>();
        int assignmentIndex = 0;
        for (Selector selector : path) {
            if (selector.hasDynamic()) {
                out.put(selector, thread.assignVariable(dynamicAssignments.get(assignmentIndex++), txnCache));
            }
        }
        return out;
    }

    public void validateDynamicSelectors(
            TypeDefinitionModel rootType, ReadOnlyMetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        int assignmentIndex = 0;
        for (int selectorIndex = 0; selectorIndex < path.size(); selectorIndex++) {
            Selector selector = path.get(selectorIndex);
            if (!selector.hasDynamic()) {
                continue;
            }

            Optional<TypeDefinitionModel> parentType =
                    rootType.getNestedType(new LHPathModel(path.subList(0, selectorIndex)), manager);
            VariableAssignmentModel assignment = dynamicAssignments.get(assignmentIndex++);
            Optional<TypeDefinitionModel> actualType =
                    assignment.resolveType(manager, threadSpec.getWfSpec(), threadSpec.getName());

            if (parentType.isEmpty() || actualType.isEmpty()) {
                continue;
            }
            if (parentType.get().getDefinedTypeCase() == DefinedTypeCase.INLINE_MAP_DEF) {
                TypeDefinitionModel expectedType =
                        parentType.get().getInlineMapDef().getKeyType();
                if (!hasCompatibleMapKeyType(expectedType, actualType.get())) {
                    throw new InvalidExpressionException(
                            "Dynamic selector is not compatible with Map key type " + expectedType);
                }
            } else if (parentType.get().getDefinedTypeCase() == DefinedTypeCase.INLINE_ARRAY_DEF
                    && actualType.get().getPrimitiveType() != io.littlehorse.sdk.common.proto.VariableType.INT) {
                throw new InvalidExpressionException("Dynamic selector for Array must resolve to INT");
            } else if (parentType.get().getDefinedTypeCase() == DefinedTypeCase.PRIMITIVE_TYPE
                    && parentType.get().isJson()
                    && actualType.get().getPrimitiveType() != io.littlehorse.sdk.common.proto.VariableType.STR
                    && actualType.get().getPrimitiveType() != io.littlehorse.sdk.common.proto.VariableType.INT) {
                throw new InvalidExpressionException("Dynamic selector for JSON must resolve to STR or INT");
            } else if (parentType.get().getDefinedTypeCase() == DefinedTypeCase.STRUCT_DEF_ID) {
                throw new InvalidExpressionException("Dynamic selectors are not supported for Struct fields");
            }
        }
    }

    public List<VariableAssignmentModel> getDynamicAssignments() {
        return Collections.unmodifiableList(dynamicAssignments);
    }

    private boolean hasCompatibleMapKeyType(TypeDefinitionModel expectedType, TypeDefinitionModel actualType) {
        if (expectedType.getDefinedTypeCase() == DefinedTypeCase.PRIMITIVE_TYPE
                || actualType.getDefinedTypeCase() == DefinedTypeCase.PRIMITIVE_TYPE) {
            return expectedType.getDefinedTypeCase() == actualType.getDefinedTypeCase()
                    && expectedType.getPrimitiveType() == actualType.getPrimitiveType();
        }
        return expectedType.isCompatibleWith(actualType);
    }

    /**
     * Converts this LHPath to a JSONPath String.
     * @return a JSONPath String.
     */
    public String toJsonPathStr() {
        StringBuilder pathBuilder = new StringBuilder("$");

        for (Selector selector : path) {
            switch (selector.getSelectorTypeCase()) {
                case INDEX:
                    pathBuilder.append(String.format("[%d]", selector.getIndex()));
                    break;
                case KEY:
                    pathBuilder.append("." + selector.getKey());
                    break;
                case DYNAMIC:
                    throw new IllegalStateException("Cannot render a dynamic selector as JSONPath");
                case SELECTORTYPE_NOT_SET:
            }
        }

        return pathBuilder.toString();
    }
}
