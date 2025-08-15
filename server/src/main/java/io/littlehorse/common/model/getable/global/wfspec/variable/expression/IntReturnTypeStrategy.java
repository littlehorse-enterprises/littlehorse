package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class IntReturnTypeStrategy implements LHTypeStrategy {

    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(VariableType.INT);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other.isNumeric(manager)) {
            return Optional.of(new TypeDefinitionModel(VariableType.INT));
        }
        throw new InvalidExpressionException("Cannot add non-numeric type " + other.toString() + " to an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other.isNumeric(manager)) {
            return Optional.of(new TypeDefinitionModel(VariableType.INT));
        }
        throw new InvalidExpressionException("Cannot subtract non-numeric type " + other.toString() + " to an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other.isNumeric(manager)) {
            return Optional.of(new TypeDefinitionModel(VariableType.INT));
        }
        throw new InvalidExpressionException("Cannot multiply non-numeric type " + other.toString() + " to an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (other.isNumeric(manager)) {
            return Optional.of(new TypeDefinitionModel(VariableType.INT));
        }
        throw new InvalidExpressionException("Cannot divide non-numeric type " + other.toString() + " to an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_IF_PRESENT on an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_KEY on an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_INDEX on an INT");
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call EXTEND on an INT");
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return true;
    }

    @Override
    public String getDescription() {
        return "INT";
    }
}
