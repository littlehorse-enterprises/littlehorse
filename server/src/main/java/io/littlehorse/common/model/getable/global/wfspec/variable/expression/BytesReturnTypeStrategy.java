package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class BytesReturnTypeStrategy implements LHTypeStrategy {
    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(VariableType.BYTES);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot add to a BYTES. Use EXTEND instead.");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply to a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_IF_PRESENT on a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_KEY on a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_INDEX on a BYTES");
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other instanceof BytesReturnTypeStrategy) {
            return Optional.of(new TypeDefinitionModel(VariableType.BYTES));
        } else {
            throw new InvalidExpressionException(
                    "Cannot call EXTEND on a BYTES with a non-BYTES rhs: " + other.getIdentity());
        }
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }

    @Override
    public String getDescription() {
        return "BYTES";
    }
}
