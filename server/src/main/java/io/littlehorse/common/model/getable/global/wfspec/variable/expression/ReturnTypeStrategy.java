package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public interface ReturnTypeStrategy {

    default boolean isPrimitive(ReadOnlyMetadataManager manager) {
        return getIdentity().isPrimitive();
    }

    default Optional<TypeDefinitionModel> resolveOperation(
            ReadOnlyMetadataManager manager, VariableMutationType mutation, ReturnTypeStrategy other)
            throws InvalidExpressionException {
        switch (mutation) {
            case ASSIGN:
                return Optional.of(getIdentity());
            case ADD:
                return add(manager, other);
            case SUBTRACT:
                return subtract(manager, other);
            case DIVIDE:
                return divide(manager, other);
            case MULTIPLY:
                return multiply(manager, other);
            case REMOVE_IF_PRESENT:
                return removeIfPresent(manager, other);
            case REMOVE_INDEX:
                return removeIndex(manager, other);
            case REMOVE_KEY:
                return removeKey(manager, other);
            case EXTEND:
                return extend(manager, other);
            case UNRECOGNIZED:
        }
        throw new IllegalStateException();
    }

    boolean isNumeric(ReadOnlyMetadataManager manager);

    TypeDefinitionModel getIdentity();

    Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, ReturnTypeStrategy other)
            throws InvalidExpressionException;
}
