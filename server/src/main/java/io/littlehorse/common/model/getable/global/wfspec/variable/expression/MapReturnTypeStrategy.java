package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Optional;

public class MapReturnTypeStrategy implements LHTypeStrategy {

    private final InlineMapDefModel inlineMapDef;

    public MapReturnTypeStrategy(InlineMapDefModel inlineMapDef) {
        this.inlineMapDef = inlineMapDef;
    }

    @Override
    public TypeDefinitionModel getIdentity() {
        return new TypeDefinitionModel(inlineMapDef);
    }

    @Override
    public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot add to a Map. Use EXTEND to put an entry.");
    }

    @Override
    public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot subtract from a Map. Use REMOVE_KEY instead.");
    }

    @Override
    public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply a Map.");
    }

    @Override
    public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide a Map.");
    }

    @Override
    public Optional<TypeDefinitionModel> pow(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot exponentiate a Map.");
    }

    @Override
    public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_IF_PRESENT on a Map. Use REMOVE_KEY instead.");
    }

    @Override
    public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        if (inlineMapDef.getKeyType() != null
                && !inlineMapDef.getKeyType().isNull()
                && !inlineMapDef.getKeyType().isCompatibleWith(other.getIdentity())) {
            throw new InvalidExpressionException("Cannot call REMOVE_KEY on a Map with key type "
                    + inlineMapDef.getKeyType() + " using argument of type " + other.getIdentity());
        }
        return Optional.of(getIdentity());
    }

    @Override
    public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot call REMOVE_INDEX on a Map. Use REMOVE_KEY instead.");
    }

    @Override
    public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
            throws InvalidExpressionException {
        TypeDefinitionModel otherIdentity = other.getIdentity();
        if (otherIdentity.getDefinedTypeCase() == DefinedTypeCase.INLINE_MAP_DEF) {
            return Optional.of(getIdentity());
        }
        throw new InvalidExpressionException("Cannot extend a Map with type " + otherIdentity
                + ". EXTEND on a Map requires a Map argument to merge into this Map.");
    }

    @Override
    public boolean isNumeric(ReadOnlyMetadataManager manager) {
        return false;
    }

    @Override
    public String getDescription() {
        return inlineMapDef.toString();
    }
}
