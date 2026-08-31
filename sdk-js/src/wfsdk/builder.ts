import { VariableAssignment } from '../proto/common_wfspec'
import { toVariableValue } from '../worker/variableMapping'

/**
 * Literal → VariableAssignment per R8 (rules.md). Encoding is delegated to
 * the worker's converter so the wfsdk and worker layers cannot drift apart.
 */
export function toVariableAssignment(value: unknown): VariableAssignment {
  return VariableAssignment.create({
    source: { oneofKind: 'literalValue', literalValue: toVariableValue(value) },
  })
}
