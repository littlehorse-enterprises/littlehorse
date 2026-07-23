import { Timestamp } from '../proto/google/protobuf/timestamp'
import { VariableAssignment, VariableAssignment_Expression } from '../proto/common_wfspec'
import { TypeDefinition, VariableValue } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { CastExpressionImpl, LHExpressionImpl, LHFormatString, SizeOfExpressionImpl } from './expressions'
import { NodeOutput } from './nodeOutputs'
import { WfRunVariable } from './variables'

/**
 * Converts a JS literal to a VariableValue (mirrors Java LHLibUtil.objToVarVal).
 *
 * JS has a single `number` type, so integer-valued numbers become INT and
 * everything else DOUBLE. Pass a bigint (or a WfRunVariable of type DOUBLE)
 * when an integer-valued DOUBLE literal is needed.
 */
export function objToVarVal(value: unknown): VariableValue {
  if (value === null || value === undefined) {
    return VariableValue.create()
  }
  if (typeof value === 'number') {
    if (Number.isInteger(value)) {
      return VariableValue.create({ value: { oneofKind: 'int', int: String(value) } })
    }
    return VariableValue.create({ value: { oneofKind: 'double', double: value } })
  }
  if (typeof value === 'bigint') {
    return VariableValue.create({ value: { oneofKind: 'int', int: String(value) } })
  }
  if (typeof value === 'string') {
    return VariableValue.create({ value: { oneofKind: 'str', str: value } })
  }
  if (typeof value === 'boolean') {
    return VariableValue.create({ value: { oneofKind: 'bool', bool: value } })
  }
  if (value instanceof Uint8Array) {
    return VariableValue.create({ value: { oneofKind: 'bytes', bytes: value } })
  }
  if (value instanceof Date) {
    return VariableValue.create({ value: { oneofKind: 'utcTimestamp', utcTimestamp: Timestamp.fromDate(value) } })
  }
  if (Array.isArray(value)) {
    return VariableValue.create({ value: { oneofKind: 'jsonArr', jsonArr: JSON.stringify(value) } })
  }
  if (typeof value === 'object') {
    return VariableValue.create({ value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify(value) } })
  }
  throw new Error(`Cannot convert value of type ${typeof value} to a VariableValue`)
}

/** Maps a VariableValue's set field to its VariableType (Java LHLibUtil.fromValueCase). */
export function variableTypeFromValue(value: VariableValue): VariableType {
  switch (value.value.oneofKind) {
    case 'str':
      return VariableType.STR
    case 'int':
      return VariableType.INT
    case 'double':
      return VariableType.DOUBLE
    case 'bool':
      return VariableType.BOOL
    case 'bytes':
      return VariableType.BYTES
    case 'utcTimestamp':
      return VariableType.TIMESTAMP
    case 'jsonObj':
      return VariableType.JSON_OBJ
    case 'jsonArr':
      return VariableType.JSON_ARR
    case 'wfRunId':
      return VariableType.WF_RUN_ID
    default:
      throw new Error(`Cannot infer VariableType from value: ${value.value.oneofKind}`)
  }
}

/**
 * Converts any LHValue (literal, WfRunVariable, NodeOutput, expression, format
 * string) into a VariableAssignment (mirrors Java BuilderUtil.assignVariable).
 */
export function toVariableAssignment(value: unknown): VariableAssignment {
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

function expressionToProto(expr: LHExpressionImpl): VariableAssignment_Expression {
  const out = VariableAssignment_Expression.create({
    lhs: toVariableAssignment(expr.lhs),
    rhs: toVariableAssignment(expr.rhs),
  })
  if (expr.mutation !== undefined) {
    out.operation = { oneofKind: 'mutationType', mutationType: expr.mutation }
  } else {
    out.operation = { oneofKind: 'comparator', comparator: expr.comparator! }
  }
  return out
}
