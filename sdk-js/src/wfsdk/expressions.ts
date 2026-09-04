import { Comparator } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { VariableMutationType } from '../proto/common_wfspec'

/**
 * Any value usable on either side of an expression or as a node/task argument:
 * a literal, a WfRunVariable, a NodeOutput, another expression, or a format
 * string. (The Java SDK types this as Serializable.)
 */
export type LHValue = unknown

/**
 * Base class providing the expression-building methods shared by expressions,
 * WfRunVariables, and anything else usable in an expression (mirrors the
 * default methods on the Java LHExpression interface).
 */
export abstract class LHExpressionBase {
  add(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.ADD }, other)
  }

  subtract(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.SUBTRACT }, other)
  }

  multiply(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.MULTIPLY }, other)
  }

  divide(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.DIVIDE }, other)
  }

  pow(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.POW }, other)
  }

  extend(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.EXTEND }, other)
  }

  removeIfPresent(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.REMOVE_IF_PRESENT }, other)
  }

  removeIndex(index: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.REMOVE_INDEX }, index)
  }

  removeKey(key: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.REMOVE_KEY }, key)
  }

  and(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.AND }, other)
  }

  or(other: LHValue): LHExpression {
    return new LHExpressionImpl(this, { mutation: VariableMutationType.OR }, other)
  }

  size(): LHExpression {
    return new SizeOfExpressionImpl(this)
  }

  isLessThan(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.LESS_THAN }, rhs)
  }

  isGreaterThan(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.GREATER_THAN }, rhs)
  }

  isLessThanEq(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.LESS_THAN_EQ }, rhs)
  }

  isGreaterThanEq(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.GREATER_THAN_EQ }, rhs)
  }

  isEqualTo(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.EQUALS }, rhs)
  }

  isNotEqualTo(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.NOT_EQUALS }, rhs)
  }

  doesContain(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(rhs, { comparator: Comparator.IN }, this)
  }

  doesNotContain(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(rhs, { comparator: Comparator.NOT_IN }, this)
  }

  isIn(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.IN }, rhs)
  }

  isNotIn(rhs: LHValue): LHExpression {
    return new LHExpressionImpl(this, { comparator: Comparator.NOT_IN }, rhs)
  }

  castTo(targetType: VariableType): LHExpression {
    return new CastExpressionImpl(this, targetType)
  }

  castToInt(): LHExpression {
    return this.castTo(VariableType.INT)
  }

  castToDouble(): LHExpression {
    return this.castTo(VariableType.DOUBLE)
  }

  castToStr(): LHExpression {
    return this.castTo(VariableType.STR)
  }

  castToBool(): LHExpression {
    return this.castTo(VariableType.BOOL)
  }

  castToBytes(): LHExpression {
    return this.castTo(VariableType.BYTES)
  }

  castToWfRunId(): LHExpression {
    return this.castTo(VariableType.WF_RUN_ID)
  }
}

export type LHExpression = LHExpressionBase

type Operation = { mutation: VariableMutationType; comparator?: never } | { comparator: Comparator; mutation?: never }

export class LHExpressionImpl extends LHExpressionBase {
  readonly lhs: LHValue
  readonly rhs: LHValue
  readonly mutation?: VariableMutationType
  readonly comparator?: Comparator

  constructor(lhs: LHValue, operation: Operation, rhs: LHValue) {
    super()
    this.lhs = lhs
    this.rhs = rhs
    this.mutation = operation.mutation
    this.comparator = operation.comparator
  }

  reverse(): LHExpressionImpl {
    if (this.comparator === undefined) {
      throw new Error('Cannot reverse non-comparator expression!')
    }
    return new LHExpressionImpl(this.lhs, { comparator: reverseComparator(this.comparator) }, this.rhs)
  }
}

function reverseComparator(comparator: Comparator): Comparator {
  switch (comparator) {
    case Comparator.LESS_THAN:
      return Comparator.GREATER_THAN_EQ
    case Comparator.GREATER_THAN:
      return Comparator.LESS_THAN_EQ
    case Comparator.LESS_THAN_EQ:
      return Comparator.GREATER_THAN
    case Comparator.GREATER_THAN_EQ:
      return Comparator.LESS_THAN
    case Comparator.IN:
      return Comparator.NOT_IN
    case Comparator.NOT_IN:
      return Comparator.IN
    case Comparator.EQUALS:
      return Comparator.NOT_EQUALS
    case Comparator.NOT_EQUALS:
      return Comparator.EQUALS
  }
}

export class CastExpressionImpl extends LHExpressionBase {
  constructor(
    readonly source: LHValue,
    readonly targetType: VariableType
  ) {
    super()
  }
}

export class SizeOfExpressionImpl extends LHExpressionBase {
  constructor(readonly operand: LHValue) {
    super()
  }
}

/** A format string whose placeholders ({0}, {1}, ...) are filled at runtime. */
export class LHFormatString {
  constructor(
    readonly format: string,
    readonly args: LHValue[]
  ) {}
}
