package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class StrReturnTypeStrategy implements LHTypeStrategy {

    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(VariableType.STR);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other.isPrimitive(manager)) {
            return Optional.of(new TypeDefinitionModel(VariableType.STR));
        }
        throw new InvalidExpressionException(
                "Cannot add non-primitive type " + other.getIdentity().toString() + " to an DOUBLE");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a STR");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply to a STR");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a STR");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isPrimitive(manager)) {
            throw new InvalidExpressionException("Cannot call REMOVE_IF_PRESENT with non-primitive type on a STR");
        }
        return Optional.of(new TypeDefinitionModel(VariableType.STR));
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_KEY on a STR");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isNumeric(manager)) {
            throw new InvalidExpressionException(
                    "Cannot use non-numeric type " + other.getIdentity() + " as key for REMOVE_INDEX");
        }
        return Optional.of(new TypeDefinitionModel(VariableType.STR));
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (!other.isPrimitive(manager)) {
            throw new InvalidExpressionException("Cannot extend a STR with non-primitive type " + other.getIdentity());
        }
        return Optional.of(new TypeDefinitionModel(VariableType.STR));
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }

    @Override
    public String getDescription() {
        return "STR";
    }
}
