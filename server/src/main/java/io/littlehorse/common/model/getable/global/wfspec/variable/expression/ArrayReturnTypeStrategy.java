package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import java.util.Optional;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;

public class ArrayReturnTypeStrategy implements LHTypeStrategy {

  private final InlineArrayDefModel inlineArrayDef;

  public ArrayReturnTypeStrategy(InlineArrayDefModel inlineArrayDef) {
    this.inlineArrayDef = inlineArrayDef;
  }

  @Override
  public TypeDefinitionModel getIdentity() {
    return new TypeDefinitionModel(inlineArrayDef);
  }

  @Override
  public Optional<TypeDefinitionModel> add(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
    throw new UnsupportedOperationException("Cannot add to an Array. Use EXTEND instead.");
  }

  @Override
  public Optional<TypeDefinitionModel> subtract(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
    throw new UnsupportedOperationException("Cannot subtract from an Array. Use a REMOVE operation instead.");
  }

  @Override
  public boolean isNumeric(ReadOnlyMetadataManager manager) {
    return false;
  }

  @Override
  public String getDescription() {
    return "ARRAY";
  }

  @Override
  public Optional<TypeDefinitionModel> multiply(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot multiply an Array.");
  }

  @Override
  public Optional<TypeDefinitionModel> divide(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot divide an Array.");
  }

  @Override
  public Optional<TypeDefinitionModel> removeIfPresent(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
      if (matchesArrayDefType(other.getIdentity())) {
        return Optional.of(getIdentity());
      }
      throw new InvalidExpressionException("Cannot call REMOVE_IF_PRESENT on an Array of type " + this.inlineArrayDef.getArrayType() + " with argument of type " + other.getIdentity());
  }

  @Override
  public Optional<TypeDefinitionModel> removeKey(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
    throw new InvalidExpressionException("Cannot call REMOVE_KEY on an ARRAY. Did you mean REMOVE_INDEX?");
  }

  @Override
  public Optional<TypeDefinitionModel> removeIndex(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
    if (!other.isNumeric(manager)) {
        throw new InvalidExpressionException(
                "Cannot call REMOVE_INDEX on an ARRAY with non-numeric argument: " + other.getIdentity());
    }
    return Optional.of(getIdentity());
  }

  @Override
  public Optional<TypeDefinitionModel> extend(ReadOnlyMetadataManager manager, LHTypeStrategy other)
      throws InvalidExpressionException {
    if (matchesArrayDefType(other.getIdentity())) {
      return Optional.of(getIdentity());
    }
    throw new UnsupportedOperationException("Cannot extend an Array of " + this.inlineArrayDef.getArrayType() + " with type " + other.getIdentity());
  }

  private boolean matchesArrayDefType(TypeDefinitionModel other) {
    return this.inlineArrayDef.getArrayType().equals(other);
  }
  
}
