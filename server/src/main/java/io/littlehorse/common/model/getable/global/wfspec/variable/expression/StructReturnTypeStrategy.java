package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class StructReturnTypeStrategy implements LHTypeStrategy {

    private final StructDefIdModel structDefId;

    public StructReturnTypeStrategy(StructDefIdModel structDefId) {
        this.structDefId = structDefId;
    }

    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(structDefId);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot add to a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply to a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot remove from a Struct yet");
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot remove a key from a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot remove index from a Struct");
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot extend a Struct");
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }

    @Override
    public String getDescription() {
        return "STRUCT";
    }
}
