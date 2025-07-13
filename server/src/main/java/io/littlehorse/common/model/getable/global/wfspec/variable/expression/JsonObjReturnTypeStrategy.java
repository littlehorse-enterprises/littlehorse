package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class JsonObjReturnTypeStrategy implements ReturnTypeStrategy {
    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(VariableType.JSON_OBJ);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a JSON_OBJ");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a JSON_OBJ");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply to a JSON_OBJ");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a JSON_OBJ");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isPrimitive(manager)) {
            throw new InvalidExpressionException(
                    "Cannot call REMOVE_IF_PRESENT on a JSON_OBJ with non-primitive argument: " + other.getIdentity());
        }
        return Optional.of(new TypeDefinitionModel(VariableType.JSON_OBJ));
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isPrimitive(manager)) {
            throw new InvalidExpressionException(
                    "Cannot call REMOVE_KEY on a JSON_OBJ with non-primitive argument: " + other.getIdentity());
        }
        return Optional.of(new TypeDefinitionModel(VariableType.JSON_OBJ));
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_INDEX on a JSON_OBJ. Did you mean REMOVE_KEY?");
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call EXTEND on a JSON_OBJ");
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }
}
