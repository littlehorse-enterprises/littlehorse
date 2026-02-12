package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public interface LHTypeStrategy {

    default boolean isPrimitive(ReadOnlyMetadataManager manager) {
        return getIdentity().isPrimitive();
    }

    default Optional<TypeDefinitionModel> resolveOperation(
            ReadOnlyMetadataManager manager, Operation mutation, LHTypeStrategy other)
            throws InvalidExpressionException {
        switch (mutation) {
            case ASSIGN:
                return assign(other);
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

    default Optional<TypeDefinitionModel> assign(LHTypeStrategy other) throws InvalidExpressionException {
        if (!other.getIdentity().isCompatibleWith(getIdentity())) {
            throw new InvalidExpressionException(
                    "Cannot use a " + other.getDescription() + " as a " + this.getDescription());
        }
        // Absent a cast, the LHS preserves its type.
        return Optional.of(getIdentity());
    }

    String getDescription();

    TypeDefinitionModel getIdentity();

    Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;

    Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException;
}
