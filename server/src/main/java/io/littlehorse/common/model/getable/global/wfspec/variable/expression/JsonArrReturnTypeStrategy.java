package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class JsonArrReturnTypeStrategy implements ReturnTypeStrategy {
    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(VariableType.JSON_ARR);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        // You can add whatever you want to a JSON_ARR
        return Optional.of(new TypeDefinitionModel(VariableType.JSON_ARR));
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot add to a JSON_ARR");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply to a JSON_ARR");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a JSON_ARR");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isPrimitive(manager)) {
            throw new InvalidExpressionException(
                    "Cannot call REMOVE_IF_PRESENT on a JSON_ARR with non-primitive argument: " + other.getIdentity());
        }
        return Optional.of(new TypeDefinitionModel(VariableType.JSON_ARR));
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_KEY on a JSON_ARR. Did you mean REMOVE_INDEX?");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isNumeric(manager)) {
            throw new InvalidExpressionException(
                    "Cannot call REMOVE_INDEX on a JSON_ARR with non-numeric argument: " + other.getIdentity());
        }
        return Optional.of(new TypeDefinitionModel(VariableType.JSON_ARR));
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        if (other instanceof JsonArrReturnTypeStrategy) {
            return Optional.of(new TypeDefinitionModel(VariableType.JSON_ARR));
        }
        throw new InvalidExpressionException(
                "Cannot call EXTEND on a JSON_ARR if rhs is not of type JSON_ARR: " + other.getIdentity());
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }
}
