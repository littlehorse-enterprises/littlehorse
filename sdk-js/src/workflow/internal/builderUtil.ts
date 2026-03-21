import type { VariableAssignment } from '../../proto/common_wfspec'
import { toVariableValue } from '../../utils/variableValueConvert'
import type { WorkflowRhs } from '../workflowRhs'
import { LHExpressionImpl } from './lhExpressionImpl'
import { LHFormatStringImpl } from './lhFormatStringImpl'
import { NodeOutputImpl } from './nodeOutputImpl'
import { WfRunVariableImpl } from './wfRunVariableImpl'

export function assignVariable(rhs: WorkflowRhs, assign: (r: WorkflowRhs) => VariableAssignment): VariableAssignment {
  if (rhs === null || rhs === undefined) {
    return {
      path: undefined,
      source: { $case: 'literalValue', value: { value: undefined } },
      targetType: undefined,
    }
  }
  if (rhs instanceof WfRunVariableImpl) {
    return rhs.toAssignment()
  }
  if (rhs instanceof NodeOutputImpl) {
    return rhs.toAssignment()
  }
  if (rhs instanceof LHFormatStringImpl) {
    return rhs.toAssignment(assign)
  }
  if (rhs instanceof LHExpressionImpl) {
    return rhs.toAssignment(assign)
  }
  return {
    path: undefined,
    source: { $case: 'literalValue', value: toVariableValue(rhs) },
    targetType: undefined,
  }
}
