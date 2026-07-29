import { VariableAssignment, VariableAssignment_Expression } from '../proto/common_wfspec'
import { TypeDefinition } from '../proto/type_definition'
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
