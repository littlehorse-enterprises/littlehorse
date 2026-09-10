import { VariableAssignment, VariableAssignment_Expression } from '../proto/common_wfspec'
import { TypeDefinition, VariableValue } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { objToVarVal } from '../common/serde'
import { CastExpressionImpl, LHExpressionImpl, LHFormatString, SizeOfExpressionImpl } from './expressions'
import { NodeOutput } from './nodeOutputs'
import { LHStructBuilder } from './structBuilders'
import { WfRunVariable } from './variables'

// Serde lives in common/ so the wfsdk and the worker cannot drift apart;
// re-exported here because callers already import it from this module.
export { objToVarVal, variableTypeOf as variableTypeFromValue } from '../common/serde'

/**
 * Converts any LHValue (literal, WfRunVariable, NodeOutput, expression, format
 * string) into a VariableAssignment (mirrors Java BuilderUtil.assignVariable).
 *
 * `typeHint` carries the declared type of the value's counterpart (the other
 * side of a comparison or the mutated variable). JS cannot spell a
 * double-typed whole number the way Java's `100.0` can, so when the hint is
 * DOUBLE a whole-number literal compiles as DOUBLE — otherwise the server
 * refuses the INT-vs-DOUBLE comparison at run time, per run. Deliberate
 * divergence from Java, where the literal's own static type always wins.
 * A bigint stays INT: it is the explicit force-INT escape hatch.
 */
export function toVariableAssignment(value: unknown, typeHint?: VariableType): VariableAssignment {
  if (typeHint === VariableType.DOUBLE && typeof value === 'number' && Number.isInteger(value)) {
    return VariableAssignment.create({
      source: {
        oneofKind: 'literalValue',
        literalValue: VariableValue.create({ value: { oneofKind: 'double', double: value } }),
      },
    })
  }
  if (value instanceof WfRunVariable) {
    const out = VariableAssignment.create({
      source: { oneofKind: 'variableName', variableName: value.name },
    })
    if (value.jsonPathStr !== undefined) {
      out.path = { oneofKind: 'jsonPath', jsonPath: value.jsonPathStr }
    } else if (value.lhPath.length > 0) {
      out.path = { oneofKind: 'lhPath', lhPath: { path: value.lhPath } }
    }
    return out
  }

  if (value instanceof NodeOutput) {
    const out = VariableAssignment.create({
      source: { oneofKind: 'nodeOutput', nodeOutput: { nodeName: value.nodeName } },
    })
    if (value.jsonPathStr !== undefined) {
      out.path = { oneofKind: 'jsonPath', jsonPath: value.jsonPathStr }
    } else if (value.lhPath.length > 0) {
      out.path = { oneofKind: 'lhPath', lhPath: { path: value.lhPath } }
    }
    return out
  }

  if (value instanceof LHFormatString) {
    return VariableAssignment.create({
      source: {
        oneofKind: 'formatString',
        formatString: {
          format: toVariableAssignment(value.format),
          args: value.args.map(arg => toVariableAssignment(arg)),
        },
      },
    })
  }

  if (value instanceof CastExpressionImpl) {
    const out = toVariableAssignment(value.source)
    out.targetType = TypeDefinition.create({
      definedType: { oneofKind: 'primitiveType', primitiveType: value.targetType },
      masked: false,
    })
    return out
  }

  if (value instanceof LHStructBuilder) {
    return VariableAssignment.create({
      source: { oneofKind: 'structBuilder', structBuilder: value.toProto() },
    })
  }

  if (value instanceof SizeOfExpressionImpl) {
    return VariableAssignment.create({
      source: { oneofKind: 'sizeOf', sizeOf: { operand: toVariableAssignment(value.operand) } },
    })
  }

  if (value instanceof LHExpressionImpl) {
    return VariableAssignment.create({
      source: { oneofKind: 'expression', expression: expressionToProto(value) },
    })
  }

  return VariableAssignment.create({
    source: { oneofKind: 'literalValue', literalValue: objToVarVal(value) },
  })
}

/**
 * True when a value's declared type is known to be DOUBLE: a declared-DOUBLE
 * variable, a castToDouble(), or an expression containing either. Only
 * declared types count — a fractional literal on the other side is not
 * treated as context, so plain literal-vs-literal encoding never changes.
 */
export function isDoubleContext(value: unknown): boolean {
  if (value instanceof WfRunVariable) {
    return (
      value.jsonPathStr === undefined &&
      value.lhPath.length === 0 &&
      value.typeDef.definedType.oneofKind === 'primitiveType' &&
      value.typeDef.definedType.primitiveType === VariableType.DOUBLE
    )
  }
  if (value instanceof CastExpressionImpl) return value.targetType === VariableType.DOUBLE
  if (value instanceof LHExpressionImpl) return isDoubleContext(value.lhs) || isDoubleContext(value.rhs)
  return false
}

function expressionToProto(expr: LHExpressionImpl): VariableAssignment_Expression {
  const out = VariableAssignment_Expression.create({
    lhs: toVariableAssignment(expr.lhs, isDoubleContext(expr.rhs) ? VariableType.DOUBLE : undefined),
    rhs: toVariableAssignment(expr.rhs, isDoubleContext(expr.lhs) ? VariableType.DOUBLE : undefined),
  })
  if (expr.mutation !== undefined) {
    out.operation = { oneofKind: 'mutationType', mutationType: expr.mutation }
  } else {
    out.operation = { oneofKind: 'comparator', comparator: expr.comparator! }
  }
  return out
}
